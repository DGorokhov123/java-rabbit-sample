package ru.dgorokhov.rabbit;

import lombok.*;
import ru.dgorokhov.proto.user.UserNotificationProto;

/**
 * обертка чтобы отвязать сервисный слой от protobuf
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationEvent {

    private UserNotificationProto userNotificationProto;

}
