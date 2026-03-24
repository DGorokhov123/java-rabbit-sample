package ru.dgorokhov.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сюда спринг автоматом инжектит все бины имплементироующие NotificationSender
 * И затем по ключу мы их можем выдергивать и использовать
 */
@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders;

    @Autowired
    public NotificationSenderFactory(Set<NotificationSender> senderBeans) {
        this.senders = senderBeans.stream()
                .collect(Collectors.toMap(
                        NotificationSender::getChannel,
                        s -> s
                ));
    }

    public NotificationSender getSender(NotificationChannel channel) {
        return senders.get(channel);
    }

}
