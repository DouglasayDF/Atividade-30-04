package com.curso.dto;

public class ValidacaoCorretoraDto {

    private boolean valida;
    private String motivo;

    public ValidacaoCorretoraDto(boolean valida, String motivo) {
        this.valida = valida;
        this.motivo = motivo;
    }

    public boolean isValida() {
        return valida;
    }

    public String getMotivo() {
        return motivo;
    }
}
