package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;

public interface BookService {
    Mono<BookDto> findById(String id);

    Flux<BookDto> findAll();

    Mono<BookDto> insert(BookFormDto bookDto);

    Mono<BookDto> update(BookFormDto bookDto);

    Mono<Void> deleteById(String id);

}