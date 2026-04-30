package com.curso.service;

import com.curso.dto.CotacaoOutputDto;
import com.curso.entity.CotacaoStrategy;
import com.curso.enums.Mercado;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CotacaoFactory {

    private final Map<String, CotacaoStrategy> strategies;

    public CotacaoFactory(Map<String, CotacaoStrategy> strategies) {
        this.strategies = strategies;
    }

    public CotacaoOutputDto executar(Mercado mercado, String ticker) {
        CotacaoStrategy strategy = strategies.get(mercado.name());

        if (strategy == null) {
            throw new RuntimeException("Mercado inválido");
        }

        return strategy.buscar(ticker);
    }
}