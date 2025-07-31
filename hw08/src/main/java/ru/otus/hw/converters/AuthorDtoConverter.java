package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

@Component
public class AuthorDtoConverter {
    public String authorDtoToString(AuthorDto author) {
        return "Id: %s, FullName: %s".formatted(author.id(), author.fullName());
    }

    public AuthorDto toDto(Author author) {
        if (author != null) {
            return new AuthorDto(author.getId(), author.getFullName());
        } else {
            return null;
        }
    }
}
