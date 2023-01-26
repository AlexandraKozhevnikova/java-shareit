package ru.practicum.shareit.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ErrorResponse>> handleConstraintViolationException(
            final ConstraintViolationException e) {
        List<String> listError = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        listError.stream()
                                .map(it -> ErrorResponse.builder()
                                        .message(it)
                                        .error(ApiError.builder()
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
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        listError.stream()
                                .map(it -> ErrorResponse.builder()
                                        .message(it)
                                        .error(ApiError.builder()
                                                .type("validation")
                                                .description("value is not valid")
                                                .build()
                                        ).build()
                                )
                                .collect(Collectors.toList())
                );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message(exception.getLocalizedMessage())
                        .error(ApiError.builder()
                                .type("validation")
                                .description("required header is missing")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message(exception.getLocalizedMessage())
                        .error(ApiError.builder()
                                .type("validation")
                                .description("request parameter is missing")
                                .build()
                        ).build()
                );
    }

    //    @ExceptionHandler(DuplicateKeyException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException exception) {
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .body(ErrorResponse.builder()
//                        .message(exception.getLocalizedMessage())
//                        .error(ApiError.builder()
//                                .type("logic")
//                                .description("object already exist")
//                                .build()
//                        ).build()
//                );
//    }
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .message(exception.getLocalizedMessage())
                        .error(ApiError.builder()
                                .type("logic")
                                .description("object already exist")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .message(exception.getLocalizedMessage())
                        .error(ApiError.builder()
                                .type("logic")
                                .description("object does not found")
                                .build()
                        ).build()
                );
    }
}
