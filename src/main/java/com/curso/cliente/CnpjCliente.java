package com.curso.cliente;

import com.curso.dto.CnpjClienteOutputDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;


@Component
public class CnpjCliente {

    private final WebClient webClient;

    public CnpjCliente(WebClient webClient) {
        this.webClient = webClient;
    }

    public CnpjClienteOutputDto buscarCnpj(String cnpj) {

        CnpjClienteOutputDto response = webClient.get()
                .uri("https://brasilapi.com.br/api/cnpj/v1/" + cnpj)
                .retrieve()
                .bodyToMono(CnpjClienteOutputDto.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        if (response == null) {
            throw new RuntimeException("CNPJ não encontrado na API");
        }

        return response;
    }
}
