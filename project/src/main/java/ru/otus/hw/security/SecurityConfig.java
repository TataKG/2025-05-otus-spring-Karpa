package ru.otus.hw.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                // CORS конфигурация
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF конфигурация - отключаем для API, оставляем для форм
                .csrf(csrf -> csrf
                                .ignoringRequestMatchers("/h2-console/**", "/api/**")
                        // Убрали CookieCsrfTokenRepository для API endpoints
                )

                // Headers для H2 console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // Авторизация запросов
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints - расширенный список
                        .requestMatchers(
                                "/", "/index.html", "/login", "/logout", "/register", "/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",
                                "/create-form-data", "/edit-form-data/**"  // Добавили пути формы
                        ).permitAll()

                        // API auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/exists/**").permitAll()

                        // Recipes - READ operations are public
                        .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/*/comments/**").permitAll()

                        // Recipes - WRITE operations require auth
                        .requestMatchers(HttpMethod.POST, "/api/recipes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/recipes/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/recipes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").authenticated()

                        // Categories - public read access
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Inventory - public read access
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**", "/admin/**", "/h2-console/**").hasRole("ADMIN")

                        // Authenticated user endpoints
                        .requestMatchers("/my-recipes", "/recipe/create", "/recipe/edit/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // User details service
                .userDetailsService(userDetailsService)

                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Для API запросов возвращаем 401, для веб - редирект на логин
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Для API запросов возвращаем 403, для веб - страница ошибки
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"Forbidden\"}");
                            } else {
                                response.sendRedirect("/error?access_denied");
                            }
                        })
                );

        return http.build();
    }
}