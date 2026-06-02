    package com.curso.cliente;

    import com.curso.dto.AlphaOutputDto;
    import com.curso.dto.AlphaOverviewDto;
    import com.curso.exception.TickerInvalidoException;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.stereotype.Component;
    import org.springframework.web.reactive.function.client.WebClient;
    import reactor.core.publisher.Mono;

    import java.time.Duration;

    @Component
    public class AlphaVantageCliente implements AcaoCliente<AlphaOutputDto> {

        private final WebClient webClient;

        public AlphaVantageCliente(@Qualifier("alphaWebClient") WebClient webClient) {
            this.webClient = webClient;
        }
        @Cacheable("overview")
        public AlphaOverviewDto buscarOverview(String ticker) {
            AlphaOverviewDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "OVERVIEW")
                            .queryParam("symbol", ticker)
                            .queryParam("apikey", "E3H8A8NHBKHTY3F5")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            resp -> Mono.error(new RuntimeException("Erro na AlphaVantage OVERVIEW")))
                    .bodyToMono(AlphaOverviewDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || response.getName() == null) {
                throw new RuntimeException("Nome da empresa não encontrado");
            }

            return response;
        }

        @Override
        public AlphaOutputDto buscarCotacao(String ticker) {

            AlphaOutputDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", ticker)
                            .queryParam("apikey", "E3H8A8NHBKHTY3F5")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            resp -> Mono.error(new RuntimeException("Erro na AlphaVantage")))
                    .bodyToMono(AlphaOutputDto.class)
                    .block();

            if (response == null ||
                    response.getGlobalQuote() == null ||
                    response.getGlobalQuote().getPreco() == null ||
                    response.getGlobalQuote().getSimbolo() == null) {

                throw new TickerInvalidoException("Ticker não encontrado");
            }



            return response;
        }
    }