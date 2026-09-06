package school.artem.reservation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/*
    CSRF — это защита от ситуации, когда чужой сайт заставляет браузер отправить запрос от твоего имени.

    UserDetailsService - объект, который умеет найти пользователя
    UserDetails — это объект с данными пользователя для Spring Security
    PasswordEncoder - объект, который умеет сравнить пароль
    AuthenticationManager - умеет запускать аутентификацию
    ProviderManager - реализует AuthenticationManager, управляет одним или несколькими AuthenticationProvider
       │
       └── внутри DaoAuthenticationProvider
                    │
                    ├── UserDetailsService
                    └── PasswordEncoder
    DaoAuthenticationProvider — конкретный AuthenticationProvider, кто реально выполняет проверку username/password.

    http.build() - Я закончил настраивать правила. Создай из них готовую цепочку безопасности
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
    Spring создаёт HttpSecurity
        ↓
    ты говоришь:
    "защищай все запросы"
            ↓
    "требуй авторизацию"
            ↓
    "используй Basic Auth"
            ↓
    http.build()
            ↓
    готовый SecurityFilterChain

    Метод securityFilter() при запуске приложения создаёт SecurityFilterChain.
    Потом через эту цепочку проходят все GET, POST, PUT, DELETE и другие HTTP-запросы.
    В нашем случае любой запрос требует аутентификации через HTTP Basic.
    Возвращает готовый SecurityFilterChain — то есть уже собранную цепочку правил/фильтров безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated()
        )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /*
        AuthenticationManager — объект, которому мы можем сказать: «вот логин и пароль — проверь их».
        AuthenticationManager получает логин/пароль
        → проверяет пользователя
        → если всё хорошо, возвращает подтверждённый Authentication;
        если нет — кидает исключение.
        Разрешает вернуть ProviderManager, потому что он реазилует сам AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(userDetailsService); // Пользователя ищи через вот этот userDetailsService

        authenticationProvider.setPasswordEncoder(passwordEncoder); // Пароли сравнивай при помощи passwordEncoder

        /*
            переменная типа AuthenticationManager содержит объект ProviderManager.
            А ProviderManager внутри хранит: DaoAuthenticationProvider
         */
        return new ProviderManager(authenticationProvider);
    }

    /*
        Создаём пользователя user/password с ролью USER.

        InMemoryUserDetailsManager хранит этого пользователя в памяти
        и умеет потом находить его по username.

        Возвращаем объект типа UserDetailsService.

        Когда Security спрашивает:
        Есть пользователь user?
        UserDetailsService возвращает его данные.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER").build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    /*
        Это объект, который умеет правильно работать с зашифрованными паролями
        и сравнивать их безопасным способом.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
