package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;
    private final MessageProvider messageProvider;

    public AuthorController(AuthorService authorService, MessageProvider messageProvider) {
        this.authorService = authorService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuthorDto>> createAuthor(@RequestBody CreateAuthorRequest request) {
        AuthorDto authorDto = authorService.createAuthor(request.userId(), request.bio());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(authorDto, messageProvider.getMessage("author.created"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDto>> getAuthorById(@PathVariable Long id) {
        AuthorDto authorDto = authorService.getAuthorById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(authorDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<AuthorDto>> getAuthorByUserId(@PathVariable Long userId) {
        AuthorDto authorDto = authorService.getAuthorByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found.user", userId)
                ));
        return ResponseEntity.ok(ApiResponse.success(authorDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDto>>> getAllAuthors() {
        List<AuthorDto> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(ApiResponse.success(authors));
    }

    @PostMapping("/convert/{userId}")
    public ResponseEntity<ApiResponse<AuthorDto>> convertUserToAuthor(
            @PathVariable Long userId,
            @RequestBody ConvertToAuthorRequest request) {
        AuthorDto authorDto = authorService.convertUserToAuthor(userId, request.bio());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(authorDto, messageProvider.getMessage("author.created"))
        );
    }

    public record CreateAuthorRequest(Long userId, String bio) {}
    public record ConvertToAuthorRequest(String bio) {}
}