package ru.otus.hw.exceptions;

public class EntityAlreadyExistsException extends CookbookException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}