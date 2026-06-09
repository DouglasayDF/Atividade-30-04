package com.curso.exception;

public class AcaoEmUsoException extends RuntimeException {

    public AcaoEmUsoException(String message) {
        super(message);
    }
}
