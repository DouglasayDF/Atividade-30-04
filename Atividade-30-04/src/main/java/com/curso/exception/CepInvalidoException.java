package com.curso.exception;

public class CepInvalidoException extends RuntimeException {
    public CepInvalidoException(String msg) {
        super(msg);
    }
}