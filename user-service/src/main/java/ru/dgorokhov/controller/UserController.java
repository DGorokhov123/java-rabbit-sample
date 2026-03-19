package ru.dgorokhov.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.dgorokhov.dto.UserCreateDto;
import ru.dgorokhov.dto.UserResponseDto;
import ru.dgorokhov.dto.UserUpdateDto;
import ru.dgorokhov.service.UserService;

import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*
    Все юзеры в виде листа dto
     */
    @GetMapping("/all")
    public List<UserResponseDto> findAll(
            @RequestParam(name = "size", defaultValue = "20") @Positive Integer size,
            @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero Integer page
    ) {
        return userService.findAll(size, page);
    }

    /*
    Возвращает dto по id
     */
    @GetMapping("/{id}")
    public UserResponseDto findById(
            @PathVariable(name = "id") @Positive Long id
    ) {
        return userService.findById(id);
    }

    /*
    Обновляет пользователя по dto. Обновляет только не-null поля.
    Возвращает измененное dto
     */
    @PutMapping
    public UserResponseDto update(
            @RequestBody @Valid UserUpdateDto userUpdateDto
    ) {
        return userService.update(userUpdateDto);
    }

    /*
    Удаляет пользователя по id. Возвращает true при успешном удалении
     */
    @DeleteMapping("/{id}")
    public boolean delete(
            @PathVariable(name = "id") @Positive Long id
    ) {
        return userService.delete(id);
    }

    /*
    Добавляет пользователя по dto и возвращает добавленного
     */
    @PostMapping
    public UserResponseDto create(
            @RequestBody @Valid UserCreateDto userCreateDto
    ) {
        return userService.create(userCreateDto);
    }

    /*
    Проверяет пользователя по id. Возвращает true если существует, либо false
     */
    @GetMapping("/check/{id}")
    public boolean existsById(
            @PathVariable(name = "id") @Positive Long id
    ) {
        return userService.existsById(id);
    }

    /*
    Возвращает пользователя по email
     */
    @GetMapping("/email/{email}")
    public UserResponseDto findByEmail(
            @PathVariable(name = "email") @NotNull @NotBlank @Email String email
    ) {
        return userService.findByEmail(email);
    }

}
