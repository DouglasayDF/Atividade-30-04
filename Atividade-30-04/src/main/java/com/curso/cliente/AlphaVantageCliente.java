    package com.curso.cliente;

    import com.curso.dto.AlphaOutputDto;
    import org.springframework.stereotype.Component;
    import org.springframework.web.reactive.function.client.WebClient;

    @Component
    public class AlphaVantageCliente implements AcaoCliente<AlphaOutputDto> {

        private final WebClient webClient;

        public AlphaVantageCliente(WebClient webClient) {
            this.webClient = webClient;
        }

        @Override
        public AlphaOutputDto buscarCotacao(String ticker) {
            var response = webClient.get()
                    .uri("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                            + ticker + "&apikey=SUA_API_KEY")
                    .retrieve()
                    .bodyToMono(AlphaOutputDto.class)
                    .block();

            if (response == null || response.getGlobalQuote() == null) {
                throw new RuntimeException("Cotação não encontrada na AlphaVantage");
            }
            return response;
        }
    }
