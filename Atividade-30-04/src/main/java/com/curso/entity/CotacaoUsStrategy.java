package com.curso.entity;

import com.curso.cliente.AlphaVantageCliente;
import com.curso.dto.CotacaoOutputDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("US")
public class CotacaoUsStrategy implements CotacaoStrategy {

    private final AlphaVantageCliente client;

    public CotacaoUsStrategy(AlphaVantageCliente client) {
        this.client = client;
    }

    @Override
    public CotacaoOutputDto buscar(String ticker) {

        var response = client.buscarCotacao(ticker);

        var q = response.getGlobalQuote();

        if (q == null) {
            throw new RuntimeException("Resposta inválida da AlphaVantage");
        }

        CotacaoOutputDto dto = new CotacaoOutputDto();

        dto.setTicker(q.getSimbolo());
        dto.setCotacao(Double.parseDouble(q.getPreco()));
        dto.setMoeda("USD");
        dto.setNomeEmpresa(ticker); // fallback seguro
        dto.setDataHora(LocalDateTime.now());

        return dto;
    }
}