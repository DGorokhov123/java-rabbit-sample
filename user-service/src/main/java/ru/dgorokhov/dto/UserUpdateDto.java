package ru.dgorokhov.dto;

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
public class UserUpdateDto {

    @NotNull(message = "UserUpdateDto should have ID ")
    @Positive(message = "ID should be positive number")
    private Long id;

    @Size(max = 255, message = "Name length should be from 1 to 255 symbols")
    private String name;

    @Size(max = 254, message = "Email length should be from 3 to 254 symbols")
    @Email(message = "Field 'email' should match email mask")
    private String email;

    @Positive
    @Max(value = 120, message = "Unfortunately, age can be less then 120 years only")
    private Integer age;

}
