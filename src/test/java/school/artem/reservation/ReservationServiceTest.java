package school.artem.reservation;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import school.artem.reservation.reservations.*;
import school.artem.reservation.reservations.availability.CreateReservationRequest;
import school.artem.reservation.reservations.availability.ReservationAvailabilityService;
import org.springframework.data.domain.Pageable;
import school.artem.reservation.user.Role;
import school.artem.reservation.user.UserEntity;
import school.artem.reservation.user.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    ReservationService reservationService;

    @Mock
    private ReservationRepository repository;

    @Mock
    private ReservationMapper mapper;

    @Mock
    private ReservationAvailabilityService availabilityService;

    @Mock
    private UserRepository userRepository;

    @Test
    void getReservationById_shouldReturnReservation() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity();

        Reservation expectedReservation = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(reservationEntity));

        Mockito.when(mapper.toDomain(reservationEntity))
                .thenReturn(expectedReservation);

        Reservation result = reservationService.getReservationById(id);

        Assertions.assertEquals(expectedReservation, result);

        Mockito.verify(repository).findById(id);
        Mockito.verify(mapper).toDomain(reservationEntity);
    }

    @Test
    void getReservationById_shouldThrowException_whenEntityNotFound() {
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationById(id));

        Mockito.verify(mapper, Mockito.never()).toDomain(Mockito.any());
    }

    @Test
    void createReservation_shouldCreateReservation() {
        var createRequest = new CreateReservationRequest(
                100L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27)
        );

        var reservationToCreate = new Reservation(
                null,
                1L,
                createRequest.roomId(),
                createRequest.startDate(),
                createRequest.endDate(),
                ReservationStatus.PENDING
        );

        ReservationEntity reservationToSave = new ReservationEntity();
        ReservationEntity savedEntity = new ReservationEntity();

        var expectedReservation = new Reservation(
                1L,
                1L,
                100L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(mapper.toEntity(reservationToCreate)).thenReturn(reservationToSave);
        Mockito.when(repository.save(reservationToSave)).thenReturn(savedEntity);
        Mockito.when(mapper.toDomain(savedEntity)).thenReturn(expectedReservation);

        String name = "Artem";

        var userEntity = new UserEntity(
                1L,
                "Artem",
                "password",
                Role.USER
        );

        Mockito.when(userRepository.findByUsername(name)).thenReturn(Optional.of(userEntity));

        Reservation result = reservationService.createReservation(createRequest, name);

        Assertions.assertEquals(expectedReservation, result);

        Mockito.verify(mapper).toEntity(reservationToCreate);
        Mockito.verify(repository, Mockito.times(1)).save(reservationToSave);
        Mockito.verify(userRepository).findByUsername(name);
    }

    @Test
    void createReservation_withStartDateAfterEndDate_ThrowsException() {
        var reservationToCreate = new CreateReservationRequest(
                100L,
                LocalDate.of(2026, 8, 27),
                LocalDate.of(2026, 8, 25)
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservationToCreate, "Artem"));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void searchAllByFilter_shouldReturnReservation() {
        ReservationSearchFilter filter = new ReservationSearchFilter(
                1L,
                1L,
                5,
                2
        );

        var pageable = Pageable.ofSize(filter.pageSize()).withPage(filter.pageNumber());

        ReservationEntity entity1 = new ReservationEntity();
        ReservationEntity entity2 = new ReservationEntity();

        List<ReservationEntity> listOfEntities = List.of(entity1, entity2);

        Reservation expectedReservation1 = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Reservation expectedReservation2 = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 8, 30),
                ReservationStatus.PENDING
        );

        List<Reservation> expectedListOfReservations = List.of(expectedReservation1, expectedReservation2);

        Mockito.when(repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        )).thenReturn(listOfEntities);

        Mockito.when(mapper.toDomain(entity1)).thenReturn(expectedReservation1);
        Mockito.when(mapper.toDomain(entity2)).thenReturn(expectedReservation2);

        List<Reservation> result = reservationService.searchAllByFilter(filter);

        Assertions.assertEquals(expectedListOfReservations, result);

        Mockito.verify(repository).searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable);
    }

    @Test
    void searchAllByFilter_shouldUseDefaultPagination_whenSizeAndNumberAreNull() {
        ReservationSearchFilter filter = new ReservationSearchFilter(
                1L,
                1L,
                null,
                null
        );
        var expectedPageable = Pageable.ofSize(10).withPage(0);

        ReservationEntity entity1 = new ReservationEntity();
        ReservationEntity entity2 = new ReservationEntity();

        List<ReservationEntity> listOfEntities = List.of(entity1, entity2);

        Reservation expectedReservation1 = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Reservation expectedReservation2 = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 8, 30),
                ReservationStatus.PENDING
        );

        List<Reservation> expectedListOfReservations = List.of(expectedReservation1, expectedReservation2);

        Mockito.when(repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                expectedPageable
        )).thenReturn(listOfEntities);

        Mockito.when(mapper.toDomain(entity1)).thenReturn(expectedReservation1);
        Mockito.when(mapper.toDomain(entity2)).thenReturn(expectedReservation2);

        List<Reservation> result = reservationService.searchAllByFilter(filter);

        Assertions.assertEquals(expectedListOfReservations, result);

        Mockito.verify(repository).searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                expectedPageable);
    }

    @Test
    void updateReservation_shouldUpdateReservation() {
        Long id = 1L;

        Reservation reservationToUpdate = new Reservation(
                null,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                ReservationStatus.PENDING
        );
        ReservationEntity reservationToSave = new ReservationEntity();
        ReservationEntity updatedReservation = new ReservationEntity();

        Reservation expectedReservation = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));
        Mockito.when(mapper.toEntity(reservationToUpdate)).thenReturn(reservationToSave);
        Mockito.when(repository.save(reservationToSave)).thenReturn(updatedReservation);
        Mockito.when(mapper.toDomain(updatedReservation)).thenReturn(expectedReservation);

        Reservation actual = reservationService.updateReservation(id, reservationToUpdate);

        Assertions.assertEquals(expectedReservation, actual);
        Assertions.assertEquals(ReservationStatus.PENDING, reservationToSave.getStatus());
        Assertions.assertEquals(1L, reservationToSave.getId());

        Mockito.verify(repository).save(reservationToSave);
    }

    @Test
    void updateReservation_shouldThrowException_whenStatusNotPending() {
        Long id = 1L;

        Reservation reservationToUpdate = new Reservation(
                null,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                ReservationStatus.APPROVED
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalStateException.class, () -> reservationService.updateReservation(id, reservationToUpdate));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateReservation_withStartDateAfterEndDate_ThrowsException() {
        Long id = 1L;

        Reservation reservationToUpdate = new Reservation(
                null,
                50L,
                5L,
                LocalDate.of(2026, 8, 27),
                LocalDate.of(2026, 8, 25),
                ReservationStatus.PENDING
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalArgumentException.class, () -> reservationService.updateReservation(id, reservationToUpdate));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateReservation_withStartDateEqualsEndDate_ThrowsException() {
        Long id = 1L;

        Reservation reservationToUpdate = new Reservation(
                null,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 25),
                ReservationStatus.PENDING
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalArgumentException.class, () -> reservationService.updateReservation(id, reservationToUpdate));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateReservation_shouldThrowException_whenEntityNotFound() {
        Long id = 1L;

        Reservation reservationToUpdate = new Reservation(
                null,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                null
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.updateReservation(id, reservationToUpdate));
        Mockito.verify(mapper, Mockito.never()).toEntity(Mockito.any());
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
        Mockito.verify(mapper, Mockito.never()).toDomain(Mockito.any());
    }

    @Test
    void cancelReservation_shouldCancelReservation() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        reservationService.cancelReservation(id);

        Mockito.verify(repository).setStatus(id, ReservationStatus.CANCELLED);
    }

    @Test
    void cancelReservation_shouldThrowException_whenStatusApproved() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                ReservationStatus.APPROVED
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(id));
        Mockito.verify(repository, Mockito.never())
                .setStatus(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void cancelReservation_shouldThrowException_whenStatusCancelled() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 23),
                LocalDate.of(2026, 8, 24),
                ReservationStatus.CANCELLED
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(id));
        Mockito.verify(repository, Mockito.never())
                .setStatus(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void cancelReservation_shouldThrowException_whenEntityNotFound() {
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.cancelReservation(id));
        Mockito.verify(repository, Mockito.never()).setStatus(Mockito.any(), Mockito.any());
    }

    @Test
    void approveReservation_shouldApproveReservation() {
        Long id = 1L;

        Reservation expectedReservation = new Reservation(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.APPROVED
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Mockito.when(availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        )).thenReturn(true);

        Mockito.when(mapper.toDomain(reservationEntity)).thenReturn(expectedReservation);

        var actual = reservationService.approveReservation(id);

        Assertions.assertEquals(expectedReservation, actual);
        Assertions.assertEquals(
                ReservationStatus.APPROVED,
                reservationEntity.getStatus()
        );

        Mockito.verify(repository).save(reservationEntity);
    }

    @Test
    void approveReservation_shouldThrowException_whenStatusNotPending() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.CANCELLED
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Assertions.assertThrows(IllegalStateException.class, () -> reservationService.approveReservation(id));

        Mockito.verify(availabilityService, Mockito.never()).isReservationAvailable(
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.any()
        );

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
        Mockito.verify(mapper, Mockito.never()).toDomain(Mockito.any());
    }

    @Test
    void approveReservation_shouldThrowException_isAvailableToApproveIsFalse() {
        Long id = 1L;

        ReservationEntity reservationEntity = new ReservationEntity(
                1L,
                50L,
                5L,
                LocalDate.of(2026, 8, 25),
                LocalDate.of(2026, 8, 27),
                ReservationStatus.PENDING
        );

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(reservationEntity));

        Mockito.when(availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        )).thenReturn(false);

        Assertions.assertThrows(IllegalStateException.class, () -> reservationService.approveReservation(id));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void approveReservation_shouldThrowException_whenEntityNotFound() {
        Long id = 1L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> reservationService.approveReservation(id));
        Mockito.verify(availabilityService, Mockito.never()).isReservationAvailable(
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.any()
        );
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
        Mockito.verify(mapper, Mockito.never()).toDomain(Mockito.any());
    }
}