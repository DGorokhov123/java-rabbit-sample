package ru.dgorokhov.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequestDto {

    @NotNull
    @NotEmpty
    @Size(max = 980)
    private String title;

    @NotNull
    @NotEmpty
    private String body;

    @NotNull
    @Email
    private String email;

}
