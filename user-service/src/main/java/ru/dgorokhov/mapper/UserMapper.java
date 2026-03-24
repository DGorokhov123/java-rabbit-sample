package ru.dgorokhov.mapper;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import ru.dgorokhov.dal.User;
import ru.dgorokhov.dto.UserCreateDto;
import ru.dgorokhov.dto.UserResponseDto;
import ru.dgorokhov.dto.UserUpdateDto;
import ru.dgorokhov.proto.user.UserActionTypeProto;
import ru.dgorokhov.proto.user.UserNotificationProto;
import ru.dgorokhov.rabbit.UserNotificationEvent;

import java.time.Instant;

@RequiredArgsConstructor
public class UserMapper {

    /*
    Создает сущность для добавления в БД
     */
    public static User fromDto(UserCreateDto dto) {
        if (dto == null) return null;
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .age(dto.getAge())
                .build();
    }

    /*
    Создает сущность-контейнер для обновления существующей сущности
     */
    public static User fromDto(UserUpdateDto dto) {
        if (dto == null) return null;
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .age(dto.getAge())
                .build();
    }

    /*
    Создает DTO
     */
    public static UserResponseDto toDto(User user) {
        if (user == null) return null;
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /*
    Создает событие USER_ACTION_CREATE для отправки уведомления
     */
    public static UserNotificationEvent toCreateNotificationEvent(User user) {
        return toNotificationEvent(user, UserActionTypeProto.USER_ACTION_CREATE);
    }

    /*
    Создает событие USER_ACTION_UPDATE для отправки уведомления
     */
    public static UserNotificationEvent toUpdateNotificationEvent(User user) {
        return toNotificationEvent(user, UserActionTypeProto.USER_ACTION_UPDATE);
    }

    /*
    Создает событие USER_ACTION_DELETE для отправки уведомления
     */
    public static UserNotificationEvent toDeleteNotificationEvent(User user) {
        return toNotificationEvent(user, UserActionTypeProto.USER_ACTION_DELETE);
    }

    /*
    Создает любое событие для отправки уведомления
     */
    public static UserNotificationEvent toNotificationEvent(User user, UserActionTypeProto action) {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano()).build();

        UserNotificationProto proto = UserNotificationProto.newBuilder()
                .setUserId(user.getId())
                .setUserEmail(user.getEmail())
                .setUserName(user.getName())
                .setActionType(action)
                .setTimestamp(timestamp)
                .build();

        return UserNotificationEvent.builder()
                .userNotificationProto(proto)
                .build();
    }

}
