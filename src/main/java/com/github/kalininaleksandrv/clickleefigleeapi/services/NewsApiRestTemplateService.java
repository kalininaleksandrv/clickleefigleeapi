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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class NewsApiRestTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsApiRestTemplateService.class.getName());

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
        Mono<ClientResponse> responce = getLatestNewsFromApi("en");
        Optional<ArrayList<News>> listOfNews = jsonResponceParser(responce);
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

    private Optional<ArrayList<News>> jsonResponceParser(Mono<ClientResponse> responce) {

        NewsJsonWraper fetchedData = Objects.requireNonNull(responce
                .block(), "data must not be null")
                .bodyToMono(NewsJsonWraper.class)
                .block();

        if (fetchedData!=null && fetchedData.getStatus().equalsIgnoreCase("ok")){
            fetchedData.getNews()
                    .forEach(System.out::println);
            return Optional.of(fetchedData.getNews());
        } else if (fetchedData!=null){
            LOGGER.warn("API returns status: "+ fetchedData.getStatus());
            return Optional.empty();
        } else {
            LOGGER.error("API returns empty response");
            return Optional.empty();
        }
    }

}
