package ru.dgorokhov.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI
 */
@Configuration
public class OpenApiConfig {

    @Value("${info.app.name:User Service}")
    private String appName;

    @Value("${info.app.description:User Service API}")
    private String appDescription;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @Value("${info.company.name:Somekind Software}")
    private String companyName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API")
                        .description("""
                                %s
                                
                                Этот сервис предоставляет REST API для управления пользователями.
                                Поддерживаются операции CRUD, поиск по email и проверка существования пользователя.
                                
                                **Версии API:**
                                - `/v1/users` - базовая версия API
                                - `/v2/users` - версия с поддержкой HATEOAS
                                """.formatted(appDescription))
                        .version(appVersion)
                        .contact(new Contact()
                                .name(companyName)
                                .email("support@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
