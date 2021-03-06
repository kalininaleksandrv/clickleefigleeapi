package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import com.github.kalininaleksandrv.clickleefigleeapi.model.NewsJsonWraper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class NewsResponseProcessorTest {

    @Mock
    InternalQueueImplementation internalQueueImplementation;
    @Mock
    CustomMessageService customMessageService;

    @InjectMocks
    NewsResponseProcessor newsResponseProcessor;

    @Captor
    private ArgumentCaptor<List<News>> listofnewscaptor;


    private static Mono<ClientResponse> badresponce;
    private static Mono<ClientResponse> goodresponce;

    @BeforeAll()
    static void setUp() {
        badresponce = Mono.just(ClientResponse
                .create(HttpStatus.FORBIDDEN)
                .build());

        goodresponce = Mono.just(ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"status\":\"ok\"," +
                        "\"news\":[{" +
                        "\"id\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"," +
                        "\"title\":\"THIS IS A TEST INSTANCE OF NEWS\"," +
                        "\"description\":\"Prime Minister Muhyiddin Yassin has announced the cabinet line-up a week after he took office amid political chaos, with CIMB Group Holdings chief \\u2026\"," +
                        "\"url\":\"https:\\/\\/www.scmp.com\\/week-asia\\/politics\\/article\\/3074303\\/malaysias-new-cabinet-sees-return-umno-banker-finance-minister\"," +
                        "\"author\":\"flipboard\"," +
                        "\"image\":\"None\"," +
                        "\"language\":\"en\"," +
                        "\"category\":[\"regional\"]," +
                        "\"published\":\"2020-03-09 11:21:16 +0000\"}]," +
                        "\"page\":1}")
                .build());
    }

    @Test
    void parseNewsResponseGood() {

        Mono<NewsJsonWraper> goodnews = newsResponseProcessor.parseNewsResponse(goodresponce);

        StepVerifier
                .create(goodnews)
                .expectNextMatches(i -> i.getStatus().equalsIgnoreCase("OK"))
                .expectComplete()
                .verify();
    }

    @Test
    void parseNewsResponseForbidden() {

        Mono<NewsJsonWraper> badnews = newsResponseProcessor.parseNewsResponse(badresponce);

        StepVerifier
                .create(badnews)
                .expectErrorMessage("403 FORBIDDEN")
                .verify();
    }

    @Test
    void processNewsFeedTrim() {

        when(internalQueueImplementation.compareAndAdd(anyString())).thenReturn(true);

        Mono<NewsJsonWraper> goodnews = newsResponseProcessor.parseNewsResponse(goodresponce);

        newsResponseProcessor.processNewsFeed(goodnews);

        Mockito.verify(internalQueueImplementation, Mockito.times(1)).trimQueue(anyInt());

        ArgumentCaptor<Integer> trimArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(internalQueueImplementation).trimQueue(trimArgumentCaptor.capture());

        Integer savedTrimRequest = trimArgumentCaptor.getValue();
        assertThat(savedTrimRequest).isNotZero();


    }

    @Test
    void processNewsFeedSucsess() {

        when(internalQueueImplementation.compareAndAdd(anyString())).thenReturn(true);

        Mono<NewsJsonWraper> goodnews = newsResponseProcessor.parseNewsResponse(goodresponce);

        newsResponseProcessor.processNewsFeed(goodnews);

        Mockito.verify(customMessageService, Mockito.times(1)).holdAllMessages(any());
        Mockito.verify(customMessageService, Mockito.never()).holdError(anyString());

        Mockito.verify(customMessageService).holdAllMessages(listofnewscaptor.capture());

        List<News> savedNewsRequest = listofnewscaptor.getValue();
        assertThat(savedNewsRequest.get(0).getId()).isEqualTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    }

        @Test
    void processNewsFeedErrorCase() {

        Mono<NewsJsonWraper> badnews = newsResponseProcessor.parseNewsResponse(badresponce);

        newsResponseProcessor.processNewsFeed(badnews);

        Mockito.verify(internalQueueImplementation, Mockito.never()).trimQueue(anyInt());

        Mockito.verify(customMessageService, Mockito.never()).holdAllMessages(any());
        Mockito.verify(customMessageService, Mockito.times(1)).holdError(anyString());

        ArgumentCaptor<String> errorArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(customMessageService).holdError(errorArgumentCaptor.capture());

        String savedErrorRequest = errorArgumentCaptor.getValue();
        assertThat(savedErrorRequest).startsWith("API returns error response");

    }
}