package school.artem.reservation.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/*
    authenticationResponse - берет логин и пароль из authenticationRequest
    и через DaoAuthenticationProvider ищет пользователя и проверяет пароль
    Если пароль неправильный, метод: authenticate(...) не вернёт нормальный результат,
    а выбросит ошибку аутентификации.

    SecurityContext — контейнер, где Spring хранит информацию о текущем авторизованном пользователе.
    SecurityContextHolder — место, откуда Spring в рамках текущего запроса может узнать:
    кто сейчас пользователь?
    SecurityContextRepository - умеет сохранить пользователя, чтобы восстановить в следующем запросе.

 */
/*
    JSON из Postman
    ↓
    LoginRequest
    ↓
    создаём unauthenticated Authentication
    ↓
    AuthenticationManager.authenticate(...)
    ↓
    проверка логина и пароля
    ↓
    успешно → authenticationResponse
 */

@RestController
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();


    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Создаём объект Authentication, который пока ещё НЕ проверен.
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.username(),
                        loginRequest.password()
                );

        Authentication authenticationResponse =
                this.authenticationManager.authenticate(authenticationRequest);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationResponse);

        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);

        log.info("User '{}' authenticated successfully", authenticationResponse.getName());

        return ResponseEntity.ok(authenticationResponse.getName());
    }

    public record LoginRequest(String username, String password) {}
}
