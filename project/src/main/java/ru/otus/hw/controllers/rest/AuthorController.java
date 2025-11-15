package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    private final MessageProvider messageProvider;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDto>> getAuthorById(@PathVariable Long id) {
        try {
            AuthorDto authorDto = authorService.getAuthorById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("author.not_found", id)
                    ));
            return ResponseEntity.ok(ApiResponse.success(authorDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("author.load_error") + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<AuthorDto>> getAuthorByUserId(@PathVariable Long userId) {
        try {
            AuthorDto authorDto = authorService.getAuthorByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("author.not_found.user", userId)
                    ));
            return ResponseEntity.ok(ApiResponse.success(authorDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("author.load_error") + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDto>>> getAllAuthors() {
        try {
            List<AuthorDto> authors = authorService.getAllAuthors();
            return ResponseEntity.ok(ApiResponse.success(authors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("authors.load_error") + e.getMessage()));
        }
    }
}