package com.curso.strategy;

import com.curso.dto.CotacaoOutputDto;

public interface CotacaoStrategy {
    CotacaoOutputDto buscar(String ticker);
}