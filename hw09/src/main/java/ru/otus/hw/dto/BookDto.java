package ru.otus.hw.dto;

public record BookDto(String id, String title, AuthorDto author, GenreDto genre) {

    public BookDto{
        id = (id != null ) ? id : "";
        title = (title != null ) ? title : "";
        author = (author != null ) ? author : AuthorDto.empty();
        genre = (genre != null ) ? genre : GenreDto.empty();
    }

    public static BookDto empty(){
        return new BookDto( null, null, null, null);
    }
}
