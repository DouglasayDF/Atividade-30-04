package com.curso.dto;

public class CnpjClienteOutputDto {

    private String razao_social;
    private String nome_fantasia;
    private String email;
    private String telefone;
    private String cep;
    private String descricao_situacao_cadastral;



    public String getRazao_social() {
        return razao_social;
    }

    public void setRazao_social(String razao_social) {
        this.razao_social = razao_social;
    }

    public String getNome_fantasia() {
        return nome_fantasia;
    }

    public void setNome_fantasia(String nome_fantasia) {
        this.nome_fantasia = nome_fantasia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getDescricao_situacao_cadastral() {
        return descricao_situacao_cadastral;
    }

    public void setDescricao_situacao_cadastral(String descricao_situacao_cadastral) {
        this.descricao_situacao_cadastral = descricao_situacao_cadastral;
    }
}

