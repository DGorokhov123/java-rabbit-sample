package ru.dgorokhov.mapper;

import ru.dgorokhov.model.UserEventNotification;
import ru.dgorokhov.model.UserEventType;
import ru.dgorokhov.proto.user.UserActionTypeProto;
import ru.dgorokhov.proto.user.UserNotificationProto;

import java.time.Instant;

public class NotificationMapper {

    public static UserEventNotification fromProto(UserNotificationProto proto) {

        return UserEventNotification.builder()
                .id(proto.getUserId())
                .email(proto.getUserEmail())
                .name(proto.getUserName())
                .type(
                        fromProto(proto.getActionType())
                )
                .timestamp(
                        Instant.ofEpochSecond(
                                proto.getTimestamp().getSeconds(),
                                proto.getTimestamp().getNanos()
                        )
                )
                .build();

    }

    private static UserEventType fromProto(UserActionTypeProto type) {
        switch (type) {
            case USER_ACTION_CREATE:
                return UserEventType.USER_ACTION_CREATE;
            case USER_ACTION_UPDATE:
                return UserEventType.USER_ACTION_UPDATE;
            case USER_ACTION_DELETE:
                return UserEventType.USER_ACTION_DELETE;
            default:
                return UserEventType.UNRECOGNIZED;
        }
    }


}
