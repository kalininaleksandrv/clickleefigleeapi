package com.github.kalininaleksandrv.clickleefigleeapi.configuration;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    Queue queue() {
        Queue queue = new Queue(NEWS_QUEUE_NAME, false);
        queue.isAutoDelete();
        return queue;
    }

    @Bean
    public Declarables topicBindings() {
        Queue topicQueueNews = new Queue(NEWS_QUEUE_NAME, false);
        Queue topicQueueError = new Queue(ERROR_QUEUE_NAME, false);

        TopicExchange topicExchange = new TopicExchange(TOPIC_EXCHANGE_NAME, false, false);

        return new Declarables(topicQueueNews, topicQueueError, topicExchange,
                BindingBuilder
                .bind(topicQueueNews)
                .to(topicExchange)
                .with(BINDING_PATTERN_NEWS),
                BindingBuilder
                .bind(topicQueueError)
                .to(topicExchange)
                .with(BINDING_PATTERN_ERROR));
    }

}
