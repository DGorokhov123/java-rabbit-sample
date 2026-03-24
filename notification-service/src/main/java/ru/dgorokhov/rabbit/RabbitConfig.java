package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dgorokhov.config.AppProperties;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;

    /*
    Объявляем exchange во всех микросервисах т.к. мы не знаем, какой инстанс поднимется первым
     */
    @Bean
    public FanoutExchange notificationFanoutExchange() {
        String fanout = appProperties.getProperties().getNotificationFanout();
        return new FanoutExchange(fanout, true, false);
    }

    /*
    Объявляем целевую очередь которую будет слушать лиснер
     */
    @Bean
    public Queue notificationQueue() {
        String fanout = appProperties.getProperties().getNotificationFanout();
        String postfix = appProperties.getProperties().getNotificationQueuePostfix();
        return QueueBuilder.durable(fanout + "." + postfix).build();
    }

    /*
    биндим очередь к эксченджу
     */
    @Bean
    public Binding binding(Queue queue, FanoutExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange);
    }

    /*
    создаем конвертер для десериализации profobuf
     */
    @Bean
    public MessageConverter protobufMessageConverter() {
        return new ProtobufMessageConverter();
    }

}
