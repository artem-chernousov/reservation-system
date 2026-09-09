package school.artem.reservation.security.dto;

public record LoginRequest (
        String username,
        String password
) {}
