package ru.dgorokhov.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ApiError {

    Instant timestamp;

    HttpStatus status;

    String error;

    String message;

    String path;

}
