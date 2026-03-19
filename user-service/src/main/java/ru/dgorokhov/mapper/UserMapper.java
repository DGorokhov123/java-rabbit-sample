package ru.dgorokhov.mapper;

import ru.dgorokhov.dal.User;
import ru.dgorokhov.dto.UserCreateDto;
import ru.dgorokhov.dto.UserResponseDto;
import ru.dgorokhov.dto.UserUpdateDto;

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

}
