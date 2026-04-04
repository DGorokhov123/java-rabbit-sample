package ru.dgorokhov.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Setter
@Builder
@Schema(description = "Универсальное сообщение об ошибке")

public class ApiError extends RepresentationModel<ApiError> {

    @Schema(
            description = "Дата и время ошибки",
            example = "2026-04-01T10:46:57.091309400Z"
    )
    Instant timestamp;

    @Schema(
            description = "Код HTTP ошибки",
            example = "400 BAD_REQUEST"
    )
    HttpStatus status;

    @Schema(
            description = "Описание ошибки",
            example = "Validation Failed"
    )
    String error;

    @Schema(
            description = "Подробное сообщение об ошибке",
            example = "Field 'email' should match email mask"
    )
    String message;

    @Schema(
            description = "Ресурс, где возникла ошибка",
            example = "/v2/users"
    )
    String path;

}
