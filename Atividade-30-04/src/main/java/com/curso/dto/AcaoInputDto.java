package com.curso.dto;

import com.curso.enums.Mercado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AcaoInputDto {


    @NotBlank
    private String ticker;

    @NotNull
    private Mercado mercado;

    @NotNull
    private Long corretoraId;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public void setMercado(Mercado mercado) {
        this.mercado = mercado;
    }

    public Long getCorretoraId() {
        return corretoraId;
    }

    public void setCorretoraId(Long corretoraId) {
        this.corretoraId = corretoraId;
    }
}