package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.util.MessageProvider;
import ru.otus.hw.dto.AuthorDto;

import java.util.List;

@RestController
@RequestMapping("/api/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDto>>> getAuthors() {
        try {
            List<AuthorDto> authors = authorService.getAllAuthors();
            return ResponseEntity.ok(ApiResponse.success(authors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка при загрузке авторов: " + e.getMessage()));
        }
    }
}