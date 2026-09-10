package school.artem.reservation.reservations.availability;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationRequest (
        @NotNull
        Long roomId,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {

}
