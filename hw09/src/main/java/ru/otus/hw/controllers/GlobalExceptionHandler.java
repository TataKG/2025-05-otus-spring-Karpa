package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String VIEW_CUSTOM_ERROR = "customError";

    private final MessageSource messageSource;

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(EntityNotFoundException ex, WebRequest request) {

        log.warn("Entity not found: {} | URL: {}", ex.getMessage(), request.getDescription(false));

        String errorText = messageSource.getMessage("entity-not-found-error", null,
                LocaleContextHolder.getLocale());

        return new ModelAndView(VIEW_CUSTOM_ERROR, "errorText", errorText);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllExceptions(Exception ex, WebRequest request) {

        String requestInfo = String.format("URL: %s | Params: %s",
                request.getDescription(false),
                request.getParameterMap());

        log.error("""
                        === INTERNAL SERVER ERROR ===
                        Message: {}
                        Request: {}
                        StackTrace: {}
                        """,
                ex.getMessage(),
                requestInfo,
                ExceptionUtils.getStackTrace(ex));

        String errorText = messageSource.getMessage("error.internal.server", null,
                LocaleContextHolder.getLocale());

        var Locale = LocaleContextHolder.getLocale();

        ModelAndView model = new ModelAndView(VIEW_CUSTOM_ERROR);
        model.addObject("errorText", errorText);
        model.addObject("requestId", request.getAttribute("requestId", RequestAttributes.SCOPE_REQUEST));
        return model;
    }

}



