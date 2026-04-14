package ru.dgorokhov.controller;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.dgorokhov.controller.v2.UserHateoasController;
import ru.dgorokhov.dal.User;
import ru.dgorokhov.dal.UserRepository;
import ru.dgorokhov.dto.user.UserCreateDto;
import ru.dgorokhov.dto.user.UserUpdateDto;
import ru.dgorokhov.hateoas.UserResponseDtoAssembler;
import ru.dgorokhov.rabbit.RabbitMessageSender;
import ru.dgorokhov.service.UserService;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserHateoasController.class)
@Import({UserService.class, UserResponseDtoAssembler.class})
class UserHateoasControllerTest {

    private final User testUser = User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .age(30)
            .createdAt(Instant.now())
            .version(0L)
            .build();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Faker faker = new Faker();
    User testUser2 = User.builder()
            .id(2L)
            .name("Jane Doe")
            .email("jane@example.com")
            .age(25)
            .createdAt(Instant.now())
            .version(1L)
            .build();
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RabbitMessageSender rabbitMessageSender;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    // ==================== ПОЗИТИВНЫЕ СЦЕНАРИИ ====================

    @Test
    @DisplayName("Найти всех пользователей - создано 2 штуки")
    void testFindAll_2Users() throws Exception {
        when(userRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(testUser, testUser2)));

        mockMvc.perform(get("/v2/users/all").accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseDtoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.userResponseDtoList[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$._embedded.userResponseDtoList[0].name").value(testUser.getName()))
                .andExpect(jsonPath("$._embedded.userResponseDtoList[0].email").value(testUser.getEmail()));

        verify(userRepository).findAll(PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("Найти всех пользователей - пустой лист")
    void testFindAll_EmptyList() throws Exception {
        when(userRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v2/users/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());

        verify(userRepository).findAll(PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("Найти пользователя по ID - пользователь найден")
    void testFindById_UserExists() throws Exception {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/v2/users/" + testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.age").value(testUser.getAge()));

        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Найти пользователя по ID - пользователь не найден")
    void testFindById_NotFound() throws Exception {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/v2/users/1"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Найти пользователя по Email - пользователь найден")
    void testFindByEmail_UserExists() throws Exception {
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/v2/users/email/" + testUser.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.age").value(testUser.getAge()));

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("Найти пользователя по Email - пользователь не найден")
    void testFindByEmail_NotFound() throws Exception {
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/v2/users/email/" + testUser.getEmail()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("Создание пользователя с корректным DTO")
    void testCreate_ValidDto() throws Exception {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(testUser.getName())
                .email(testUser.getEmail())
                .age(testUser.getAge())
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(userCreateDto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.age").value(testUser.getAge()));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с DTO = null")
    void testCreate_NullDto() throws Exception {

        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(null))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя с корректным DTO")
    void testUpdate_ValidDto() throws Exception {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        User testUser3 = User.builder()
                .id(testUser.getId())
                .name(testUser2.getName())
                .email(testUser2.getEmail())
                .age(testUser2.getAge())
                .createdAt(testUser.getCreatedAt())
                .version(testUser.getVersion() + 1L)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser3);

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .id(testUser.getId())
                .name(testUser2.getName())
                .email(testUser2.getEmail())
                .age(testUser2.getAge())
                .build();

        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(userUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser3.getId()))
                .andExpect(jsonPath("$.name").value(testUser3.getName()))
                .andExpect(jsonPath("$.email").value(testUser3.getEmail()))
                .andExpect(jsonPath("$.age").value(testUser3.getAge()));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с DTO = null")
    void testUpdate_NullDto() throws Exception {
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(null))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление существующего пользователя")
    void testDelete_UserExists() throws Exception {
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(delete("/v2/users/" + testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).deleteById(testUser.getId());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void testDelete_NotFound() throws Exception {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/v2/users/1"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("Проверка существующего пользователя")
    void testExistsById_UserExists() throws Exception {
        when(userRepository.existsById(testUser.getId()))
                .thenReturn(true);

        mockMvc.perform(get("/v2/users/check/" + testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("true"));

        verify(userRepository).existsById(testUser.getId());
    }

    @Test
    @DisplayName("Проверка несуществующего пользователя")
    void testExistsById_NotFound() throws Exception {
        when(userRepository.existsById(testUser.getId()))
                .thenReturn(false);

        mockMvc.perform(get("/v2/users/check/" + testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("false"));

        verify(userRepository).existsById(testUser.getId());
    }

    @Test
    @DisplayName("Создание пользователя - валидация поля Name")
    void testCreateValidation_Name() throws Exception {
        // Blank
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name("")
                                                .email(testUser.getEmail())
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // null
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(null)
                                                .email(testUser.getEmail())
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // too long
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name("a".repeat(256))
                                                .email(testUser.getEmail())
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание пользователя - валидация поля Email")
    void testCreateValidation_Email() throws Exception {
        // blank
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email("")
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // null
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email(null)
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // invalid
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email("not-an-email")
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // missing @
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email("testexample.com")
                                                .age(testUser.getAge())
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание пользователя - валидация поля Age")
    void testCreateValidation_Age() throws Exception {
        // negative
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email(testUser.getEmail())
                                                .age(-5)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // zero
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email(testUser.getEmail())
                                                .age(0)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // too large
        mockMvc.perform(
                        post("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserCreateDto.builder()
                                                .name(testUser.getName())
                                                .email(testUser.getEmail())
                                                .age(121)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя - валидация поля Id")
    void testUpdateValidation_ID() throws Exception {
        // null
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .name("New Name")
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // negative
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(-1L)
                                                .name("New Name")
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя - валидация поля Name")
    void testUpdateValidation_Name() throws Exception {
        // too long
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .name("a".repeat(256))
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя - валидация поля Email")
    void testUpdateValidation_Email() throws Exception {
        // invalid
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .email("not-an-email")
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // missing @
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .email("testexample.com")
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя - валидация поля Age")
    void testUpdateValidation_Age() throws Exception {
        // negative
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .age(-5)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // zero
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .age(0)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        // too old
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .age(121)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление пользователя - валидации аннотации @AtLeastOneNotNull")
    void testUpdate_DtoWithNullFields() throws Exception {
        mockMvc.perform(
                        put("/v2/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        UserUpdateDto.builder()
                                                .id(testUser.getId())
                                                .name(null)
                                                .email(null)
                                                .age(null)
                                                .build()
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userRepository, never()).save(any());
    }

}
