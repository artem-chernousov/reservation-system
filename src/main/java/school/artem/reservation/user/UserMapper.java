package school.artem.reservation.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import school.artem.reservation.auth.dto.RegisterResponse;

@Component
public class UserMapper {
    public UserDetails toDetails(
            UserEntity userEntity
    ) {
        return User.withUsername(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole().name())
                .build();
    }

    public RegisterResponse toRegisterResponse(
            UserEntity userEntity
    ) {
        return new RegisterResponse(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getRole()
        );
    }
}
