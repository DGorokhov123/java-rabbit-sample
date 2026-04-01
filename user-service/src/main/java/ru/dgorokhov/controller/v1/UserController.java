package ru.dgorokhov.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/v1/users")
@RestController
@Tag(name = "Пользователи /v1/users", description = "API v.1 для управления пользователями")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*
    Все юзеры в виде листа dto
     */
    @GetMapping("/all")
    @Operation(description = "Найти всех пользователей. Возвращает List<UserResponseDto> постранично")
    public List<UserResponseDto> findAll(
            @Parameter(description = "Размер выдачи на 1 странице", required = false, example = "20")
            @RequestParam(name = "size", defaultValue = "20")
            @Positive(message = "Размер страницы должен быть положительным")
            Integer size,

            @Parameter(description = "Номер страницы", required = false, example = "0")
            @RequestParam(name = "page", defaultValue = "0")
            @PositiveOrZero(message = "Номер страницы должен быть неотрицательным")
            Integer page
    ) {
        return userService.findAll(size, page).getContent();
    }

    /*
    Возвращает dto по id
     */
    @GetMapping("/{id}")
    @Operation(description = "Найти пользователя по ID. Возвращает UserResponseDto для найденного пользователя")
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
    @Operation(description = "Обновляет данные конкретного пользователя. Возвращает UserResponseDto с обновленными данными пользователя")
    public UserResponseDto update(
            @RequestBody @Valid UserUpdateDto userUpdateDto
    ) {
        return userService.update(userUpdateDto);
    }

    /*
    Удаляет пользователя по id. Возвращает true при успешном удалении
     */
    @DeleteMapping("/{id}")
    @Operation(description = "Удаляет пользователя по ID. Возвращает true если пользователь успешно удален")
    public boolean delete(
            @PathVariable(name = "id") @Positive Long id
    ) {
        return userService.delete(id);
    }

    /*
    Добавляет пользователя по dto и возвращает добавленного
     */
    @PostMapping
    @Operation(description = "Создает нового пользователя. Возвращает UserResponseDto с данными созданного пользователя")
    public UserResponseDto create(
            @RequestBody @Valid UserCreateDto userCreateDto
    ) {
        return userService.create(userCreateDto);
    }

    /*
    Проверяет пользователя по id. Возвращает true если существует, либо false
     */
    @GetMapping("/check/{id}")
    @Operation(description = "Проверяет существование пользователя по ID. Возвращает true если пользователь существует, иначе false")
    public boolean existsById(
            @PathVariable(name = "id") @Positive Long id
    ) {
        return userService.existsById(id);
    }

    /*
    Возвращает пользователя по email
     */
    @GetMapping("/email/{email}")
    @Operation(description = "Найти пользователя по Email. Возвращает UserResponseDto для найденного пользователя")
    public UserResponseDto findByEmail(
            @PathVariable(name = "email") @NotNull @NotBlank @Email String email
    ) {
        return userService.findByEmail(email);
    }

}
