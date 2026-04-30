package com.curso.exception;

public class CnpjInvalidoException extends RuntimeException {
    public CnpjInvalidoException(String msg) {
        super(msg);
    }
}