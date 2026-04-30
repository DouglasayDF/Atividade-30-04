package com.curso.domains;

import com.curso.enums.Mercado;
import com.curso.enums.Moeda;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "acoes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "ticker")
})
public class Acao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretora;

    @Column(nullable = false)
    @NotBlank
    private String ticker;

    @Column(nullable = false)
    private String nomeEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mercado mercado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Moeda moeda;

    @Column(nullable = false)
    private BigDecimal cotacaoAtual;

    @Column(nullable = false)
    private LocalDateTime dataHoraCotacao;

    @PrePersist
    public void prePersist() {
        this.dataHoraCotacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Corretora getCorretora() {
        return corretora;
    }

    public void setCorretora(Corretora corretora) {
        this.corretora = corretora;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public void setMercado(Mercado mercado) {
        this.mercado = mercado;
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public void setMoeda(Moeda moeda) {
        this.moeda = moeda;
    }

    public BigDecimal getCotacaoAtual() {
        return cotacaoAtual;
    }

    public void setCotacaoAtual(BigDecimal cotacaoAtual) {
        this.cotacaoAtual = cotacaoAtual;
    }

    public LocalDateTime getDataHoraCotacao() {
        return dataHoraCotacao;
    }

    public void setDataHoraCotacao(LocalDateTime dataHoraCotacao) {
        this.dataHoraCotacao = dataHoraCotacao;
    }


}