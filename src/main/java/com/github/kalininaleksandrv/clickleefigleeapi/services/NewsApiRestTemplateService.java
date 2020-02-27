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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class NewsApiRestTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsApiRestTemplateService.class.getName());

    private static final String LATESTNEWSURL = "https://api.currentsapi.services/v1";

    private final WebClient webClient;

    @Value("${currentsapi.secret}")
    private String secret;

    Set<String> listofnews;

    public NewsApiRestTemplateService() {
        listofnews = new LinkedHashSet<>();
        this.webClient = WebClient.builder()
                .baseUrl(LATESTNEWSURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void init(){
        System.out.println("initialize app");

        for (int i = 0; i < 10; i++) {
            try {
                Mono<ClientResponse> responce = getLatestNewsFromApi("en");
                Mono<NewsJsonWraper> newsStream = jsonResponseParser(responce);
                processNewsFeed(newsStream);
                Thread.sleep(300000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        listofnews.forEach(System.out::println);
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

        AtomicInteger addedElementCounter = new AtomicInteger();

        itemsOfNewsStream.subscribe(
            success -> Flux.fromIterable(success.getNews())
                    .doOnNext(item -> {
                            if (listofnews.add(item.getId())) {
                                addedElementCounter.getAndIncrement();
                                System.out.println("----------element added " + item.getId());
                            } else {
                                System.out.println("---------element rejected " + item.getId());
                            }
                    })
                    .doOnError(e -> LOGGER.error("----------Stream of News return error " + e))
                    .doOnComplete(() -> passCountOfelement(addedElementCounter))
                    .subscribe(),
            error -> LOGGER.error("----------API returns error response "+error));

    }

    private void passCountOfelement(AtomicInteger numberOfAddedElements) {

        System.out.println("COUNTED " + numberOfAddedElements);
        trimSetOfId(numberOfAddedElements.get());

    }

    private void trimSetOfId(int numberOfAddedElements) {
        int size = listofnews.size();
        if(size>300){
            String[] targetArray = listofnews.toArray(new String[size]);
            Set<String> strSet = Arrays.stream(targetArray)
                    .skip(numberOfAddedElements)
                    .collect(Collectors.toSet());
            listofnews.clear();
            listofnews.addAll(strSet);
            System.out.println("-----------remove " + numberOfAddedElements + " elements from set");
            AtomicInteger count= new AtomicInteger();
            listofnews.forEach(i -> {
                System.out.println(count +" "+i);
                count.getAndIncrement();
            });
        }
    }
}
