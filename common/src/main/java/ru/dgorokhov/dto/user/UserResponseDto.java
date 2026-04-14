package ru.dgorokhov.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Schema(description = "DTO для представления информации о пользователе")
public class UserResponseDto {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Имя пользователя",
            example = "Иван Иванов"
    )
    private String name;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com"
    )
    private String email;

    @Schema(
            description = "Возраст пользователя",
            example = "30"
    )
    private Integer age;

    @Schema(
            description = "Дата и время создания записи",
            example = "2024-01-15T10:30:00Z"
    )
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
