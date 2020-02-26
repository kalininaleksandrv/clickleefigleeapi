package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsJsonWraper;
import net.minidev.json.JSONUtil;
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
        Mono<ClientResponse> responce = getLatestNewsFromApi("qq");
        jsonResponseParser(responce);
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

    private void jsonResponseParser(Mono<ClientResponse> responce) {

        Mono<ClientResponse> resp = responce.flatMap(response -> {
            if (!response.statusCode().is2xxSuccessful()) return Mono.error(new Exception(response.statusCode().toString()));
            return Mono.just(response);
        });

        resp.subscribe(
                success -> {
                    try {
                        success.bodyToMono(NewsJsonWraper.class).subscribe(
                                i -> i.getNews().forEach(System.out::println)
                        );
                    } catch (Exception e) {
                        LOGGER.error("Parse error "+e);
                    }
                },
                error -> {
                    LOGGER.error("API returns error response "+error);
                });
    }

}
