package com.curso.exception;

public class TickerInvalidoException extends RuntimeException {
    public TickerInvalidoException(String msg) {
        super(msg);
    }
}