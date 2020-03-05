package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsJsonWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NewsApiRestTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsApiRestTemplateService.class.getName());

    private static final String LATESTNEWSURL = "https://api.currentsapi.services/v1";

    private final WebClient webClient;

    private final CustomMessageService customMessageService;

    private final InternalQueueImplementation internalQueueImplementation;

    @Value("${currentsapi.secret}")
    private String secret;

    @Value("${currentsapi.lang}")
    private String lang;

    public NewsApiRestTemplateService(CustomMessageService customMessageService, InternalQueueImplementation internalQueueImplementation) {
        this.customMessageService = customMessageService;
        this.internalQueueImplementation = internalQueueImplementation;
        this.webClient = WebClient.builder()
                .baseUrl(LATESTNEWSURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void init(){
        LOGGER.info("starting app...");
        startSheduligConsumingNewsApi();
    }

    @Scheduled(initialDelay = 300000, fixedRate=300000)
    private void startSheduligConsumingNewsApi() {
        Mono<ClientResponse> responce = getLatestNewsFromApi(lang);
        Mono<NewsJsonWraper> newsStream = jsonResponseParser(responce);
        processNewsFeed(newsStream);
    }

    public Mono<ClientResponse> getLatestNewsFromApi (String lang) {

        return webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/latest-news")
                        .queryParam("language", lang)
                        .queryParam("apiKey", secret)
                        .build())
                .exchange();
    }

    private Mono<NewsJsonWraper> jsonResponseParser(Mono<ClientResponse> responce) {

        return responce.flatMap(response -> {
            if (!response.statusCode().is2xxSuccessful()) return Mono.error(new Exception(response.statusCode().toString()));
            return responce.flatMap(i->i.bodyToMono(NewsJsonWraper.class));
        });

    }

    private void processNewsFeed(Mono<NewsJsonWraper> itemsOfNewsStream) {

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
