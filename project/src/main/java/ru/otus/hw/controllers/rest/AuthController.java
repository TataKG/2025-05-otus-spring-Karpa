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
            return ResponseEntity.ok(ApiResponse.success(new AuthUserResponse(false, null, null, null)));
        }

        String username = authentication.getName();
        System.out.println("Authenticated user: " + username);

        UserDto userDto = userService.getUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found.username", username)
                ));

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        System.out.println("User authorities: " + authorities);

        // Получаем информацию об авторе (биографию)
        String bio = userService.getUserBio(username);

        System.out.println("Returning user data: " + new AuthUserResponse(true, userDto.username(), authorities, bio));

        return ResponseEntity.ok(ApiResponse.success(
                new AuthUserResponse(true, userDto.username(), authorities, bio)
        ));
    }

    public record RegisterRequest(
            String username,
            String email,
            String password,
            String bio  // Добавляем поле bio
    ) {}

    public record LoginRequest(String username, String password) {}

    public record AuthUserResponse(
            boolean authenticated,
            String name,
            List<String> authorities,
            String bio  // Добавляем bio в ответ
    ) {}
}