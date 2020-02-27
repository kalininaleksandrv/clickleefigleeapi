package com.github.kalininaleksandrv.clickleefigleeapi.configuration;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public class CustomRabbitMQMessagePostProcessor implements MessagePostProcessor {

    private final String ttl;

    public CustomRabbitMQMessagePostProcessor(String ttl) {
        this.ttl = ttl;
    }

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().getHeaders().put("expiration", ttl);
        return message;
    }
}
