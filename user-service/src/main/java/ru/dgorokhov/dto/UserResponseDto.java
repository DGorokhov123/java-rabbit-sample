package ru.dgorokhov.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class UserResponseDto {

    private Long id;

    private String name;

    private String email;

    private Integer age;

    private Instant createdAt;

    @Override
    public String toString() {
        return "[" + id + "] Имя: " + name + ", Email: " + email + ", Возраст: " + age + ", Создан: " + createdAt;
    }

    public String toFormattedString() {
        return String.format(
                "[%5d ] %-30s | %-35s | Возраст: %-3d | Создан: %s",
                id, name, email, age, createdAt
        );
    }

}
