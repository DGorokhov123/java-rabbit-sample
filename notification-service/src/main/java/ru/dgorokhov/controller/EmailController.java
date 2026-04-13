package ru.dgorokhov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.dgorokhov.dto.notification.SendEmailRequestDto;
import ru.dgorokhov.dto.notification.SendEmailResponseDto;
import ru.dgorokhov.service.EmailService;

import java.util.UUID;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /*
    Отправка писем. Возвращаем сразу статус ACCEPTED, отправляем асинхронно
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/email")
    public SendEmailResponseDto sendEmail(
            @RequestBody @Valid SendEmailRequestDto sendEmailRequestDto
    ) {
        return emailService.sendEmail(sendEmailRequestDto);
    }

    /*
    Проверка статуса отправки. Можно вызывать с фронта раз в N секунд для подтверждения отправки
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/email/{uuid}/status")
    public SendEmailResponseDto getStatus(
            @PathVariable UUID uuid
    ) {
        return emailService.getStatus(uuid);
    }


}
