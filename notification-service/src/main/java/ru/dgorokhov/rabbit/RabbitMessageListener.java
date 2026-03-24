package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.dgorokhov.mapper.NotificationMapper;
import ru.dgorokhov.proto.user.UserNotificationProto;
import ru.dgorokhov.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMessageListener {

    private final NotificationService notificationService;

    /*
    слушаем очередь, перекладываем protobuf в dto и отправляем в сервис
     */
    @RabbitListener(
            queues = "${app.properties.notification-fanout}.${app.properties.notification-queue-postfix}",
            messageConverter = "protobufMessageConverter"
    )
    public void listen(UserNotificationProto proto) {
        log.debug("Received message: user Id = {}, email = {}, action = {}",
                proto.getUserId(), proto.getUserEmail(), proto.getActionType());
        notificationService.notifyUserEvent(NotificationMapper.fromProto(proto));
    }

}
