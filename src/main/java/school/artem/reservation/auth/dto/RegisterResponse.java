package school.artem.reservation.auth.dto;

import school.artem.reservation.user.Role;

public record RegisterResponse (
        Long id,
        String username,
        Role role
) {}
