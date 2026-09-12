package school.artem.reservation.reservations;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import school.artem.reservation.reservations.availability.CreateReservationRequest;
import school.artem.reservation.reservations.availability.UpdateReservationRequest;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id,
            Authentication authentication
    ){

        String username = authentication.getName();

        log.info("Called getReservationById: id={}", id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id, username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Reservation>> getAllReservations(
            Authentication authentication
    ){

        String username = authentication.getName();

        log.info("Called getAllReservationsById: username={}", username);

        return ResponseEntity.ok(reservationService.getAllReservations(username));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> searchAllByFilter(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ){
        log.info("Called getAllReservations");

        var filter = new ReservationSearchFilter(
                roomId,
                userId,
                pageSize,
                pageNumber
        );

        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid CreateReservationRequest reservationToCreate,
            Authentication authentication
    ) {
        String username = authentication.getName();

        log.info("Creating reservation for user {}", username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateReservationRequest updateReservationRequest,
            Authentication authentication
    ) {

        String username = authentication.getName();

        log.info("User {} updating reservation id={}", username, id);
        var updated = reservationService.updateReservation(id, updateReservationRequest, username);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<String> cancelReservation(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {

        String username = authentication.getName();

        log.info("Called deleteReservation id={}", id);

        reservationService.cancelReservation(id, username);

        return ResponseEntity.ok("Reservation cancelled successfully");
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);

        var reservation = reservationService.approveReservation(id);

        return ResponseEntity.ok(reservation);
    }
}
