package com.curso.exception;

public class MoedaInvalidaException extends RuntimeException {
    public MoedaInvalidaException(String msg) {
        super(msg);
    }
}
