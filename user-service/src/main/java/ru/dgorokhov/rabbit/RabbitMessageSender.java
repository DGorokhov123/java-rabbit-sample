package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.dgorokhov.config.AppProperties;
import ru.dgorokhov.proto.user.UserNotificationProto;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMessageSender {

    private final AppProperties appProperties;
    private final RabbitTemplate rabbitTemplate;

    public void send(UserNotificationProto proto) {
        String fanout = appProperties.getProperties().getNotificationFanout();
        try {
            rabbitTemplate.convertAndSend(fanout, null, proto);
            log.debug("[   Sent   ] id {}, email {}, name {}, action {}",
                    proto.getUserId(), proto.getUserEmail(), proto.getUserName(), proto.getActionType());
        } catch (AmqpException e) {
            log.error("Failed to send message: user Id = {}, email = {}, action = {}",
                    proto.getUserId(), proto.getUserEmail(), proto.getActionType());
        }
    }

    /*
    Захватываем событие после коммита транзакции и отправляем уведомление
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserNotificationEvent(UserNotificationEvent event) {
        send(event.getUserNotificationProto());
    }

}
