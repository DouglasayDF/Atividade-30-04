package com.curso.service;

import com.curso.cliente.BrapiCliente;
import com.curso.domains.Acao;
import com.curso.domains.HistoricoCotacao;
import com.curso.dto.AcaoInputDto;
import com.curso.dto.BrapiListResponseDto;
import com.curso.dto.HistoricoCotacaoOutputDto;
import com.curso.enums.Mercado;
import com.curso.enums.Moeda;
import com.curso.exception.AcaoEmUsoException;
import com.curso.exception.AcaoNaoEncontradaException;
import com.curso.exception.MoedaInvalidaException;
import com.curso.dto.CotacaoOutputDto;
import com.curso.exception.TickerInvalidoException;
import com.curso.repository.AcaoRepository;
import com.curso.repository.HistoricoCotacaoRepository;
import com.curso.repository.OperacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Transactional
@Service
public class AcaoService {

    private static final int LIMITE_HISTORICO = 5;

    private final AcaoRepository repository;
    private final OperacaoRepository operacaoRepository;
    private final HistoricoCotacaoRepository historicoRepository;
    private final CotacaoFactory cotacaoFactory;
    private final BrapiCliente brapiCliente;

    public AcaoService(AcaoRepository repository,
                       OperacaoRepository operacaoRepository,
                       HistoricoCotacaoRepository historicoRepository,
                       CotacaoFactory cotacaoFactory,
                       BrapiCliente brapiCliente) {
        this.repository = repository;
        this.operacaoRepository = operacaoRepository;
        this.historicoRepository = historicoRepository;
        this.cotacaoFactory = cotacaoFactory;
        this.brapiCliente = brapiCliente;
    }



    public List<Acao> listar() {
        return repository.findAll();
    }

    public BrapiListResponseDto listarAcoesBrapi(
            String search,
            String sortBy,
            String sortOrder,
            Integer limit,
            Integer page,
            String sector,
            String type,
            String subType,
            String token) {
        return brapiCliente.listarAcoes(
                search,
                sortBy,
                sortOrder,
                limit,
                page,
                sector,
                type,
                subType,
                token
        );
    }

    private Mercado inferirMercadoPorTicker(String ticker, Mercado mercadoInformado) {
        String tickerNormalizado = ticker == null
                ? ""
                : ticker.trim().toUpperCase(Locale.ROOT);

        if (tickerNormalizado.matches("^[A-Z]{4}\\d{1,2}[A-Z]?$") ||
                tickerNormalizado.matches("^[A-Z0-9]+\\.SA$")) {
            return Mercado.BR;
        }

        if (tickerNormalizado.matches("^[A-Z]{1,5}([.-][A-Z])?$")) {
            return Mercado.US;
        }

        return mercadoInformado != null ? mercadoInformado : Mercado.BR;
    }

    public Acao cadastrar(AcaoInputDto dto) {

        String tickerNormalizado = dto.getTicker().trim().toUpperCase(Locale.ROOT);
        Mercado mercado = inferirMercadoPorTicker(tickerNormalizado, dto.getMercado());

        if (repository.existsByTicker(tickerNormalizado)) {
            throw new TickerInvalidoException("Ticker já cadastrado");
        }


        CotacaoOutputDto cotacao = cotacaoFactory.executar(
                mercado,
               // dto.getTicker()
                tickerNormalizado
        );

        Acao a = new Acao();

       // a.setTicker(dto.getTicker());
        a.setTicker(tickerNormalizado);
        a.setMercado(mercado);

        try {
            a.setMoeda(Moeda.valueOf(cotacao.getMoeda()));
        } catch (Exception e) {
            throw new MoedaInvalidaException("Moeda inválida da API");
        }
        a.setCotacaoAtual(cotacao.getCotacao());
        a.setNomeEmpresa(cotacao.getNomeEmpresa());
        a.setDataHoraCotacao(LocalDateTime.now());

        return repository.save(a);
    }

    public Acao atualizarCotacao(Long id) {

        Acao acao = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ação não encontrada"));

        CotacaoOutputDto cotacao = cotacaoFactory.executar(
                acao.getMercado(),
                acao.getTicker()
        );

        salvarHistorico(acao);

        acao.setCotacaoAtual(cotacao.getCotacao());
        acao.setDataHoraCotacao(LocalDateTime.now());
        acao.setNomeEmpresa(cotacao.getNomeEmpresa());

        return repository.save(acao);

    }

    public List<HistoricoCotacaoOutputDto> listarHistorico(Long id) {
        buscarPorId(id);

        return historicoRepository
                .findByAcaoIdOrderByDataHoraCotacaoDescIdDesc(id)
                .stream()
                .map(this::toHistoricoDto)
                .toList();
    }

    private void salvarHistorico(Acao acao) {
        HistoricoCotacao historico = new HistoricoCotacao();
        historico.setAcao(acao);
        historico.setCotacao(acao.getCotacaoAtual());
        historico.setDataHoraCotacao(acao.getDataHoraCotacao());
        historicoRepository.save(historico);

        List<HistoricoCotacao> registros =
                historicoRepository.findByAcaoIdOrderByDataHoraCotacaoDescIdDesc(acao.getId());

        if (registros.size() > LIMITE_HISTORICO) {
            historicoRepository.deleteAll(registros.subList(LIMITE_HISTORICO, registros.size()));
        }
    }

    private HistoricoCotacaoOutputDto toHistoricoDto(HistoricoCotacao historico) {
        HistoricoCotacaoOutputDto dto = new HistoricoCotacaoOutputDto();
        dto.setId(historico.getId());
        dto.setCotacao(historico.getCotacao());
        dto.setDataHoraCotacao(historico.getDataHoraCotacao());
        return dto;
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

        if (operacaoRepository.existsByAcaoId(id)) {
            throw new AcaoEmUsoException(
                    "Não é possível excluir a ação " + acao.getTicker()
                            + ": existem operações vinculadas"
            );
        }

        historicoRepository.deleteByAcaoId(id);
        repository.delete(acao);
    }
}
