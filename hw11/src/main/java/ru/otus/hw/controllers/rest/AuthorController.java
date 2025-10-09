package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.AuthorRepository;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRepository authorRepository;

    private final AuthorDtoConverter authorDtoConverter;

    @GetMapping
    public Flux<AuthorDto> getAllAuthors() {
        return authorRepository.findAll()
                .map(authorDtoConverter::toDto);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AuthorDto>> getAuthorById(@PathVariable String id) {
        return authorRepository.findById(id)
                .map(authorDtoConverter::toDto)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Author with id %s not found!".formatted(id))));
    }
}
