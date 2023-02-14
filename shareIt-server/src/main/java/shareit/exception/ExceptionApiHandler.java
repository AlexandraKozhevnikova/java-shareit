package shareit.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shareit.booking.ItemCanNotBeBookedByOwnerException;
import shareit.booking.ItemNotAvailableForBookingException;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionApiHandler {
    private static final Logger log = LogManager.getLogger(ExceptionApiHandler.class);

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

    @ExceptionHandler({SQLException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException e) {
        log.error(e.getMessage(), e);
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("logic")
                                .description("exception from db")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error(e.getLocalizedMessage())
                        .errorInfo(ApiError.builder()
                                .type("logic")
                                .description("object does not found")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(ItemNotAvailableForBookingException.class)
    public ResponseEntity<ErrorResponse> handleItemNotAvailableForBookingException(ItemNotAvailableForBookingException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(e.getMessage())
                        .errorInfo(ApiError.builder()
                                .type("logic")
                                .description("object does not available for booking")
                                .build()
                        ).build()
                );
    }

    @ExceptionHandler(ItemCanNotBeBookedByOwnerException.class)
    public ResponseEntity<ErrorResponse> handleItemCanNotBeBookedByOwnerException(ItemCanNotBeBookedByOwnerException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error(e.getMessage())
                        .errorInfo(ApiError.builder()
                                .type("logic")
                                .description("item can not be booked by owner")
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
