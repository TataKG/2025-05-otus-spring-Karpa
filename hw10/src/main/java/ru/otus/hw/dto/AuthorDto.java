package ru.otus.hw.dto;

public record AuthorDto(String id, String fullName) {
    public AuthorDto {
        id = (id != null) ? id : "";
        fullName = (fullName != null) ? fullName : "";
    }

    public static AuthorDto empty() {
        return new AuthorDto(null, null);
    }
}