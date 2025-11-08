package ru.otus.hw.services;

import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Optional<BookDto> findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookFormDto bookDto);

    BookDto update(BookFormDto bookDto);

    void deleteById(long id);

}