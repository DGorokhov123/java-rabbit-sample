package ru.dgorokhov.controller.cbtest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
public class CircuitBreakerTestController {

    private final Map<String, HttpStatus> cbErrors = Map.of(
            "500", HttpStatus.INTERNAL_SERVER_ERROR,
            "502", HttpStatus.BAD_GATEWAY,
            "503", HttpStatus.SERVICE_UNAVAILABLE,
            "504", HttpStatus.GATEWAY_TIMEOUT
    );

    @RequestMapping(
            value = "/cbtest",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD}
    )
    public ResponseEntity<Map<String, String>> cbTest(
            @RequestParam(name = "code", required = false, defaultValue = "random") String code
    ) {
        if (cbErrors.containsKey(code)) return ResponseEntity
                .status(cbErrors.get(code))
                .body(Map.of("error", cbErrors.get(code).toString()));

        int rnd = new Random().nextInt(cbErrors.size());
        List<HttpStatus> statuses = new ArrayList<>(cbErrors.values());
        HttpStatus status = statuses.get(rnd);

        return ResponseEntity
                .status(status)
                .body(Map.of("error", status.toString()));
    }

}
