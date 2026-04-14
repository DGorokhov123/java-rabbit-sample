package ru.dgorokhov.tester;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.dgorokhov.config.AppProperties;
import ru.dgorokhov.dto.user.UserCreateDto;
import ru.dgorokhov.dto.user.UserResponseDto;
import ru.dgorokhov.proto.user.UserNotificationProto;
import ru.dgorokhov.rabbit.RabbitMessageListener;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TesterService {

    private final DiscoveryClient discoveryClient;
    private final ApplicationContext context;
    private final AppProperties appProperties;
    private final RabbitMessageListener rabbitMessageListener;
    private final Faker faker = new Faker();

    private final JsonMapper jsonMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void testStarter() throws InterruptedException {

        System.out.print("Ждем запуска CONFIG-SERVICE ... ");
        int counter = 0;
        while (true) {
            List<ServiceInstance> instances = discoveryClient.getInstances("config-service");
            if (!instances.isEmpty()) {
                System.out.println("OK");
                break;
            }
            Thread.sleep(600);
            if (counter++ > 100) exitWithMessage("ОШИБКА: CONFIG-SERVICE недоступен");
        }

        System.out.print("Ждем запуска GATEWAY-SERVICE ... ");
        counter = 0;
        String gatewayUrl;
        while (true) {
            List<ServiceInstance> instances = discoveryClient.getInstances("gateway-service");
            if (!instances.isEmpty()) {
                gatewayUrl = instances.get(0).getUri().toString();
                System.out.println("OK, получен URL " + gatewayUrl);
                break;
            }
            Thread.sleep(600);
            if (counter++ > 100) exitWithMessage("ОШИБКА: GATEWAY-SERVICE недоступен");
        }

        System.out.print("Ждем запуска USER-SERVICE ... ");
        counter = 0;
        while (true) {
            List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
            if (!instances.isEmpty()) {
                System.out.println("OK");
                break;
            }
            Thread.sleep(600);
            if (counter++ > 100) exitWithMessage("ОШИБКА: USER-SERVICE недоступен");
        }

        System.out.print("Ждем запуска NOTIFICATION-SERVICE ... ");
        counter = 0;
        while (true) {
            List<ServiceInstance> instances = discoveryClient.getInstances("notification-service");
            if (!instances.isEmpty()) {
                System.out.println("OK");
                break;
            }
            Thread.sleep(600);
            if (counter++ > 100) exitWithMessage("ОШИБКА: NOTIFICATION-SERVICE недоступен");
        }

        System.out.print("\nUSER-SERVICE: отправляем запрос на создание юзера ... ");

        RestClient restClient = RestClient.builder()
                .baseUrl(gatewayUrl)
                .defaultHeader("Accept", "application/json")
                .build();

        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .age(faker.number().numberBetween(18, 90))
                .build();

        ResponseEntity<UserResponseDto> responseEntity = restClient.post()
                .uri("/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(userCreateDto)
                .retrieve()
                .toEntity(UserResponseDto.class);

        UserResponseDto userResponseDto = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && userResponseDto != null) {
            System.out.println("УСПЕШНО, получен " + userResponseDto);
        } else {
            exitWithMessage("ОШИБКА: Запрос на создание юзера завершился кодом " + responseEntity.getStatusCode().toString());
        }

        Thread.sleep(1000);

        System.out.print("\nRABBITMQ: проверяем сообщение в кролике ... ");
        UserNotificationProto proto = rabbitMessageListener.getProtoByUserId(userResponseDto.getId());
        if (proto != null) {
            System.out.println("УСПЕШНО, получен " + proto);
        } else {
            exitWithMessage("ОШИБКА: в кролике ничего нет " + responseEntity.getStatusCode().toString());
        }

        System.out.print("MAILPIT: проверяем сообщение в почте ... ");

        RestClient mailPitClient = RestClient.builder()
                .baseUrl(appProperties.getMailUrl())
                .defaultHeader("Accept", "application/json")
                .build();

        counter = 0;
        while (true) {
            ResponseEntity<String> mailPitResponse = mailPitClient.get()
                    .uri("/api/v1/messages")
                    .retrieve()
                    .toEntity(String.class);

            Map<String, Object> messagesResponse = jsonMapper.readValue(mailPitResponse.getBody(), Map.class);
            List<Map<String, Object>> messages = (List<Map<String, Object>>) messagesResponse.get("messages");

            boolean found = messages.stream()
                    .anyMatch(msg -> {
                        Object toObj = msg.get("To");
                        if (toObj instanceof List) {
                            List<?> toList = (List<?>) toObj;
                            return toList.stream().anyMatch(to -> to.toString().contains(userCreateDto.getEmail()));
                        }
                        return false;
                    });

            if (found) {
                System.out.println("УСПЕШНО, получено письмо на " + userCreateDto.getEmail());
                break;
            }
            Thread.sleep(300);
            if (counter++ > 100) exitWithMessage("ОШИБКА: письмо на " + userCreateDto.getEmail() + " так и не получено!");
        }

        exitWithMessage("\nВсе тесты прошли успешно");
    }

    private void exitWithMessage(String message) {
        System.out.println(message);
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

}
