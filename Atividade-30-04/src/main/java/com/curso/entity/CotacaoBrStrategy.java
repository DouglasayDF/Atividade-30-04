package com.curso.entity;

import com.curso.cliente.BrapiCliente;
import com.curso.dto.CotacaoOutputDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("BR")
public class CotacaoBrStrategy implements CotacaoStrategy {

    private final BrapiCliente client;

    public CotacaoBrStrategy(BrapiCliente client) {
        this.client = client;
    }

    @Override
    public CotacaoOutputDto buscar(String ticker) {

        var response = client.buscarCotacao(ticker);

        var stock = response.getResults().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ticker não encontrado na BRAPI"));

        CotacaoOutputDto dto = new CotacaoOutputDto();
        dto.setTicker(stock.getSymbol());
        dto.setNomeEmpresa(stock.getShortName());
        dto.setMoeda("BRL");
        dto.setCotacao(stock.getRegularMarketPrice());
        dto.setDataHora(LocalDateTime.now());

        return dto;
    }
}