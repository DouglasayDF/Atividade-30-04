package com.curso.strategy;

import com.curso.cliente.AlphaVantageCliente;
import com.curso.dto.CotacaoOutputDto;
import com.curso.exception.TickerInvalidoException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component("US")
public class CotacaoUsStrategy implements CotacaoStrategy {

    private final AlphaVantageCliente client;

    public CotacaoUsStrategy(AlphaVantageCliente client) {
        this.client = client;
    }

    @Override
    public CotacaoOutputDto buscar(String ticker) {

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
    }
}
