package com.github.kalininaleksandrv.clickleefigleeapi.services;

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
import reactor.core.publisher.Mono;

@Service
public class NewsApiRestTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsApiRestTemplateService.class.getName());

    private static final String LATESTNEWSURL = "https://api.currentsapi.services/v1";

    private final WebClient webClient;
    private final NewsResponseProcessor newsResponseProcessor;

    @Value("${currentsapi.secret}")
    private String secret;

    @Value("${currentsapi.lang}")
    private String lang;

    public NewsApiRestTemplateService(NewsResponseProcessor newsResponseProcessor) {
        this.newsResponseProcessor = newsResponseProcessor;
        this.webClient = WebClient.builder()
                .baseUrl(LATESTNEWSURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        getAndProcessResponse();
        LOGGER.info("NewsApiRestTemplateService starts ...");
    }

    @Scheduled(initialDelay = 300000, fixedRate=300000)
    public void getAndProcessResponse(){
        Mono<ClientResponse> response = getLatestNewsFromApi(lang);
        Mono<NewsJsonWraper> newsStream = newsResponseProcessor.parseNewsResponse(response);
        newsResponseProcessor.processNewsFeed(newsStream);
    }

    private Mono<ClientResponse> getLatestNewsFromApi(String language) {

        return webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/latest-news")
                        .queryParam("language", language)
                        .queryParam("apiKey", secret)
                        .build())
                .exchange();
    }

}
