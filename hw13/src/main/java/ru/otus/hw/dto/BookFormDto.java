package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookFormDto(long id,
                          @NotBlank(message = "заполните название книги")
                          String title,
                          @NotNull(message = "выберите автора из списка")
                          long authorId,
                          @NotNull(message = "выберите жанр из списка")
                          long genreId) {
}
