package ru.dgorokhov.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.dgorokhov.dto.UserCreateDto;
import ru.dgorokhov.dto.UserResponseDto;
import ru.dgorokhov.dto.UserUpdateDto;
import ru.dgorokhov.hateoas.UserResponseDtoAssembler;
import ru.dgorokhov.service.UserService;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RequestMapping("/v2/users")
@RestController
@Tag(name = "Пользователи /v2/users", description = "API v.2 с поддержкой HATEOAS для управления пользователями")
@Validated
@RequiredArgsConstructor
public class UserHateoasController {

    private final UserService userService;
    private final UserResponseDtoAssembler assembler;

    /*
    Все юзеры в виде листа dto
     */
    @GetMapping("/all")
    @Operation(description = "Найти всех пользователей. Возвращает PagedModel<EntityModel<UserResponseDto>> постранично с HATEOAS-ссылками")
    public PagedModel<EntityModel<UserResponseDto>> findAll(
            @Parameter(description = "Размер выдачи на 1 странице", required = false, example = "20")
            @RequestParam(name = "size", defaultValue = "20")
            @Positive(message = "Размер страницы должен быть положительным")
            Integer size,

            @Parameter(description = "Номер страницы", required = false, example = "0")
            @RequestParam(name = "page", defaultValue = "0")
            @PositiveOrZero(message = "Номер страницы должен быть неотрицательным")
            Integer page
    ) {
        return assembler.toPagedModel(userService.findAll(size, page));
    }

    /*
    Возвращает dto по id
     */
    @GetMapping("/{id}")
    @Operation(description = "Найти пользователя по ID. Возвращает EntityModel<UserResponseDto> с HATEOAS-ссылками")
    public EntityModel<UserResponseDto> findById(
            @PathVariable(name = "id") @Positive Long id
    ) {
        UserResponseDto dto = userService.findById(id);
        return assembler.toModel(dto);
    }

    /*
    Обновляет пользователя по dto. Обновляет только не-null поля.
    Возвращает измененное dto
     */
    @PutMapping
    @Operation(description = "Обновляет данные конкретного пользователя. Возвращает EntityModel<UserResponseDto> с HATEOAS-ссылками")
    public EntityModel<UserResponseDto> update(
            @RequestBody @Valid UserUpdateDto userUpdateDto
    ) {
        UserResponseDto dto = userService.update(userUpdateDto);
        return assembler.toModel(dto);
    }

    /*
    Удаляет пользователя по id. Возвращает результат с HATEOAS-ссылками
     */
    @DeleteMapping("/{id}")
    @Operation(description = "Удаляет пользователя по ID. Возвращает EntityModel<Map<String, String>> с результатом и HATEOAS-ссылками")
    public EntityModel<Map<String, String>> delete(
            @PathVariable(name = "id") @Positive Long id
    ) {
        boolean deleted = userService.delete(id);
        return EntityModel.of(
                Map.of("result", String.valueOf(deleted)),
                linkTo(methodOn(UserHateoasController.class).findAll(20, 0))
                        .withRel("users")
                        .withTitle("Список пользователей, ссылка на 1 страницу")
        );
    }

    /*
    Добавляет пользователя по dto и возвращает добавленного
     */
    @PostMapping
    @Operation(description = "Создает нового пользователя. Возвращает EntityModel<UserResponseDto> с HATEOAS-ссылками")
    public EntityModel<UserResponseDto> create(
            @RequestBody @Valid UserCreateDto userCreateDto
    ) {
        UserResponseDto dto = userService.create(userCreateDto);
        return assembler.toModel(dto);
    }

    /*
    Проверяет пользователя по id. Возвращает результат с HATEOAS-ссылками
     */
    @GetMapping("/check/{id}")
    @Operation(description = "Проверяет существование пользователя по ID. Возвращает EntityModel<Map<String, String>> с результатом и HATEOAS-ссылками")
    public EntityModel<Map<String, String>> existsById(
            @PathVariable(name = "id") @Positive Long id
    ) {
        boolean exists = userService.existsById(id);
        return EntityModel.of(
                Map.of("result", String.valueOf(exists)),
                linkTo(methodOn(UserHateoasController.class).existsById(id))
                        .withSelfRel()
                        .withTitle("Проверка существования пользователя"),
                linkTo(methodOn(UserHateoasController.class).findById(id))
                        .withRel("user")
                        .withTitle("Ссылка на данного пользователя")
        );
    }

    /*
    Возвращает пользователя по email
     */
    @GetMapping("/email/{email}")
    @Operation(description = "Найти пользователя по Email. Возвращает EntityModel<UserResponseDto> с HATEOAS-ссылками")
    public EntityModel<UserResponseDto> findByEmail(
            @PathVariable(name = "email") @NotNull @NotBlank @Email String email
    ) {
        UserResponseDto dto = userService.findByEmail(email);
        return assembler.toModel(dto);
    }

}
