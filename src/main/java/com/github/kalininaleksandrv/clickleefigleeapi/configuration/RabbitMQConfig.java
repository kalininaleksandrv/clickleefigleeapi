package com.github.kalininaleksandrv.clickleefigleeapi.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    static final String queueName = "news-queue";

    @Bean
    Queue queue() {
        Queue queue = new Queue(queueName, false);
        queue.isAutoDelete();
        return queue;
    }
}
