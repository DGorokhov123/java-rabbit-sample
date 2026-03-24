package ru.dgorokhov.rabbit;

import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.Method;

public class ProtobufMessageConverter implements MessageConverter {

    public static final String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    @Override
    public Message toMessage(
            @NonNull Object object,
            @NonNull MessageProperties props
    ) throws MessageConversionException {
        if (object instanceof com.google.protobuf.Message proto) {
            props.setContentType(PROTOBUF_CONTENT_TYPE);
            props.setHeader("X-Protobuf-Type", proto.getClass().getName());
            return new Message(proto.toByteArray(), props);
        } else {
            throw new MessageConversionException("Expected Protobuf message, got: " + object.getClass());
        }
    }

    @Override
    public Object fromMessage(
            @NonNull Message message
    ) throws MessageConversionException {
        String typeName = message.getMessageProperties().getHeader("X-Protobuf-Type");

        if (typeName == null)
            throw new MessageConversionException("Not found 'X-Protobuf-Type' header, cannot deserialize");

        try {
            Class<?> clazz = Class.forName(typeName);
            if (!com.google.protobuf.Message.class.isAssignableFrom(clazz))
                throw new MessageConversionException("Class is not Protobuf Message: " + typeName);
            Method parseFrom = clazz.getMethod("parseFrom", byte[].class);
            return parseFrom.invoke(null, message.getBody());
        } catch (Exception e) {
            throw new MessageConversionException("Failed to deserialize Protobuf message", e);
        }
    }

}
