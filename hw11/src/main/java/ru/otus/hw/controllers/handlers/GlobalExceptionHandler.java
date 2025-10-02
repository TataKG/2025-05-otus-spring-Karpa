package ru.otus.hw.controllers.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Rendering handleAllExceptions(Exception ex, ServerWebExchange exchange) {

        return Rendering.view("customError")
                .modelAttribute("errorText", "Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}


