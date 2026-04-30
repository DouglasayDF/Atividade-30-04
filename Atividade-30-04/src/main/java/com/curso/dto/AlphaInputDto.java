package com.curso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaInputDto {

    @JsonProperty("01. symbol")
    private String simbolo;

    @JsonProperty("05. price")
    private String preco;

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }
}