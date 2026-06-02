package com.curso.dto;

import jakarta.validation.constraints.NotBlank;

public class UsuarioInputDto {

    @NotBlank
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
