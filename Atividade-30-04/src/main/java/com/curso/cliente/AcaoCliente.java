package com.curso.cliente;

public interface AcaoCliente<T> {
    T buscarCotacao(String ticker);
}