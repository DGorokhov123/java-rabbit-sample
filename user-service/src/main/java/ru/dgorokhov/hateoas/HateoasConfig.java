package ru.dgorokhov.hateoas;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация HATEOAS с поддержкой content negotiation.
 * Поддерживаемые форматы: application/json, application/hal+json
 */
@Configuration
@EnableHypermediaSupport(
        type = {
                EnableHypermediaSupport.HypermediaType.HAL,
                EnableHypermediaSupport.HypermediaType.HAL_FORMS
        }
)
public class HateoasConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("hal", new MediaType("application", "hal+json"))
                .favorParameter(true)
                .parameterName("format")
                .ignoreAcceptHeader(false);
    }

}
