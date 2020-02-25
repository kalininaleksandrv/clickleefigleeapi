package com.github.kalininaleksandrv.clickleefigleeapi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class NewsApiRestTemplateService {

    private static final String LATESTNEWSURL = "https://api.currentsapi.services/v1";

    private final WebClient webClient;

    @Value("${currentsapi.secret}")
    private String secret;

    public NewsApiRestTemplateService() {
        this.webClient = WebClient.builder()
                .baseUrl(LATESTNEWSURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void init(){
        System.out.println("initialize app");
        getLatestNewsFromApi("en");
    }

    public void getLatestNewsFromApi (String lang){

        Mono<ClientResponse> newsResponse = webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/latest-news")
                        .queryParam("language", lang)
                        .queryParam("apiKey", secret)
                        .build())
                .exchange();

        String fetchedData = Objects.requireNonNull(newsResponse
                .block())
                .bodyToMono(String.class)
                .block();

        System.out.println(fetchedData);
    }
}
