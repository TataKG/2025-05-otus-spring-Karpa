package ru.otus.hw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.otus.hw.services.UserService;
import ru.otus.hw.services.UserServiceImpl;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Теперь зависит от отдельного UserDetailsService
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ВРЕМЕННО для тестирования
        return NoOpPasswordEncoder.getInstance();
        // Позже заменить на: return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Статические ресурсы и публичные страницы
                        .requestMatchers("/", "/index.html", "/login", "/logout", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // API endpoints - разделяем доступ
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/recipes/**", "/api/categories/**").permitAll()
                        .requestMatchers("/api/comments/**").hasAnyRole("USER", "AUTHOR", "ADMIN")
                        .requestMatchers("/api/authors/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers("/api/users/**", "/api/inventory/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // H2 console - только для админов
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")

                        // Web страницы
                        .requestMatchers("/my-recipes").hasAnyRole("USER", "AUTHOR", "ADMIN")
                        .requestMatchers("/recipe/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
}