package com.curso.cliente;

import com.curso.dto.BrapiInputDto;
import com.curso.dto.BrapiListResponseDto;
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

    public BrapiListResponseDto listarAcoes(
            String search,
            String sortBy,
            String sortOrder,
            Integer limit,
            Integer page,
            String sector,
            String type,
            String subType,
            String token) {

        BrapiListResponseDto response = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/quote/list");
                    if (search != null && !search.isBlank()) builder.queryParam("search", search);
                    if (sortBy != null && !sortBy.isBlank()) builder.queryParam("sortBy", sortBy);
                    if (sortOrder != null && !sortOrder.isBlank()) builder.queryParam("sortOrder", sortOrder);
                    if (limit != null) builder.queryParam("limit", limit);
                    if (page != null) builder.queryParam("page", page);
                    if (sector != null && !sector.isBlank()) builder.queryParam("sector", sector);
                    if (type != null && !type.isBlank()) builder.queryParam("type", type);
                    if (subType != null && !subType.isBlank()) builder.queryParam("subType", subType);
                    if (token != null && !token.isBlank()) builder.queryParam("token", token);
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status.isError(),
                        resp -> Mono.error(new RuntimeException("Erro na lista de ações da BRAPI")))
                .bodyToMono(BrapiListResponseDto.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        if (response == null) {
            throw new RuntimeException("Lista de ações não encontrada na BRAPI");
        }

        return response;
    }
}
