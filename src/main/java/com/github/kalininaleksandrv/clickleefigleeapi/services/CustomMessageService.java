package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.MessageHolder;
import com.github.kalininaleksandrv.clickleefigleeapi.configuration.CustomRabbitMQMessagePostProcessor;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsDAOWraper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomMessageService implements MessageHolder {

    private final RabbitTemplate rabbitTemplate;

    private Queue queue;

    private CustomRabbitMQMessagePostProcessor messagePostProcessor;

    public CustomMessageService(RabbitTemplate rabbitTemplate, Queue queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
        this.messagePostProcessor = new CustomRabbitMQMessagePostProcessor("10000");
    }

    @Override
    public void holdMessage(News news) {
        NewsDAOWraper message = new NewsDAOWraper(news);
        this.rabbitTemplate.convertAndSend(queue.getName(), message, messagePostProcessor);
    }
}
