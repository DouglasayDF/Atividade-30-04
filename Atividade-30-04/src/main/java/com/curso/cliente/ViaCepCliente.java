package com.curso.cliente;

import com.curso.dto.CepOutputDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class ViaCepCliente {

    private final WebClient webClient;

    public ViaCepCliente(WebClient webClient) {
        this.webClient = webClient;
    }

    public CepOutputDto buscarCep(String cep) {
        try {
            CepOutputDto response = webClient.get()
                    .uri("https://viacep.com.br/ws/" + cep + "/json/")
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            resp -> Mono.error(new RuntimeException("CEP inválido")))
                    .onStatus(status -> status.is5xxServerError(),
                            resp -> Mono.error(new RuntimeException("API ViaCEP fora do ar")))
                    .bodyToMono(CepOutputDto.class)
                    .block();

            if (response == null || response.getLogradouro() == null) {
                throw new RuntimeException("CEP não encontrado");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar CEP: " + e.getMessage());
        }
    }
}