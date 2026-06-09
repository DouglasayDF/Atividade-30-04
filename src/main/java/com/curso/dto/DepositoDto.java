package com.curso.dto;

import com.curso.enums.Moeda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositoDto {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;

    @NotNull
    private Moeda moeda;

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public void setMoeda(Moeda moeda) {
        this.moeda = moeda;
    }
}
