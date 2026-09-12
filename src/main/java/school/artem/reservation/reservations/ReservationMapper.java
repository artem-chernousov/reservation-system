package school.artem.reservation.reservations;

import org.springframework.stereotype.Component;
import school.artem.reservation.reservations.availability.UpdateReservationRequest;

@Component
public class ReservationMapper {
    public Reservation toDomain(
            ReservationEntity reservation
    ) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }

    public ReservationEntity toEntity(
            Reservation reservation
    ) {
        return new ReservationEntity(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.status()
        );
    }
}
