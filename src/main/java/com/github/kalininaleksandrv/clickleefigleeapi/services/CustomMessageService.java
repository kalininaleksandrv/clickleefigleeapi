package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.configuration.CustomRabbitMQMessagePostProcessor;
import com.github.kalininaleksandrv.clickleefigleeapi.configuration.RabbitMQConfig;
import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.MessageHolder;
import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.NewsRepository;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class CustomMessageService implements MessageHolder {

    private final NewsRepository newsRepository;
    private final RabbitTemplate rabbitTemplate;
    private CustomRabbitMQMessagePostProcessor messagePostProcessor;

    private static final String CREATIONTIME = "creationtime";
    private static final String NEWSID = "newsid";
    private static final String COUNTNEWSINBUNCH = "countnewsinbunch";
    private static String ROUTING_KEY_NEWS = "this.news";
    private static String ROUTING_KEY_ERROR = "this.error";

    public CustomMessageService(NewsRepository newsRepository, RabbitTemplate rabbitTemplate) {
        this.newsRepository = newsRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messagePostProcessor = new CustomRabbitMQMessagePostProcessor("10000");
    }

    @Override
    public void holdAllMessages(List<News> news) {
            pushNotificationsToQueue(saveNewsToDb(news));
    }

    @Override
    public void holdError(String error) {
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, ROUTING_KEY_ERROR, error);
    }

    private List<String> saveNewsToDb(List<News> newsList) {
        List<String> listOfSavedIds = new LinkedList<>();
        newsList.forEach(i->{
            System.out.println("save news to db " + i.getId());
            listOfSavedIds.add(i.getId());
        });
        newsRepository.saveAll(newsList);
        return listOfSavedIds;
    }

    private void pushNotificationsToQueue(@NotNull List<String> newsIdList) {
        String count = String.valueOf(newsIdList.size());
        newsIdList.forEach(i -> {
            Map<String, String> nsIdMap = new HashMap<>();
            nsIdMap.put(CREATIONTIME, String.valueOf(System.currentTimeMillis()));
            nsIdMap.put(NEWSID, i);
            nsIdMap.put(COUNTNEWSINBUNCH, count);
            this.rabbitTemplate
                .convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME,
                        ROUTING_KEY_NEWS,
                        nsIdMap,
                        messagePostProcessor);
            }

        );
    }

}
