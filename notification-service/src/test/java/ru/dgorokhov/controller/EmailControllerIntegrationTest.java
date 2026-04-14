package ru.dgorokhov.controller;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.dgorokhov.dto.notification.SendEmailRequestDto;
import ru.dgorokhov.dto.notification.SendEmailResponseDto;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Слишком тяжелый тест для автоматического запуска")
@Testcontainers
@SpringBootTest(properties = {
        "app.email.provider=javamail",
        "spring.mail.host=localhost",
        "spring.mail.username=",
        "spring.mail.password=",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false"
})
@AutoConfigureMockMvc
class EmailControllerIntegrationTest {

    @Container
    private final static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:v1.29.4")
            .withExposedPorts(1025, 8025)
            .waitingFor(Wait.forHttp("/").forPort(8025));

    private final Faker faker = new Faker();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
    }

    @Nested
    @DisplayName("POST /email - Проверка валидации")
    class SendEmailTests {

        @Test
        @DisplayName("Корректный email - статус ACCEPTED")
        void testSendEmail_Success() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(5))
                    .body(faker.lorem().paragraph(3))
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid", notNullValue()))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.uri", notNullValue()));
        }

        @Test
        @DisplayName("Валидация поля title (null)")
        void testSendEmail_NullTitle() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(null)
                    .body(faker.lorem().paragraph(2))
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля title (пустая строка)")
        void testSendEmail_EmptyTitle() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title("")
                    .body(faker.lorem().paragraph(2))
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля title (слишком длинный)")
        void testSendEmail_TooLongTitle() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().characters(1000))
                    .body(faker.lorem().paragraph(2))
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля body (null)")
        void testSendEmail_NullBody() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(3))
                    .body(null)
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля body (пустая строка)")
        void testSendEmail_EmptyBody() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(3))
                    .body("")
                    .email(faker.internet().emailAddress())
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля email (null)")
        void testSendEmail_NullEmail() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(3))
                    .body(faker.lorem().paragraph(2))
                    .email(null)
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Валидация поля email (некорректный формат)")
        void testSendEmail_InvalidEmail() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(3))
                    .body(faker.lorem().paragraph(2))
                    .email("not-a-valid-email")
                    .build();

            mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }
    }

    @Nested
    @DisplayName("Проверка отправки и получения")
    class GetStatusTests {

        @Test
        @DisplayName("Письмо отправлено корректно (SENT)")
        void testGetStatus_EmailSent() throws Exception {
            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(faker.lorem().sentence(5))
                    .body(faker.lorem().paragraph(3))
                    .email(faker.internet().emailAddress())
                    .build();

            String response = mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            SendEmailResponseDto responseDto = jsonMapper.readValue(response, SendEmailResponseDto.class);
            UUID uuid = responseDto.getUuid();

            // проверяем статус сразу до завершения асинхронной отправки
            mockMvc.perform(get("/email/" + uuid + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            // Ждем асинхронной отправки
            await()
                    .atMost(30, TimeUnit.SECONDS)
                    .pollInterval(500, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        mockMvc.perform(get("/email/" + uuid + "/status"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                                .andExpect(jsonPath("$.status").value("SENT"));
                    });
        }

        @Test
        @DisplayName("Неизвестный UUID (UNKNOWN)")
        void testGetStatus_UnknownUuid() throws Exception {
            UUID unknownUuid = UUID.randomUUID();

            mockMvc.perform(get("/email/" + unknownUuid + "/status"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(unknownUuid.toString()))
                    .andExpect(jsonPath("$.status").value("UNKNOWN"));
        }

        @Test
        @DisplayName("Полный цикл: создание, отправка, получение")
        void testEndToEnd_EmailReceivedInMailpit() throws Exception {
            String email = faker.internet().emailAddress();
            String title = "Test Subject: " + faker.lorem().word();
            String body = faker.lorem().paragraph(3);

            SendEmailRequestDto requestDto = SendEmailRequestDto.builder()
                    .title(title)
                    .body(body)
                    .email(email)
                    .build();

            // Отправляем письмо
            String response = mockMvc.perform(post("/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            SendEmailResponseDto responseDto = jsonMapper.readValue(response, SendEmailResponseDto.class);
            UUID uuid = responseDto.getUuid();

            // Ждем отправки
            await()
                    .atMost(30, TimeUnit.SECONDS)
                    .pollInterval(500, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        mockMvc.perform(get("/email/" + uuid + "/status"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SENT"));
                    });

            // Проверяем что письмо получено в Mailpit через API
            String mailpitUrl = "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages";

            await()
                    .atMost(100, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .ignoreException(IOException.class)
                    .untilAsserted(() -> {
                        HttpClient client = HttpClient.newBuilder().build();
                        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(mailpitUrl)).GET().build();
                        HttpResponse<String> mailpitResponse = client.send(request, BodyHandlers.ofString());

                        assertEquals(200, mailpitResponse.statusCode());
                        Map<String, Object> messagesResponse = jsonMapper.readValue(mailpitResponse.body(), Map.class);
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) messagesResponse.get("messages");

                        assertNotNull(messages);
                        assertFalse(messages.isEmpty());

                        // Проверяем что есть письмо на наш email
                        boolean found = messages.stream()
                                .anyMatch(msg -> {
                                    Object toObj = msg.get("To");
                                    if (toObj instanceof List) {
                                        List<?> toList = (List<?>) toObj;
                                        return toList.stream().anyMatch(to -> to.toString().contains(email));
                                    }
                                    return false;
                                });

                        assertTrue(found, "Письмо на email " + email + " не найдено в Mailpit");
                    });
        }
    }
}
