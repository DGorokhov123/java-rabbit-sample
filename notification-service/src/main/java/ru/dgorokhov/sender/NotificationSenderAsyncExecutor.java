package ru.dgorokhov.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Асинхронно отправляет уведомления и обновляет статус
 * вынесен в отдельный класс чтобы уменьшить оверхед при проксировании
 */
@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class NotificationSenderAsyncExecutor {

    private final NotificationSenderFactory senderFactory;

    /*
    простейшая реализация хранения статусов. делает сервис stateful и так конечно делать нельзя если мы
    хотим поднимать несколько инстансов. нужно выносить состояние в redis (имхо лучший вариант) или в бд.
    также нужно его периодически чистить от старых записей.
    но в рамках MVP можно оставить пока и так =)
     */
    private final Map<UUID, SendingStatus> sendingStatuses = new ConcurrentHashMap<>();

    @Async("notificationExecutor")
    public void sendAsync(NotificationDto dto, NotificationChannel channel, UUID uuid) {
        try {
            senderFactory.getSender(channel).send(dto);
            sendingStatuses.put(uuid, SendingStatus.SENT);
        } catch (NotificationDeliveryException e) {
            log.warn("Failed sending notification through {}", channel, e);
            sendingStatuses.put(uuid, SendingStatus.REJECTED);
        }
    }

    public void putStatus(UUID uuid, SendingStatus status) {
        sendingStatuses.put(uuid, status);
    }

    public SendingStatus getStatus(UUID uuid) {
        return sendingStatuses.getOrDefault(uuid, SendingStatus.UNKNOWN);
    }


}
