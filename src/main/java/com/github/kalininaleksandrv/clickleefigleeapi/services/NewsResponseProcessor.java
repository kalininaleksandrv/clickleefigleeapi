package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsJsonWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NewsResponseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsResponseProcessor.class.getName());

    private final InternalQueueImplementation internalQueueImplementation;
    private final CustomMessageService customMessageService;

    public NewsResponseProcessor(InternalQueueImplementation internalQueueImplementation,
                                 CustomMessageService customMessageService) {
        this.internalQueueImplementation = internalQueueImplementation;
        this.customMessageService = customMessageService;
    }


    public Mono<NewsJsonWraper> parseNewsResponse(Mono<ClientResponse> responce) {

        return responce.flatMap(response -> {
            if (!response.statusCode().is2xxSuccessful()) return Mono.error(new Exception(response.statusCode().toString()));
            return responce.flatMap(i->i.bodyToMono(NewsJsonWraper.class));
        });

    }

    public void processNewsFeed(Mono<NewsJsonWraper> itemsOfNewsStream) {

        AtomicInteger addedElementCounter = new AtomicInteger(0);
        List<News> newsToSave = new LinkedList<>();

        itemsOfNewsStream.subscribe(
                success -> Flux.fromIterable(success.getNews())
                        .doOnNext(item -> {
                            if (internalQueueImplementation.compareAndAdd(item.getId())) {
                                addedElementCounter.getAndIncrement();
                                newsToSave.add(item);
                            }
                        })
                        .doOnError(e -> {
                            String msg = "Stream of News return error " + e;
                            LOGGER.error(msg);
                            customMessageService.holdError(msg);
                        })
                        .doOnComplete(() -> {
                            LOGGER.info("passed to MessageHolder: " + addedElementCounter + " news");
                            customMessageService.holdAllMessages(newsToSave);
                            internalQueueImplementation.trimQueue(addedElementCounter.get());
                            newsToSave.clear();
                            addedElementCounter.set(0);
                        })
                        .subscribe(),
                error -> {
                    String msg = "API returns error response " + error;
                    LOGGER.error(msg);
                    customMessageService.holdError(msg);
                });
    }
}
