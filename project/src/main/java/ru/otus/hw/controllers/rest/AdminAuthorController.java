package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;
    private final MessageProvider messageProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDto>>> getAuthors() {
        try {
            List<AuthorDto> authors = authorService.getAllAuthors();
            return ResponseEntity.ok(ApiResponse.success(authors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(messageProvider.getMessage("authors.load_error") + e.getMessage()));
        }
    }
}