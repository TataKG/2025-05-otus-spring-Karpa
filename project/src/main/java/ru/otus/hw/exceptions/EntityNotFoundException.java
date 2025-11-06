package ru.otus.hw.exceptions;

public class EntityNotFoundException extends CookbookException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
