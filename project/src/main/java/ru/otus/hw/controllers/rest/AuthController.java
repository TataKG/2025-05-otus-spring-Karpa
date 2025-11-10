package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.UserService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MessageProvider messageProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody RegisterRequest request) {
        UserDto userDto = userService.createUser(
                request.username(),
                request.email(),
                request.password(),
                request.bio()  // Добавляем bio
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(userDto, messageProvider.getMessage("user.created"))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful"));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser(Authentication authentication) {
        System.out.println("=== AUTH USER ENDPOINT ===");
        System.out.println("Authentication: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("User not authenticated");
            return ResponseEntity.ok(ApiResponse.success(new AuthUserResponse(false, null, null, null, false)));
        }

        String username = authentication.getName();
        System.out.println("Authenticated user: " + username);

        UserDto userDto = userService.getUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found.username", username)
                ));

        // Получаем роли БЕЗ префикса ROLE_ для фронтенда
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", "")) // Убираем ROLE_ для фронтенда
                .collect(Collectors.toList());

        System.out.println("User roles for frontend: " + roles);

        // Проверяем, есть ли роль ADMIN на бэкенде (с префиксом ROLE_)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        System.out.println("Is admin (backend check): " + isAdmin);

        // Получаем информацию об авторе (биографию)
        String bio = userService.getUserBio(username);

        System.out.println("Returning user data: " + new AuthUserResponse(true, userDto.username(), roles, bio, isAdmin));

        return ResponseEntity.ok(ApiResponse.success(
                new AuthUserResponse(true, userDto.username(), roles, bio, isAdmin)
        ));
    }

    public record RegisterRequest(
            String username,
            String email,
            String password,
            String bio
    ) {}

    public record LoginRequest(String username, String password) {}

    public record AuthUserResponse(
            boolean authenticated,
            String name,
            List<String> roles,
            String bio,
            boolean isAdmin
    ) {}
}