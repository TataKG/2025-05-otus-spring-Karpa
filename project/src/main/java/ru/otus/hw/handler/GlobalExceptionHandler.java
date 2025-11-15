package ru.otus.hw.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.utils.MessageProvider;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageProvider messageProvider;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        String errorMessage = messageProvider.getMessage("error.unexpected");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityAlreadyExistsException(EntityAlreadyExistsException ex) {
        log.warn("Entity already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurityException(SecurityException ex) {
        log.warn("Security violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(this::getValidationErrorMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        String errorMessage;
        if (errorMessages.size() > 1) {
            errorMessage = errorMessages.stream()
                    .map(msg -> "• " + msg)
                    .collect(Collectors.joining("\n"));
        } else if (errorMessages.size() == 1) {
            errorMessage = errorMessages.get(0);
        } else {
            errorMessage = messageProvider.getMessage("validation.error.default");
        }

        log.warn("Validation errors: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    private String getValidationErrorMessage(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();

        if (message != null && message.startsWith("{") && message.endsWith("}")) {
            String messageKey = message.substring(1, message.length() - 1);
            try {
                String resolvedMessage = messageProvider.getMessage(messageKey);

                if (fieldError.getArguments() != null && fieldError.getArguments().length > 1) {
                    Object[] args = Arrays.copyOfRange(fieldError.getArguments(), 1, fieldError.getArguments().length);
                    try {
                        return MessageFormat.format(resolvedMessage, args);
                    } catch (Exception e) {
                        log.warn("Failed to format message: {} with args: {}", resolvedMessage, Arrays.toString(args));
                    }
                }

                return resolvedMessage;
            } catch (Exception e) {
                log.warn("Failed to resolve message for key: {}", messageKey);
                return messageProvider.getMessage("validation.error.default");
            }
        }

        return message;
    }

    private String resolveMessageFromTemplate(String message) {
        if (message != null && message.startsWith("{") && message.endsWith("}")) {
            String messageKey = message.substring(1, message.length() - 1);
            try {
                return messageProvider.getMessage(messageKey);
            } catch (Exception e) {
                log.warn("Failed to resolve message for key: {}", messageKey);
                return messageProvider.getMessage("validation.error.default");
            }
        }
        return message;
    }
}