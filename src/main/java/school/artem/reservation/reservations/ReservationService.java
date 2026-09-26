package school.artem.reservation.reservations;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.artem.reservation.reservations.availability.CreateReservationRequest;
import school.artem.reservation.reservations.availability.ReservationAvailabilityService;
import school.artem.reservation.reservations.availability.UpdateReservationRequest;
import school.artem.reservation.user.Role;
import school.artem.reservation.user.UserEntity;
import school.artem.reservation.user.UserRepository;
import school.artem.reservation.web.UserNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {
    private final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;

    private final UserRepository userRepository;

    private final ReservationMapper mapper;

    private final ReservationAvailabilityService availabilityService;

    public ReservationService(ReservationRepository repository, UserRepository userRepository, ReservationMapper mapper, ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id, String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));


        Long userId = userEntity.getId();
        Long userIdReservation = reservationEntity.getUserId();

        boolean isOwner = userId.equals(userIdReservation);
        boolean isAdmin = userEntity.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can't get this reservation");
        }

        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> getAllReservations(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = userEntity.getId();

        List<ReservationEntity> allEntities = repository.findAllByUserIdOrderByIdAsc(userId);

        return allEntities.stream().map(mapper::toDomain).toList();
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : 10;

        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );
        return allEntities.stream().map(mapper::toDomain).toList();
    }

    public Reservation createReservation(CreateReservationRequest createRequest, String username) {
        if(!createRequest.endDate().isAfter(createRequest.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if(createRequest.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date can't be in the past");
        }

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        var reservationToCreate = new Reservation(
                null,
                userEntity.getId(),
                createRequest.roomId(),
                createRequest.startDate(),
                createRequest.endDate(),
                ReservationStatus.PENDING
        );

        var entityToSave = mapper.toEntity(reservationToCreate);

        var savedEntity = repository.save(entityToSave);

        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(
            Long id,
            UpdateReservationRequest updateReservation,
            String username
    ) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = userEntity.getId();

        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        Long idUserReservation = reservationEntity.getUserId();

        if(!idUserReservation.equals(userId) && userEntity.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can't modify this reservation");
        }

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't modify reservation: status=" + reservationEntity.getStatus());
        }

        if(!updateReservation.endDate().isAfter(updateReservation.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if(updateReservation.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date can't be in the past");
        }

        Long reservationOwnerId = reservationEntity.getUserId();

        var reservationToUpdate = new Reservation(
                null,
                reservationOwnerId,
                updateReservation.roomId(),
                updateReservation.startDate(),
                updateReservation.endDate(),
                ReservationStatus.PENDING
        );

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setUserId(reservationOwnerId);
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = repository.save(reservationToSave);

        return mapper.toDomain(updatedReservation);
    }


    @Transactional
    public void cancelReservation(Long id, String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = userEntity.getId();

        var reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        Long idUserReservation = reservation.getUserId();

        boolean isOwner = idUserReservation.equals(userId);
        boolean isAdmin = userEntity.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can't cancel this reservation");
        }

        if(reservation.getStatus().equals(ReservationStatus.APPROVED) && !isAdmin) {
            throw new IllegalStateException("Can't cancel approved reservation. Contact with manager please");
        }

        if(reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Can't cancel the reservation. Reservation was already cancelled");
        }

        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can't approve reservation: status=" + reservationEntity.getStatus());
        }

        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if(!isAvailableToApprove) {
            throw new IllegalStateException("Can't approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);

        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }
}