package school.artem.reservation.security.dto;

public record RegisterRequest(
        String username,
        String password
) {}
