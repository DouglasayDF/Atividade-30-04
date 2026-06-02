package com.curso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CompraInputDto {

    @NotNull
    private Long acaoId;

    @NotNull
    @Min(1)
    private Integer quantidade;

    public Long getAcaoId() {
        return acaoId;
    }

    public void setAcaoId(Long acaoId) {
        this.acaoId = acaoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}