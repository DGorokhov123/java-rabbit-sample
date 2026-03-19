package ru.dgorokhov.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        log.debug("Not found: {}", e.getMessage());
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,               // @Valid annotation exceptions
            ConstraintViolationException.class                   // Other annotation exceptions
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(Exception e, HttpServletRequest request) {
        String errorMessage;
        if (e instanceof MethodArgumentNotValidException ex) {
            errorMessage = ex.getBindingResult()
                    .getAllErrors()
                    .get(0)
                    .getDefaultMessage() + " for " + ex.getBindingResult().getTarget().getClass().getSimpleName();
        } else if (e instanceof ConstraintViolationException ex) {
            errorMessage = ex.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("Invalid input");
        } else {
            errorMessage = e.getMessage();
        }

        log.debug("VALIDATION FAILED: {}", errorMessage);
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Validation Failed")
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class          // violates data constraints
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalArgument(DataIntegrityViolationException e, HttpServletRequest request) {
        log.debug("DATA CONFLICT: {}", e.getMessage());
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT)
                .error("Data constraints violated")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class,                        // wrong arguments like -1
            MethodArgumentTypeMismatchException.class,             // argument type mismatch
            HttpMessageNotReadableException.class,                 // wrong json in request body
            MissingServletRequestParameterException.class          // missing RequestParam
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(Exception e, HttpServletRequest request) {
        log.debug("ILLEGAL ARGUMENT: {}", e.getMessage());
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Illegal Argument")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(CustomValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCustomValidationException(CustomValidationException e, HttpServletRequest request) {
        log.debug("Invalid request has passed controller validation, fix it!: {}", e.getMessage());
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Invalid request has passed controller validation, fix it!")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

}
