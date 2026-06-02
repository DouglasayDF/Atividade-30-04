package com.curso.cliente;

import com.curso.dto.CepOutputDto;
import com.curso.exception.CepInvalidoException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;


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
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new CepInvalidoException(
                        "CEP não encontrado"
                );
            }

            if (Boolean.TRUE.equals(response.getErro())) {
                throw new CepInvalidoException(
                        "CEP inválido"
                );
            }
            return response;

        } catch (Exception e) {
            throw new CepInvalidoException("Erro ao consultar CEP: " + e.getMessage());
        }
    }
}