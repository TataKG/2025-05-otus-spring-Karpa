package ru.otus.hw.dto;

public record GenreDto(String id, String name) {
    public GenreDto{
        id = (id != null ) ? id : "";
        name = ( name != null ) ? name : "";
    }

    public static GenreDto empty (){
        return new GenreDto(null, null);
    }
}
