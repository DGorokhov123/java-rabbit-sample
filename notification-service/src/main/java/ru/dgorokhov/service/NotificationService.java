package ru.dgorokhov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dgorokhov.model.UserEventNotification;
import ru.dgorokhov.sender.NotificationChannel;
import ru.dgorokhov.sender.NotificationDto;
import ru.dgorokhov.sender.NotificationSenderAsyncExecutor;
import ru.dgorokhov.sender.SendingStatus;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSenderAsyncExecutor senderAsyncExecutor;

    public void notifyUserEvent(UserEventNotification notification) {
        if (notification == null) return;
        String title, body;

        switch (notification.getType()) {
            case USER_ACTION_CREATE:
                title = "Created User with email " + notification.getEmail();
                body = """
                        Dear %s !
                        
                        We inform you about the successful creation of an account in the 'Userservice' service.
                        To log in, use your email address %s
                        
                        Welcome onboard!
                        """.formatted(notification.getName(), notification.getEmail());
                break;
            case USER_ACTION_UPDATE:
                title = "Updated User with email " + notification.getEmail();
                body = """
                        Dear %s !
                        
                        We are informing you about the successful change of the account data in the 'Userservice'
                        service linked to the email address %s
                        
                        Stay tuned!
                        """.formatted(notification.getName(), notification.getEmail());
                break;
            case USER_ACTION_DELETE:
                title = "Deleted User with email " + notification.getEmail();
                body = """
                        Dear %s !
                        
                        We inform you about the successful deletion of the account in the 'Userservice' service
                        linked to the email address %s
                        
                        Good bye and fuck off!
                        """.formatted(notification.getName(), notification.getEmail());
                break;
            default:
                log.warn("Unrecognized User Event type " + notification.getType());
                return;
        }

        NotificationDto notificationDto = NotificationDto.builder()
                .title(title)
                .body(body)
                .channelData(Map.of(NotificationChannel.EMAIL, notification.getEmail()))
                .build();
        sendAll(notificationDto);
    }

    private void sendAll(NotificationDto dto) {
        for (NotificationChannel channel : dto.getChannelData().keySet()) {
            UUID uuid = UUID.randomUUID();
            senderAsyncExecutor.putStatus(uuid, SendingStatus.ACCEPTED);
            senderAsyncExecutor.sendAsync(dto, channel, uuid);
        }
    }

}
