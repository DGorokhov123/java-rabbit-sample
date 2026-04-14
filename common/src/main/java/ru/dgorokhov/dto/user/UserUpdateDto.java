package ru.dgorokhov.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.dgorokhov.validation.AtLeastOneNotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOneNotNull(fields = {"name", "email", "age"})
@Schema(description = "DTO для обновления данных пользователя")
public class UserUpdateDto {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "UserUpdateDto should have ID ")
    @Positive(message = "ID should be positive number")
    private Long id;

    @Schema(
            description = "Новое имя пользователя",
            example = "Иван Петров",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 255, message = "Name length should be from 1 to 255 symbols")
    private String name;

    @Schema(
            description = "Новый email пользователя",
            example = "ivan.petrov@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 254, message = "Email length should be from 3 to 254 symbols")
    @Email(message = "Field 'email' should match email mask")
    private String email;

    @Schema(
            description = "Новый возраст пользователя",
            example = "31",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive
    @Max(value = 120, message = "Unfortunately, age can be less then 120 years only")
    private Integer age;

}
