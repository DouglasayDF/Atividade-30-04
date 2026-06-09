package com.curso.service;

import com.curso.domains.Acao;
import com.curso.domains.LancamentoFinanceiro;
import com.curso.domains.Operacao;
import com.curso.domains.Usuario;
import com.curso.dto.OperacaoInputDto;
import com.curso.enums.Moeda;
import com.curso.enums.TipoLancamento;
import com.curso.enums.TipoOperacao;
import com.curso.repository.AcaoRepository;
import com.curso.repository.LancamentoFinanceiroRepository;
import com.curso.repository.OperacaoRepository;
import com.curso.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private AcaoRepository acaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LancamentoFinanceiroRepository financeiroRepository;

    private OperacaoService service;

    @BeforeEach
    void setUp() {
        service = new OperacaoService(
                operacaoRepository,
                acaoRepository,
                usuarioRepository,
                financeiroRepository
        );
    }

    @Test
    void naoDeveUsarSaldoEmReaisParaComprarAcaoEmDolares() {
        prepararCompraUsd(List.of(
                lancamento(TipoLancamento.DEPOSITO, "10000.00", Moeda.BRL)
        ));

        assertThrows(RuntimeException.class, () -> service.cadastrar(compraUsd()));

        verify(financeiroRepository, never()).save(
                org.mockito.ArgumentMatchers.any(LancamentoFinanceiro.class)
        );
    }

    @Test
    void deveDebitarCompraNaMoedaDaAcao() {
        prepararCompraUsd(List.of(
                lancamento(TipoLancamento.DEPOSITO, "100.00", Moeda.USD)
        ));

        service.cadastrar(compraUsd());

        ArgumentCaptor<LancamentoFinanceiro> captor =
                ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(financeiroRepository).save(captor.capture());
        assertEquals(Moeda.USD, captor.getValue().getMoeda());
        assertEquals(TipoLancamento.COMPRA_ACAO, captor.getValue().getTipo());
        assertEquals(new BigDecimal("32.50"), captor.getValue().getValor());
    }

    @Test
    void deveVincularVendaACompraDeOrigem() {
        Acao acao = acao(1L, "PETR4", Moeda.BRL);
        Usuario usuario = usuario(1L);
        Operacao compra = operacao(
                83L, TipoOperacao.COMPRA, 1, acao, usuario, null
        );

        when(acaoRepository.findById(1L)).thenReturn(Optional.of(acao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(operacaoRepository.findByUsuarioIdAndAcaoId(1L, 1L))
                .thenReturn(List.of(compra));
        when(operacaoRepository.findById(83L)).thenReturn(Optional.of(compra));
        when(operacaoRepository.findByCompraOrigemId(83L)).thenReturn(List.of());
        when(operacaoRepository.save(any(Operacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Operacao venda = service.cadastrar(venda(83L, 1));

        assertEquals(83L, venda.getCompraOrigemId());
        assertEquals(TipoOperacao.VENDA, venda.getTipo());
    }

    @Test
    void naoDeveVenderNovamenteUmaCompraJaVendida() {
        Acao acao = acao(1L, "PETR4", Moeda.BRL);
        Usuario usuario = usuario(1L);
        Operacao compraVendida = operacao(
                83L, TipoOperacao.COMPRA, 1, acao, usuario, null
        );
        Operacao outraCompra = operacao(
                85L, TipoOperacao.COMPRA, 1, acao, usuario, null
        );
        Operacao vendaAnterior = operacao(
                84L, TipoOperacao.VENDA, 1, acao, usuario, 83L
        );

        when(acaoRepository.findById(1L)).thenReturn(Optional.of(acao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(operacaoRepository.findByUsuarioIdAndAcaoId(1L, 1L))
                .thenReturn(List.of(compraVendida, outraCompra, vendaAnterior));
        when(operacaoRepository.findById(83L))
                .thenReturn(Optional.of(compraVendida));
        when(operacaoRepository.findByCompraOrigemId(83L))
                .thenReturn(List.of(vendaAnterior));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(venda(83L, 1))
        );

        assertEquals("Esta compra já foi vendida", exception.getMessage());
        verify(financeiroRepository, never()).save(any(LancamentoFinanceiro.class));
    }

    private void prepararCompraUsd(List<LancamentoFinanceiro> lancamentos) {
        Acao acao = new Acao();
        acao.setId(2L);
        acao.setTicker("AAPL");
        acao.setMoeda(Moeda.USD);
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(acaoRepository.findById(2L)).thenReturn(Optional.of(acao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(financeiroRepository.findByUsuarioId(1L)).thenReturn(lancamentos);
    }

    private OperacaoInputDto compraUsd() {
        OperacaoInputDto dto = new OperacaoInputDto();
        dto.setAcaoId(2L);
        dto.setUsuarioId(1L);
        dto.setTipo(TipoOperacao.COMPRA);
        dto.setQuantidade(1);
        dto.setPrecoUnitario(new BigDecimal("32.50"));
        return dto;
    }

    private OperacaoInputDto venda(Long compraOrigemId, int quantidade) {
        OperacaoInputDto dto = new OperacaoInputDto();
        dto.setAcaoId(1L);
        dto.setUsuarioId(1L);
        dto.setTipo(TipoOperacao.VENDA);
        dto.setQuantidade(quantidade);
        dto.setPrecoUnitario(new BigDecimal("41.22"));
        dto.setCompraOrigemId(compraOrigemId);
        return dto;
    }

    private Acao acao(Long id, String ticker, Moeda moeda) {
        Acao acao = new Acao();
        acao.setId(id);
        acao.setTicker(ticker);
        acao.setMoeda(moeda);
        return acao;
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Operacao operacao(
            Long id,
            TipoOperacao tipo,
            int quantidade,
            Acao acao,
            Usuario usuario,
            Long compraOrigemId) {
        Operacao operacao = new Operacao();
        operacao.setId(id);
        operacao.setTipo(tipo);
        operacao.setQuantidade(quantidade);
        operacao.setPrecoUnitario(BigDecimal.ONE);
        operacao.setAcao(acao);
        operacao.setUsuario(usuario);
        operacao.setCompraOrigemId(compraOrigemId);
        return operacao;
    }

    private LancamentoFinanceiro lancamento(
            TipoLancamento tipo,
            String valor,
            Moeda moeda) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setTipo(tipo);
        lancamento.setValor(new BigDecimal(valor));
        lancamento.setMoeda(moeda);
        return lancamento;
    }
}
