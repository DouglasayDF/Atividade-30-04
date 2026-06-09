package com.curso.dto;

import java.math.BigDecimal;

public class SaldoOutputDto {

    private BigDecimal brl;
    private BigDecimal usd;

    public SaldoOutputDto(BigDecimal brl, BigDecimal usd) {
        this.brl = brl;
        this.usd = usd;
    }

    public BigDecimal getBrl() {
        return brl;
    }

    public void setBrl(BigDecimal brl) {
        this.brl = brl;
    }

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }
}
