package ru.practicum.shareit.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionApiHandler {
    private static final Logger log = LogManager.getLogger(ExceptionApiHandler.class);


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorResponse>> handleConstraintViolationException(
            final ConstraintViolationException e) {
        log.error(e.getMessage(), e);
        List<String> listError = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        listError.stream()
                                .map(it -> ErrorResponse.builder()
                                        .error(it)
                                        .errorInfo(ApiError.builder()
                                                .type("validation")
                                                .description("value is not valid")
                                                .build()
                                        ).build()
                                )
                                .collect(Collectors.toList())
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationException(MethodArgumentNotValidException e) {
        List<String> listError = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        listError.stream()
                                .map(it -> ErrorResponse.builder()
                                        .error(it)
                                        .errorInfo(ApiError.builder()
                                                .type("validation")
                                                .description("value is not valid")
                                                .build()
                                        ).build()
                                )
                                .collect(Collectors.toList())
                );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("validation")
                                .description("required header is missing")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("validation")
                                .description("request parameter is missing")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("validation")
                                .description("object does not valid")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("common")
                                .description("no detailed exception")
                                .build()
                        ).build()
                );
    }
}
