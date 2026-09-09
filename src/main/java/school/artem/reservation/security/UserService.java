package school.artem.reservation.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.artem.reservation.security.dto.RegisterRequest;
import school.artem.reservation.security.dto.RegisterResponse;

@Service
public class UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse registerUser(
            RegisterRequest registerRequest
    ) {
        if(repository.existsByUsername(registerRequest.username())) {
            throw new IllegalStateException("This username already exists.");
        }

        var userToCreate = new UserEntity(
                null,
                registerRequest.username(),
                passwordEncoder.encode(registerRequest.password()),
                Role.USER
        );

        var savedUser = repository.save(userToCreate);

        return userMapper.toRegisterResponse(savedUser);
    }
}
