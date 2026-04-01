package ru.dgorokhov.hateoas;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.dgorokhov.controller.v2.UserHateoasController;
import ru.dgorokhov.dto.UserResponseDto;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
@RequiredArgsConstructor
public class UserResponseDtoAssembler implements RepresentationModelAssembler<UserResponseDto, EntityModel<UserResponseDto>> {

    @Override
    public EntityModel<UserResponseDto> toModel(UserResponseDto dto) {
        EntityModel<UserResponseDto> model = toModelWithBasicLinks(dto);

        model.add(
                linkTo(methodOn(UserHateoasController.class).findAll(20, 0))
                        .withRel("users")
                        .withTitle("Список пользователей, ссылка на 1 страницу")
        );

        model.add(
                linkTo(methodOn(UserHateoasController.class).delete(dto.getId()))
                        .withRel("delete")
                        .withType("DELETE")
                        .withTitle("Удалить пользователя")
        );

        model.add(
                linkTo(methodOn(UserHateoasController.class).update(null))
                        .withRel("update")
                        .withType("PUT")
                        .withTitle("Изменить данные пользователя")
                        .andAffordance(
                                afford(methodOn(UserHateoasController.class).update(null))
                        )
        );

        model.add(
                linkTo(methodOn(UserHateoasController.class).create(null))
                        .withRel("create")
                        .withType("POST")
                        .withTitle("Создать нового пользователя")
                        .andAffordance(
                                afford(methodOn(UserHateoasController.class).create(null))
                        )
        );

        return model;
    }

    private EntityModel<UserResponseDto> toModelWithBasicLinks(UserResponseDto dto) {
        EntityModel<UserResponseDto> model = EntityModel.of(dto);

        model.add(
                linkTo(methodOn(UserHateoasController.class).findById(dto.getId()))
                        .withSelfRel()
                        .withTitle("Ссылка на данного пользователя")
        );

        model.add(
                linkTo(methodOn(UserHateoasController.class).existsById(dto.getId()))
                        .withRel("check-existence")
                        .withTitle("Проверка существования пользователя")
        );

        return model;
    }

    public PagedModel<EntityModel<UserResponseDto>> toPagedModel(
            List<UserResponseDto> dtos,
            Integer size,
            Integer page
    ) {
        List<EntityModel<UserResponseDto>> models = dtos.stream()
                .map(this::toModelWithBasicLinks)
                .toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, dtos.size());
        PagedModel<EntityModel<UserResponseDto>> pagedModel = PagedModel.of(models, metadata);

        pagedModel.add(
                linkTo(methodOn(UserHateoasController.class).findAll(size, page))
                        .withSelfRel()
                        .withTitle("Ссылка на данную выборку")
        );

        pagedModel.add(
                linkTo(methodOn(UserHateoasController.class).findAll(size, 0))
                        .withRel("first")
                        .withTitle("Ссылка первую страницу")
        );

        if (page > 0) pagedModel.add(
                linkTo(methodOn(UserHateoasController.class).findAll(size, page - 1))
                        .withRel("prev")
                        .withTitle("Ссылка на предыдущую страницу")
        );

        if (dtos.size() == size) pagedModel.add(
                linkTo(methodOn(UserHateoasController.class).findAll(size, page + 1))
                        .withRel("next")
                        .withTitle("Ссылка на следующую страницу")
        );

        return pagedModel;
    }

}
