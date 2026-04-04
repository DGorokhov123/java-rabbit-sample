package ru.dgorokhov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для создания нового пользователя")
public class UserCreateDto {

    @Schema(
            description = "Имя пользователя",
            example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Field 'name' shouldn't be blank")
    @Size(min = 1, max = 255, message = "Name length should be from 1 to 255 symbols")
    private String name;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Field 'email' shouldn't be blank")
    @Size(min = 3, max = 254, message = "Email length should be from 3 to 254 symbols")
    @Email(message = "Field 'email' should match email mask")
    private String email;

    @Schema(
            description = "Возраст пользователя",
            example = "30",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive
    @Max(value = 120, message = "Unfortunately, age can be less then 120 years only")
    private Integer age;

}
