package school.artem.reservation.reservations.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {

}
