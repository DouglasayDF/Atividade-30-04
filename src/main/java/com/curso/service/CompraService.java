package com.curso.service;

import com.curso.domains.Acao;
import com.curso.domains.Compra;
import com.curso.dto.CompraInputDto;
import com.curso.dto.CotacaoOutputDto;
import com.curso.repository.AcaoRepository;
import com.curso.repository.CompraRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CompraService {

    private final AcaoRepository acaoRepository;
    private final CompraRepository compraRepository;
    private final CotacaoFactory cotacaoFactory;

    public CompraService(
            AcaoRepository acaoRepository,
            CompraRepository compraRepository,
            CotacaoFactory cotacaoFactory) {

        this.acaoRepository = acaoRepository;
        this.compraRepository = compraRepository;
        this.cotacaoFactory = cotacaoFactory;
    }

    public Compra comprar(CompraInputDto dto) {

        Acao acao = acaoRepository.findById(dto.getAcaoId())
                .orElseThrow(() ->
                        new RuntimeException("Ação não encontrada"));

        CotacaoOutputDto cotacao =
                cotacaoFactory.executar(
                        acao.getMercado(),
                        acao.getTicker());

        Compra compra = new Compra();

        compra.setAcao(acao);
        compra.setQuantidade(dto.getQuantidade());
        compra.setPrecoCompra(cotacao.getCotacao());

        return compraRepository.save(compra);
    }
}