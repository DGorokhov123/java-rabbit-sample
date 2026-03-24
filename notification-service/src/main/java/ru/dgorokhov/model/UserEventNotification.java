package ru.dgorokhov.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO чтобы сервисный слой отвязать от protobuf
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventNotification {

    private Long id;
    private String email;
    private String name;
    private UserEventType type;
    private Instant timestamp;

}
