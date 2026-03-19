package ru.dgorokhov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dgorokhov.dal.User;
import ru.dgorokhov.dal.UserRepository;
import ru.dgorokhov.dto.UserCreateDto;
import ru.dgorokhov.dto.UserResponseDto;
import ru.dgorokhov.dto.UserUpdateDto;
import ru.dgorokhov.exception.CustomValidationException;
import ru.dgorokhov.exception.NotFoundException;
import ru.dgorokhov.mapper.UserMapper;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /*
    Все юзеры в виде листа dto
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll(Integer size, Integer page) {
        if (size == null || size < 1) throw new CustomValidationException("size should not be null or less then 1");
        if (page == null || page < 0) throw new CustomValidationException("size should not be null or negative");

        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        if (users.isEmpty()) return List.of();

        return users.stream()
                .map(UserMapper::toDto)
                .toList();
    }

    /*
    Возвращает dto по id
     */
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        if (id == null || id < 1) throw new CustomValidationException("id should not be null or negative or zero");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found User " + id));
        return UserMapper.toDto(user);
    }

    /*
    Обновляет пользователя по dto. Обновляет только не-null поля. Возвращает измененное dto
     */
    @Transactional
    public UserResponseDto update(UserUpdateDto userUpdateDto) {
        if (userUpdateDto == null) throw new CustomValidationException("dto should not be null");
        Long id = userUpdateDto.getId();
        if (id == null || id < 1) throw new CustomValidationException("dto.id should not be null or negative or zero");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found User " + id));

        if (userUpdateDto.getName() != null && !userUpdateDto.getName().isBlank()
                && !Objects.equals(userUpdateDto.getName(), user.getName())) {
            user.setName(userUpdateDto.getName());
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank()
                && !Objects.equals(userUpdateDto.getEmail(), user.getEmail())) {
            user.setEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.getAge() != null && !Objects.equals(userUpdateDto.getAge(), user.getAge())) {
            user.setAge(userUpdateDto.getAge());
        }

        return UserMapper.toDto(userRepository.save(user));
    }

    /*
    Удаляет пользователя по id. Возвращает true при успешном удалении
     */
    @Transactional
    public boolean delete(Long id) {
        if (id == null || id < 1) throw new CustomValidationException("id should not be null or negative or zero");

        if (!userRepository.existsById(id)) throw new NotFoundException("Not found User " + id);
        userRepository.deleteById(id);
        return true;
    }

    /*
    Добавляет пользователя по dto и возвращает добавленного
     */
    @Transactional
    public UserResponseDto create(UserCreateDto userCreateDto) {
        if (userCreateDto == null) throw new CustomValidationException("dto should not be null");

        User user = UserMapper.fromDto(userCreateDto);
        return UserMapper.toDto(userRepository.save(user));
    }

    /*
    Проверяет пользователя по id. Возвращает true если существует, либо false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null || id < 1) throw new CustomValidationException("id should not be null or negative or zero");

        return userRepository.existsById(id);
    }

    /*
    Возвращает пользователя по email
     */
    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        if (email == null || email.isBlank()) throw new CustomValidationException("email should not be null or blank");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Not found User with email = " + email));
        return UserMapper.toDto(user);
    }

}
