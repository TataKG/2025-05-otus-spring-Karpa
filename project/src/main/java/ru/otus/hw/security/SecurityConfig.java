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
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
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

                        // API endpoints - разрешаем неаутентифицированный доступ к публичным данным
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/recipes", "/api/recipes/published", "/api/recipes/category/**").permitAll()
                        .requestMatchers("/api/recipes/{id}", "/api/recipes/{id}/detailed").permitAll()
                        .requestMatchers("/api/recipes/search/**", "/api/recipes/filter").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()

                        // API endpoints требующие аутентификации
                        .requestMatchers("/api/recipes/my-recipes").authenticated()
                        .requestMatchers("/api/recipes/create-form-data", "/api/recipes/edit-form-data/**").authenticated()
                        .requestMatchers("/api/recipes/**").authenticated() // все остальные API рецептов
                        .requestMatchers("/api/comments/**").authenticated()

                        // Админские endpoints
                        .requestMatchers("/api/admin/**", "/api/authors/**", "/api/users/**", "/api/inventory/**").hasAuthority("ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")

                        // H2 console - только для админов
                        .requestMatchers("/h2-console/**").hasAuthority("ADMIN")

                        // Web страницы
                        .requestMatchers("/my-recipes").authenticated()
                        .requestMatchers("/recipe/create", "/recipe/edit/**").authenticated()
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