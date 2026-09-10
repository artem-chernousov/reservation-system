package school.artem.reservation.auth.dto;

public record LoginRequest (
        String username,
        String password
) {}
