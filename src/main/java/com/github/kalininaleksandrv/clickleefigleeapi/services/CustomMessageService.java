package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.configuration.RabbitMQConfig;
import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.MessageHolder;
import com.github.kalininaleksandrv.clickleefigleeapi.configuration.CustomRabbitMQMessagePostProcessor;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsDAOWraper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
public class CustomMessageService implements MessageHolder {

    private final RabbitTemplate rabbitTemplate;

    private Queue queue;

    private CustomRabbitMQMessagePostProcessor messagePostProcessor;

    private static String ROUTING_KEY_NEWS = "this.news";
    private static String ROUTING_KEY_ERROR = "this.error";

    public CustomMessageService(RabbitTemplate rabbitTemplate, Queue queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
        this.messagePostProcessor = new CustomRabbitMQMessagePostProcessor("10000");
    }

    @Override
    public void holdAllMessages(List<News> news) {

        List<NewsDAOWraper> newsDaoList = new LinkedList<>();
        news.forEach(
                i -> {
                    this.rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, ROUTING_KEY_NEWS, i.getId(), messagePostProcessor);
                    NewsDAOWraper newsDao = new NewsDAOWraper(i);
                    newsDaoList.add(newsDao);
                }
        );
        saveNewsToDb(newsDaoList);
    }


    @Override
    public void holdError(String error) {
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, ROUTING_KEY_ERROR, error);
    }

    @Async
    private void saveNewsToDb(List<NewsDAOWraper> newsDaoList) {
        newsDaoList.forEach(i->System.out.println("save news to db " + i.getId()));
    }

}
