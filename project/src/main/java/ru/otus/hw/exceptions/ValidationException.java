package ru.otus.hw.exceptions;

public class ValidationException extends CookbookException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}