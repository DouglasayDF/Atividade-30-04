package com.curso.dto;

public class CorretoraPadraoDto {

    private String nome;
    private String cnpj;
    private String cep;
    private String numero;
    private String complemento;

    public CorretoraPadraoDto(String nome, String cnpj, String cep, String numero, String complemento) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getCep() {
        return cep;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }
}
