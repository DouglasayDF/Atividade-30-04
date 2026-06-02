package com.curso.dto;

import com.curso.enums.TipoOperacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class OperacaoInputDto {

    @NotNull
    private Long acaoId;

    @NotNull
    private TipoOperacao tipo;

    @NotNull
    @Min(1)
    private Integer quantidade;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal precoUnitario;

    @NotNull
    private Long usuarioId;

    public Long getAcaoId() {
        return acaoId;
    }

    public void setAcaoId(Long acaoId) {
        this.acaoId = acaoId;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOperacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}