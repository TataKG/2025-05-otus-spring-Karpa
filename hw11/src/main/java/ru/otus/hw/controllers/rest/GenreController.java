package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.GenreRepository;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    private final GenreDtoConverter genreDtoConverter;

    @GetMapping
    public Flux<GenreDto> getAllGenres() {
        return genreRepository.findAll()
                .map(genreDtoConverter::toDto);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<GenreDto>> getGenreById(@PathVariable String id) {
        return genreRepository.findById(id)
                .map(genreDtoConverter::toDto)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Genre with id %s not found!".formatted(id))));
    }
}
