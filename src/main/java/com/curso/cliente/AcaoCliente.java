package com.curso.cliente;

import com.curso.dto.AlphaOverviewDto;

public interface AcaoCliente<T> {

    T buscarCotacao(String ticker);
}