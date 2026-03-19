package ru.dgorokhov.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Field 'name' shouldn't be blank")
    @Size(min = 1, max = 255, message = "Name length should be from 1 to 255 symbols")
    private String name;

    @NotBlank(message = "Field 'email' shouldn't be blank")
    @Size(min = 3, max = 254, message = "Email length should be from 3 to 254 symbols")
    @Email(message = "Field 'email' should match email mask")
    private String email;

    @Positive
    @Max(value = 120, message = "Unfortunately, age can be less then 120 years only")
    private Integer age;

}
