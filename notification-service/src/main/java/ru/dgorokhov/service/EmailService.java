package ru.dgorokhov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dgorokhov.dto.SendEmailRequestDto;
import ru.dgorokhov.dto.SendEmailResponseDto;
import ru.dgorokhov.model.UserEventNotification;
import ru.dgorokhov.sender.NotificationChannel;
import ru.dgorokhov.sender.NotificationDto;
import ru.dgorokhov.sender.NotificationSenderAsyncExecutor;
import ru.dgorokhov.sender.SendingStatus;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final NotificationSenderAsyncExecutor senderAsyncExecutor;

    public SendEmailResponseDto sendEmail(SendEmailRequestDto sendEmailRequestDto) {
        NotificationDto notificationDto = NotificationDto.builder()
                .title(sendEmailRequestDto.getTitle())
                .body(sendEmailRequestDto.getBody())
                .channelData(Map.of(NotificationChannel.EMAIL, sendEmailRequestDto.getEmail()))
                .build();

        UUID uuid = UUID.randomUUID();
        senderAsyncExecutor.putStatus(uuid, SendingStatus.ACCEPTED);
        senderAsyncExecutor.sendAsync(notificationDto, NotificationChannel.EMAIL, uuid);

        return SendEmailResponseDto.builder()
                .uuid(uuid)
                .status(String.valueOf(SendingStatus.ACCEPTED))
                .uri(URI.create("/email/" + uuid + "/status"))
                .build();
    }

    public SendEmailResponseDto getStatus(UUID uuid) {
        SendingStatus status = senderAsyncExecutor.getStatus(uuid);
        return SendEmailResponseDto.builder()
                .uuid(uuid)
                .status(String.valueOf(status))
                .uri(URI.create("/email/" + uuid + "/status"))
                .build();
    }

}
