package ru.dgorokhov.hateoas;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@Configuration
@EnableHypermediaSupport(
        type = {
                EnableHypermediaSupport.HypermediaType.HAL,
                EnableHypermediaSupport.HypermediaType.HAL_FORMS
        }
)
public class HateoasConfig {
}
