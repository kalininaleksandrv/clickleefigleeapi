package com.github.kalininaleksandrv.clickleefigleeapi.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NEWS_QUEUE_NAME = "news-queue";
    public static final String ERROR_QUEUE_NAME = "error-queue";
    public final static String TOPIC_EXCHANGE_NAME = "custom-exchange";
    public static final String BINDING_PATTERN_NEWS = "#.news";
    public static final String BINDING_PATTERN_ERROR = "#.error";

    @Bean
    public Queue newsQueue() {
        return QueueBuilder.nonDurable(NEWS_QUEUE_NAME)
                .ttl(84_000_000)
                .maxLength(1_000)
                .maxLengthBytes(100_000_000)
                .build();
    }

    @Bean
    public Queue errorQueue() {
        return QueueBuilder.nonDurable(ERROR_QUEUE_NAME)
                .maxLength(100)
                .build();
    }

    @Bean
    public Declarables topicBindings() {

        TopicExchange topicExchange = new TopicExchange(TOPIC_EXCHANGE_NAME, false, false);

        return new Declarables(newsQueue(), errorQueue(), topicExchange,
                BindingBuilder
                .bind(newsQueue())
                .to(topicExchange)
                .with(BINDING_PATTERN_NEWS),
                BindingBuilder
                .bind(errorQueue())
                .to(topicExchange)
                .with(BINDING_PATTERN_ERROR));
    }

}
