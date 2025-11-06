package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.UserService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MessageProvider messageProvider;

    public UserController(UserService userService, MessageProvider messageProvider) {
        this.userService = userService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
        UserDto userDto = userService.createUser(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(userDto, messageProvider.getMessage("user.created"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        UserDto userDto = userService.getUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found.username", username)
                ));
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllEnabledUsers() {
        List<UserDto> users = userService.getAllEnabledUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.userExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    public record CreateUserRequest(String username, String email, String password) {}
}
