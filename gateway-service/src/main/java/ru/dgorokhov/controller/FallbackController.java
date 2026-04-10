package ru.dgorokhov.controller;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping(
            value = "/fallback/{serviceName}",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD}
    )
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> userServiceFallback(
            ServerWebExchange exchange,
            @PathVariable("serviceName") String serviceName
    ) {
        Throwable exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        String exceptionType = exception != null ? exception.getClass().getName() : "N/A";
        String exceptionMessage = exception != null ? exception.getMessage() : "N/A";
        boolean isCircuitOpen = exception instanceof CallNotPermittedException;

        return Mono.just(
                Map.of(
                        "service", serviceName,
                        "message", serviceName + " is temporarily unavailable. Please try again later.",
                        "exceptionType", exceptionType,
                        "exceptionMessage", exceptionMessage,
                        "circuitState", isCircuitOpen ? "OPEN" : "CLOSED",
                        "timestamp", Instant.now().toString()
                )
        );
    }

}
