package com.curso.service;

import com.curso.domains.Acao;
import com.curso.domains.Corretora;
import com.curso.dto.AcaoInputDto;
import com.curso.enums.Moeda;
import com.curso.exception.AcaoNaoEncontradaException;
import com.curso.exception.MoedaInvalidaException;
import com.curso.repository.CorretoraRepository;
import com.curso.dto.CotacaoOutputDto;
import com.curso.exception.TickerInvalidoException;
import com.curso.repository.AcaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class AcaoService {

    private final AcaoRepository repository;
    private final CorretoraRepository corretoraRepository;
    private final CotacaoFactory cotacaoFactory;

    public AcaoService(AcaoRepository repository,
                       CorretoraRepository corretoraRepository,
                       CotacaoFactory cotacaoFactory) {
        this.repository = repository;
        this.corretoraRepository = corretoraRepository;
        this.cotacaoFactory = cotacaoFactory;
    }



    public List<Acao> listar() {
        return repository.findAll();
    }

    public Acao cadastrar(AcaoInputDto dto) {

        String tickerNormalizado = dto.getTicker().toUpperCase();

        if (repository.existsByTicker(tickerNormalizado)) {
            throw new TickerInvalidoException("Ticker já cadastrado");
        }

        Corretora corretora = corretoraRepository.findById(dto.getCorretoraId())
                .orElseThrow(() -> new RuntimeException("Corretora não encontrada"));

        CotacaoOutputDto cotacao = cotacaoFactory.executar(
                dto.getMercado(),
               // dto.getTicker()
                tickerNormalizado
        );

        Acao a = new Acao();

       // a.setTicker(dto.getTicker());
        a.setTicker(tickerNormalizado);
        a.setMercado(dto.getMercado());

        try {
            a.setMoeda(Moeda.valueOf(cotacao.getMoeda()));
        } catch (Exception e) {
            throw new MoedaInvalidaException("Moeda inválida da API");
        }
        a.setCotacaoAtual(cotacao.getCotacao());
        a.setNomeEmpresa(cotacao.getNomeEmpresa());
        a.setDataHoraCotacao(LocalDateTime.now());
        a.setCorretora(corretora);

        return repository.save(a);
    }

    public Acao atualizarCotacao(Long id) {

        Acao acao = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ação não encontrada"));

        CotacaoOutputDto cotacao = cotacaoFactory.executar(
                acao.getMercado(),
                acao.getTicker()
        );

        acao.setCotacaoAtual(cotacao.getCotacao());
        acao.setDataHoraCotacao(LocalDateTime.now());
        acao.setNomeEmpresa(cotacao.getNomeEmpresa());

        return repository.save(acao);

    }


    public Acao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new AcaoNaoEncontradaException("Ação não encontrada"));
    }

    public Acao buscarPorTicker(String ticker) {

        return repository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() ->
                        new AcaoNaoEncontradaException("Ação não encontrada"));
    }

    public void deletar(Long id) {
        Acao acao = repository.findById(id) .orElseThrow(() ->
                new AcaoNaoEncontradaException("Ação não encontrada"));
        repository.delete(acao); }
}
