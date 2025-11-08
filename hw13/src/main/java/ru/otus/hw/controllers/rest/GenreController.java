package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.GenreService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<GenreDto> getAllGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getAGenreById(@PathVariable Long id) {
        return genreService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %s not found!".formatted(id)));
    }
}
