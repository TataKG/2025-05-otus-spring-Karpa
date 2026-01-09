package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.UserService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthorService authorService;
    private final MessageProvider messageProvider;

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.success(new AuthUserResponse(false, null, null, null, false)));
            }

            String username = authentication.getName();

            Optional<AuthorDto> authorOpt = authorService.getAuthorByUsername(username);

            if (authorOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(
                        new AuthUserResponse(true, username, List.of("USER"), null, false)
                ));
            }

            AuthorDto author = authorOpt.get();
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            return ResponseEntity.ok(ApiResponse.success(
                    new AuthUserResponse(true, author.user().username(), roles, author.bio(), isAdmin)
            ));
        } catch (Exception e) {
            if (authentication != null && authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.success(
                        new AuthUserResponse(true, authentication.getName(), List.of("USER"), null, false)
                ));
            }

            return ResponseEntity.ok(ApiResponse.success(new AuthUserResponse(false, null, null, null, false)));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request) {

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
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("user.register_error")));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageProvider.getMessage("auth.login_success")));
    }

    public record RegisterRequest(
            @NotBlank(message = "{user.username_empty}")
            @Size(min = 3, max = 50, message = "{user.username_size}")
            @Pattern(regexp = "[a-zA-Z0-9_]+", message = "{user.username_pattern}")
            String username,

            @NotBlank(message = "{user.email_empty}")
            @Email(message = "{user.email_invalid}")
            String email,

            @NotBlank(message = "{user.password_empty}")
            @Size(min = 6, max = 100, message = "{user.password_size}")
            String password,

            @Size(max = 500, message = "{user.bio_max_length}")
            String bio
    ) {
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