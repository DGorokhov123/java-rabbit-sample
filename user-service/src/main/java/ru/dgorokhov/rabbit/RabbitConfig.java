package ru.dgorokhov.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dgorokhov.config.AppProperties;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final AppProperties appProperties;

    /*
    Объявляем exchange
     */
    @Bean
    public FanoutExchange notificationFanoutExchange() {
        String fanout = appProperties.getProperties().getNotificationFanout();
        return new FanoutExchange(fanout, true, false);
    }

    /*
    делаем эхо-очередь чтобы самим ее читать. можно использовать для контроля или отладки
     */
    @Bean
    public Queue notificationEchoQueue() {
        String fanout = appProperties.getProperties().getNotificationFanout();
        String postfix = appProperties.getProperties().getEchoQueuePostfix();
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
    конвертер для сериализации protobuf
     */
    @Bean
    public MessageConverter protobufMessageConverter() {
        return new ProtobufMessageConverter();
    }

    /*
    конвертер для сериализации json
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        var mapper = JsonMapper.builder().build();
        return new JacksonJsonMessageConverter(mapper);
    }

    /*
    RabbitTemplate для отправки сообщений в кролика
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory cf,
            @Qualifier("protobufMessageConverter") MessageConverter converter
    ) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }

}
