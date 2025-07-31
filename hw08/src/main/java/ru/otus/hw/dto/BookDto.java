package ru.otus.hw.dto;

public record BookDto(String id, String title, AuthorDto author, GenreDto genre) {
}
