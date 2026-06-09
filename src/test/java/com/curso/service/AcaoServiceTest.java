package com.curso.service;

import com.curso.cliente.BrapiCliente;
import com.curso.domains.Acao;
import com.curso.domains.HistoricoCotacao;
import com.curso.dto.CotacaoOutputDto;
import com.curso.enums.Mercado;
import com.curso.exception.AcaoEmUsoException;
import com.curso.repository.AcaoRepository;
import com.curso.repository.HistoricoCotacaoRepository;
import com.curso.repository.OperacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcaoServiceTest {

    @Mock
    private AcaoRepository acaoRepository;

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private HistoricoCotacaoRepository historicoRepository;

    @Mock
    private CotacaoFactory cotacaoFactory;

    @Mock
    private BrapiCliente brapiCliente;

    private AcaoService service;

    @BeforeEach
    void setUp() {
        service = new AcaoService(
                acaoRepository,
                operacaoRepository,
                historicoRepository,
                cotacaoFactory,
                brapiCliente
        );
    }

    @Test
    void naoDeveExcluirAcaoComOperacoesVinculadas() {
        Acao acao = new Acao();
        acao.setId(9L);
        acao.setTicker("TSLA");

        when(acaoRepository.findById(9L)).thenReturn(Optional.of(acao));
        when(operacaoRepository.existsByAcaoId(9L)).thenReturn(true);

        assertThrows(AcaoEmUsoException.class, () -> service.deletar(9L));

        verify(acaoRepository, never()).delete(acao);
    }

    @Test
    void deveExcluirAcaoSemOperacoesVinculadas() {
        Acao acao = new Acao();
        acao.setId(10L);
        acao.setTicker("MSFT");

        when(acaoRepository.findById(10L)).thenReturn(Optional.of(acao));
        when(operacaoRepository.existsByAcaoId(10L)).thenReturn(false);

        service.deletar(10L);

        verify(historicoRepository).deleteByAcaoId(10L);
        verify(acaoRepository).delete(acao);
    }

    @Test
    void deveSalvarValorAnteriorAoAtualizarCotacao() {
        LocalDateTime dataAnterior = LocalDateTime.of(2026, 6, 9, 9, 50);
        Acao acao = criarAcao(1L, "PETR4", new BigDecimal("41.22"), dataAnterior);
        CotacaoOutputDto novaCotacao = new CotacaoOutputDto();
        novaCotacao.setCotacao(new BigDecimal("42.10"));
        novaCotacao.setNomeEmpresa("Petrobras PN");

        when(acaoRepository.findById(1L)).thenReturn(Optional.of(acao));
        when(cotacaoFactory.executar(Mercado.BR, "PETR4")).thenReturn(novaCotacao);
        when(historicoRepository.findByAcaoIdOrderByDataHoraCotacaoDescIdDesc(1L))
                .thenReturn(List.of(new HistoricoCotacao()));
        when(acaoRepository.save(acao)).thenReturn(acao);

        service.atualizarCotacao(1L);

        var captor = org.mockito.ArgumentCaptor.forClass(HistoricoCotacao.class);
        verify(historicoRepository).save(captor.capture());
        assertEquals(new BigDecimal("41.22"), captor.getValue().getCotacao());
        assertEquals(dataAnterior, captor.getValue().getDataHoraCotacao());
        assertEquals(new BigDecimal("42.10"), acao.getCotacaoAtual());
    }

    @Test
    void deveManterSomenteCincoRegistrosNoHistorico() {
        Acao acao = criarAcao(
                1L,
                "PETR4",
                new BigDecimal("41.22"),
                LocalDateTime.of(2026, 6, 9, 9, 50)
        );
        CotacaoOutputDto novaCotacao = new CotacaoOutputDto();
        novaCotacao.setCotacao(new BigDecimal("42.10"));
        novaCotacao.setNomeEmpresa("Petrobras PN");
        List<HistoricoCotacao> seisRegistros = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            seisRegistros.add(new HistoricoCotacao());
        }

        when(acaoRepository.findById(1L)).thenReturn(Optional.of(acao));
        when(cotacaoFactory.executar(Mercado.BR, "PETR4")).thenReturn(novaCotacao);
        when(historicoRepository.findByAcaoIdOrderByDataHoraCotacaoDescIdDesc(1L))
                .thenReturn(seisRegistros);
        when(acaoRepository.save(acao)).thenReturn(acao);

        service.atualizarCotacao(1L);

        verify(historicoRepository).deleteAll(eq(seisRegistros.subList(5, 6)));
    }

    private Acao criarAcao(
            Long id,
            String ticker,
            BigDecimal cotacao,
            LocalDateTime dataHoraCotacao) {
        Acao acao = new Acao();
        acao.setId(id);
        acao.setTicker(ticker);
        acao.setMercado(Mercado.BR);
        acao.setCotacaoAtual(cotacao);
        acao.setDataHoraCotacao(dataHoraCotacao);
        return acao;
    }
}
