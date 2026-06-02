package com.curso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public WebClient alphaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://www.alphavantage.co")
                .build();
    }

    @Bean
    public WebClient brapiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://brapi.dev")
                .build();
    }
}