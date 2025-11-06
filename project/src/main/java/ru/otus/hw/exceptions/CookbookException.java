package ru.otus.hw.exceptions;

public class CookbookException extends RuntimeException {
    public CookbookException(String message) {
        super(message);
    }

    public CookbookException(String message, Throwable cause) {
        super(message, cause);
    }
}
