package com.curso.cliente;

import com.curso.dto.BrapiInputDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class BrapiCliente implements AcaoCliente<BrapiInputDto> {

    private final WebClient webClient;

    public BrapiCliente(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public BrapiInputDto buscarCotacao(String ticker) {

        BrapiInputDto response = webClient.get()
                .uri("https://brapi.dev/api/quote/" + ticker)
                .retrieve()
                .bodyToMono(BrapiInputDto.class)
                .block();

        if (response == null ||
                response.getResults() == null ||
                response.getResults().isEmpty()) {
            throw new RuntimeException("Ticker não encontrado na BRAPI");
        }

        return response;
    }
}