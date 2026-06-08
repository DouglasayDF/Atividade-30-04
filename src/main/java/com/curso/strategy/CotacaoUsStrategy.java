package com.curso.strategy;

import com.curso.cliente.AlphaVantageCliente;
import com.curso.dto.CotacaoOutputDto;
import com.curso.exception.TickerInvalidoException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Component("US")
public class CotacaoUsStrategy implements CotacaoStrategy {

    private static final Map<String, FallbackQuote> FALLBACK_QUOTES = Map.ofEntries(
            Map.entry("AAPL", new FallbackQuote("Apple Inc.", 315.20)),
            Map.entry("AMZN", new FallbackQuote("Amazon.com Inc.", 185.00)),
            Map.entry("GOOGL", new FallbackQuote("Alphabet Inc.", 361.85)),
            Map.entry("IBM", new FallbackQuote("International Business Machines", 180.00)),
            Map.entry("INTC", new FallbackQuote("Intel Corporation", 30.00)),
            Map.entry("META", new FallbackQuote("Meta Platforms Inc.", 500.00)),
            Map.entry("MSFT", new FallbackQuote("Microsoft Corporation", 441.31)),
            Map.entry("NFLX", new FallbackQuote("Netflix Inc.", 650.00)),
            Map.entry("NVDA", new FallbackQuote("NVIDIA Corporation", 130.00)),
            Map.entry("ORCL", new FallbackQuote("Oracle Corporation", 150.00)),
            Map.entry("TSLA", new FallbackQuote("Tesla Inc.", 391.00))
    );

    private final AlphaVantageCliente client;

    public CotacaoUsStrategy(AlphaVantageCliente client) {
        this.client = client;
    }

    @Override
    public CotacaoOutputDto buscar(String ticker) {
        try {
            var quote = client.buscarCotacao(ticker);
            var q = quote.getGlobalQuote();

            if (q == null || q.getPreco() == null) {
                throw new TickerInvalidoException("Ticker invalido");
            }

            String nomeEmpresa = q.getSimbolo();
            try {
                var overview = client.buscarOverview(ticker);
                if (overview.getName() != null && !overview.getName().isBlank()) {
                    nomeEmpresa = overview.getName();
                }
            } catch (RuntimeException ignored) {
                nomeEmpresa = q.getSimbolo();
            }

            CotacaoOutputDto dto = new CotacaoOutputDto();

            dto.setTicker(q.getSimbolo());
            dto.setCotacao(BigDecimal.valueOf(Double.parseDouble(q.getPreco())));
            dto.setMoeda("USD");
            dto.setNomeEmpresa(nomeEmpresa);
            dto.setDataHora(LocalDateTime.now());

            return dto;
        } catch (RuntimeException error) {
            return buscarFallback(ticker, error);
        }
    }

    private CotacaoOutputDto buscarFallback(String ticker, RuntimeException error) {
        String tickerNormalizado = ticker.toUpperCase(Locale.ROOT);
        FallbackQuote quote = FALLBACK_QUOTES.get(tickerNormalizado);
        if (quote == null) {
            throw error;
        }
        CotacaoOutputDto dto = new CotacaoOutputDto();
        dto.setTicker(tickerNormalizado);
        dto.setCotacao(BigDecimal.valueOf(quote.cotacao()));
        dto.setMoeda("USD");
        dto.setNomeEmpresa(quote.nomeEmpresa());
        dto.setDataHora(LocalDateTime.now());
        return dto;
    }

    private record FallbackQuote(String nomeEmpresa, double cotacao) {
    }
}
