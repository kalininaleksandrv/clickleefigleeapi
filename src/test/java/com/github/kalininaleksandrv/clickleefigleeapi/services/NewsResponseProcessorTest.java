package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsJsonWraper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class NewsResponseProcessorTest {

    @Mock
    InternalQueueImplementation internalQueueImplementation;
    @Mock
    CustomMessageService customMessageService;

    @InjectMocks
    NewsResponseProcessor newsResponseProcessor;

    @Test
    void parseNewsResponseGood() {

        Mono<ClientResponse> goodresponce = Mono.just(ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"status\":\"ok\"," +
                        "\"news\":[{" +
                        "\"id\":\"ac58169c-e157-4619-be58-bcd424334216\"," +
                        "\"title\":\"Malaysia's new cabinet sees return of Umno, with a banker as finance minister\"," +
                        "\"description\":\"Prime Minister Muhyiddin Yassin has announced the cabinet line-up a week after he took office amid political chaos, with CIMB Group Holdings chief \\u2026\"," +
                        "\"url\":\"https:\\/\\/www.scmp.com\\/week-asia\\/politics\\/article\\/3074303\\/malaysias-new-cabinet-sees-return-umno-banker-finance-minister\"," +
                        "\"author\":\"flipboard\"," +
                        "\"image\":\"None\"," +
                        "\"language\":\"en\"," +
                        "\"category\":[\"regional\"]," +
                        "\"published\":\"2020-03-09 11:21:16 +0000\"}]," +
                        "\"page\":1}")
                .build());

        Mono<NewsJsonWraper> goodnews = newsResponseProcessor.parseNewsResponse(goodresponce);

        StepVerifier
                .create(goodnews)
                .expectNextMatches(i -> i.getStatus().equalsIgnoreCase("OK"))
                .expectComplete()
                .verify();
    }


    @Test
    void parseNewsResponseForbidden() {

        Mono<ClientResponse> badresponce = Mono.just(ClientResponse
                .create(HttpStatus.FORBIDDEN)
                .build());

        Mono<NewsJsonWraper> badnews = newsResponseProcessor.parseNewsResponse(badresponce);

        StepVerifier
                .create(badnews)
                .expectErrorMessage("403 FORBIDDEN")
                .verify();
    }


    @Test
    void processNewsFeed() {
    }
}