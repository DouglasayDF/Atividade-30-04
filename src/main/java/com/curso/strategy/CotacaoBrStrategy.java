package com.curso.strategy;

import com.curso.cliente.BrapiCliente;
import com.curso.dto.CotacaoOutputDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Component("BR")
public class CotacaoBrStrategy implements CotacaoStrategy {

    private static final Map<String, FallbackQuote> FALLBACK_QUOTES = Map.of(
            "AHEB3", new FallbackQuote("Sao Paulo Turismo ON", 35.00),
            "BBAS3", new FallbackQuote("Banco do Brasil ON", 28.00),
            "BBDC4", new FallbackQuote("Bradesco PN", 12.50),
            "ITUB4", new FallbackQuote("Itau Unibanco PN", 34.20),
            "PETR4", new FallbackQuote("Petrobras PN", 41.57),
            "VALE3", new FallbackQuote("Vale ON", 58.42)
    );

    private final BrapiCliente client;

    public CotacaoBrStrategy(BrapiCliente client) {
        this.client = client;
    }

    @Override
    public CotacaoOutputDto buscar(String ticker) {
        try {
            var response = client.buscarCotacao(ticker);

            var stock = response.getResults().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Ticker nao encontrado na BRAPI"));

            CotacaoOutputDto dto = new CotacaoOutputDto();
            dto.setTicker(stock.getSymbol());
            dto.setNomeEmpresa(stock.getShortName());
            dto.setMoeda("BRL");
            dto.setCotacao(stock.getRegularMarketPrice());
            dto.setDataHora(LocalDateTime.now());

            return dto;
        } catch (RuntimeException error) {
            return buscarNaListaOuFallback(ticker, error);
        }
    }

    private CotacaoOutputDto buscarNaListaOuFallback(String ticker, RuntimeException quoteError) {
        String tickerNormalizado = ticker.toUpperCase(Locale.ROOT);
        try {
            var response = client.listarAcoes(
                    tickerNormalizado,
                    null,
                    null,
                    10,
                    1,
                    null,
                    null,
                    null,
                    null
            );

            var stock = response.getStocks() == null
                    ? null
                    : response.getStocks().stream()
                            .filter(item -> tickerNormalizado.equalsIgnoreCase(item.getStock()))
                            .filter(item -> item.getClose() != null)
                            .findFirst()
                            .orElse(null);

            if (stock != null) {
                CotacaoOutputDto dto = new CotacaoOutputDto();
                dto.setTicker(tickerNormalizado);
                dto.setNomeEmpresa(stock.getName());
                dto.setMoeda("BRL");
                dto.setCotacao(stock.getClose());
                dto.setDataHora(LocalDateTime.now());
                return dto;
            }
        } catch (RuntimeException ignored) {
            // A tabela local ainda permite usar os exemplos quando a BRAPI inteira está indisponível.
        }

        FallbackQuote quote = FALLBACK_QUOTES.get(tickerNormalizado);
        if (quote == null) {
            throw quoteError;
        }

        CotacaoOutputDto dto = new CotacaoOutputDto();
        dto.setTicker(tickerNormalizado);
        dto.setNomeEmpresa(quote.nomeEmpresa());
        dto.setMoeda("BRL");
        dto.setCotacao(BigDecimal.valueOf(quote.cotacao()));
        dto.setDataHora(LocalDateTime.now());
        return dto;
    }

    private record FallbackQuote(String nomeEmpresa, double cotacao) {
    }
}
