package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.dgorokhov.proto.user.UserNotificationProto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMessageListener {

    private final Map<Long, UserNotificationProto> protos = new ConcurrentHashMap<>();

    @RabbitListener(
            queues = "${app.properties.notification-fanout}.${app.properties.notification-queue-postfix}",
            messageConverter = "protobufMessageConverter"
    )
    public void listen(UserNotificationProto proto) {
        protos.put(proto.getUserId(), proto);
    }

    public UserNotificationProto getProtoByUserId(Long userId) {
        return protos.get(userId);
    }

}
