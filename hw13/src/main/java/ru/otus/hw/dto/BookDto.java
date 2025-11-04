package ru.otus.hw.dto;

public record BookDto(long id, String title, AuthorDto author, GenreDto genre) {
}
