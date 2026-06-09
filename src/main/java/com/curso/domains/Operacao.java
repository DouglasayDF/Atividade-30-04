package com.curso.domains;

import com.curso.enums.TipoOperacao;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacoes")
public class Operacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Acao acao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private TipoOperacao tipo;

    private Integer quantidade;

    private BigDecimal precoUnitario;

    private BigDecimal valorTotal;

    private LocalDateTime dataOperacao;

    @Column(name = "compra_origem_id")
    private Long compraOrigemId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Acao getAcao() {
        return acao;
    }

    public void setAcao(Acao acao) {
        this.acao = acao;
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

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataOperacao() {
        return dataOperacao;
    }

    public void setDataOperacao(LocalDateTime dataOperacao) {
        this.dataOperacao = dataOperacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getCompraOrigemId() {
        return compraOrigemId;
    }

    public void setCompraOrigemId(Long compraOrigemId) {
        this.compraOrigemId = compraOrigemId;
    }

    @PrePersist
    public void prePersist() {
        dataOperacao = LocalDateTime.now();

        if (precoUnitario == null || quantidade == null) {
            throw new IllegalStateException(
                    "Preço e quantidade são obrigatórios"
            );
        }

        if (valorTotal == null) {
            valorTotal = precoUnitario.multiply(
                    BigDecimal.valueOf(quantidade)
            );
        }


    }
}
