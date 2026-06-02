package com.curso.exception;

public class CorretoraNaoEncontradaException extends RuntimeException {
    public CorretoraNaoEncontradaException(String msg) {
        super(msg);
    }
}
