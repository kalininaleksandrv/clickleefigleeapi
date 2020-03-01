package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.configuration.CustomRabbitMQMessagePostProcessor;
import com.github.kalininaleksandrv.clickleefigleeapi.configuration.RabbitMQConfig;
import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.MessageHolder;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsDAOWraper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomMessageService implements MessageHolder {

    private final RabbitTemplate rabbitTemplate;

    private CustomRabbitMQMessagePostProcessor messagePostProcessor;

    private static String ROUTING_KEY_NEWS = "this.news";
    private static String ROUTING_KEY_ERROR = "this.error";

    public CustomMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagePostProcessor = new CustomRabbitMQMessagePostProcessor("10000");
    }

    @Override
    public void holdAllMessages(List<News> news) {
        List<NewsDAOWraper> newsDaoList = getNewsDAOWrapers(news);
        if (saveNewsToDb(newsDaoList)) {
            pushNotificationsToQueue(newsDaoList);
        }
    }

    @Override
    public void holdError(String error) {
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, ROUTING_KEY_ERROR, error);
    }

    private List<NewsDAOWraper> getNewsDAOWrapers(List<News> news) {
        String bunchID = UUID.randomUUID().toString();
        List<NewsDAOWraper> newsDaoList = new LinkedList<>();
        news.forEach(
                i -> {
                    NewsDAOWraper newsDao = new NewsDAOWraper(i);
                    newsDao.setBunchId(bunchID);
                    newsDao.setCreationTime(System.currentTimeMillis());
                    newsDaoList.add(newsDao);
                }
        );
        return newsDaoList;
    }

    private boolean saveNewsToDb(List<NewsDAOWraper> newsDaoList) {
        newsDaoList.forEach(i->System.out.println("save news to db " + i.getId()));
        return true;
    }

    private void pushNotificationsToQueue(List<NewsDAOWraper> newsDaoList) {
        newsDaoList.forEach(i -> this.rabbitTemplate
                .convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME,
                        ROUTING_KEY_NEWS,
                        i.getNews().getId(),
                        messagePostProcessor)

        );
    }

}
