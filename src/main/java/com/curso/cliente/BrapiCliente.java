package com.curso.cliente;

import com.curso.dto.BrapiInputDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Component
public class BrapiCliente implements AcaoCliente<BrapiInputDto> {

    private final WebClient webClient;

    public BrapiCliente(
            @Qualifier("brapiWebClient")
            WebClient webClient) {

        this.webClient = webClient;
    }

    @Override
    public BrapiInputDto buscarCotacao(String ticker) {

        BrapiInputDto response = webClient.get()
                .uri("https://brapi.dev/api/quote/" + ticker)
                .retrieve()
                .onStatus(status -> status.isError(),
                        resp -> Mono.error(new RuntimeException("Erro na BRAPI")))
                .bodyToMono(BrapiInputDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        if (response == null ||
                response.getResults() == null ||
                response.getResults().isEmpty()) {
            throw new RuntimeException("Ticker não encontrado na BRAPI");
        }

        return response;
    }
}