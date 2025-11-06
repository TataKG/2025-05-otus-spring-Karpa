package ru.otus.hw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.otus.hw.services.UserDetailService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailService userDetailService;

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
                        // Статические ресурсы и публичные endpoints
                        .requestMatchers("/", "/login", "/logout", "/error").permitAll()
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/css/**", "/js/**", "/webjars/**").permitAll()

                        // API endpoints - исправляем hasAnyRole
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "USER")

                        // Web интерфейс
                        .requestMatchers("/books/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/genres/**", "/authors/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/books", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
