package com.reliaquest.api.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final MockApiProperties mockApiProperties;

    @Bean
    public WebClient mockApiWebClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, mockApiProperties.getConnectionTimeout())
                .responseTimeout(Duration.ofMillis(mockApiProperties.getReadTimeout()))
                .doOnConnected(conn -> conn.addHandlerLast(
                                new ReadTimeoutHandler(mockApiProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                                new WriteTimeoutHandler(mockApiProperties.getReadTimeout(), TimeUnit.MILLISECONDS)));

        log.info("Configuring WebClient for Mock API with base URL: {}", mockApiProperties.getBaseUrl());

        return webClientBuilder
                .baseUrl(mockApiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
