package com.curso.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CnpjInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCnpj(CnpjInvalidoException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCep(CepInvalidoException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TickerInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTicker(TickerInvalidoException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(mensagem));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGeneric(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erro interno: " + ex.getMessage()));
    }
    @ExceptionHandler(CorretoraNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCorretoraNaoEncontrada(
            CorretoraNaoEncontradaException ex) {

        return ResponseEntity.status(404)
                .body(new ErrorResponse(ex.getMessage()));
    }
    @ExceptionHandler(AcaoNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleAcaoNaoEncontrada(
            AcaoNaoEncontradaException ex) {

        return ResponseEntity.status(404)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MoedaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleMoeda(
            MoedaInvalidaException ex) {

        return ResponseEntity.status(404)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
