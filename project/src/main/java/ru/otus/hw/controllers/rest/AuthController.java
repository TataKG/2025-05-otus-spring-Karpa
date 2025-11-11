package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        try {
            UserDto userDto = userService.createUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.bio()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(userDto, messageProvider.getMessage("user.created"))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("user.register_error") + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageProvider.getMessage("auth.login_success")));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.success(new AuthUserResponse(false, null, null, null, false)));
            }

            String username = authentication.getName();

            UserDto userDto = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("user.not_found.username", username)
                    ));

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            String bio = userService.getUserBio(username);

            return ResponseEntity.ok(ApiResponse.success(
                    new AuthUserResponse(true, userDto.username(), roles, bio, isAdmin)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("auth.user_info_error")));
        }
    }

    public record RegisterRequest(
            String username,
            String email,
            String password,
            String bio) {
    }

    public record LoginRequest(
            String username,
            String password) {
    }

    public record AuthUserResponse(
            boolean authenticated,
            String name,
            List<String> roles,
            String bio,
            boolean isAdmin) {
    }
}