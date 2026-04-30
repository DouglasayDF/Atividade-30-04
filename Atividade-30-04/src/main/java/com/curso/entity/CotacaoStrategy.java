package com.curso.entity;

import com.curso.dto.CotacaoOutputDto;

public interface CotacaoStrategy {
    CotacaoOutputDto buscar(String ticker);
}