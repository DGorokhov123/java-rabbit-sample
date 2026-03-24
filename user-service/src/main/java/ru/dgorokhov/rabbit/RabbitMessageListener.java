package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.dgorokhov.config.AppProperties;
import ru.dgorokhov.proto.user.UserNotificationProto;

/*
Слушаем эхо-очередь для контроля отправки. просто логируем
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMessageListener {

    private final AppProperties appProperties;

    @RabbitListener(
            queues = "${app.properties.notification-fanout}.${app.properties.echo-queue-postfix}",
            messageConverter = "protobufMessageConverter"
    )
    public void listen(UserNotificationProto proto) {
        log.debug("[ Received ] id {}, email {}, name {}, action {}",
                proto.getUserId(), proto.getUserEmail(), proto.getUserName(), proto.getActionType());

    }

}
