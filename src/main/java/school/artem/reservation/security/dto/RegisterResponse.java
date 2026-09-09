package school.artem.reservation.security.dto;

import school.artem.reservation.security.Role;

public record RegisterResponse (
        Long id,
        String username,
        Role role
) {}
