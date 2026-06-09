package com.curso.service;

import com.curso.domains.LancamentoFinanceiro;
import com.curso.domains.Usuario;
import com.curso.dto.DepositoDto;
import com.curso.dto.SaldoOutputDto;
import com.curso.enums.Moeda;
import com.curso.enums.TipoLancamento;
import com.curso.repository.LancamentoFinanceiroRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LancamentoFinanceiroRepository lancamentoRepository;

    private FinanceiroService service;

    @BeforeEach
    void setUp() {
        service = new FinanceiroService(usuarioRepository, lancamentoRepository);
    }

    @Test
    void deveRegistrarDepositoNaMoedaEscolhida() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        DepositoDto dto = new DepositoDto();
        dto.setValor(new BigDecimal("100.00"));
        dto.setMoeda(Moeda.USD);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        service.depositar(1L, dto);

        ArgumentCaptor<LancamentoFinanceiro> captor =
                ArgumentCaptor.forClass(LancamentoFinanceiro.class);
        verify(lancamentoRepository).save(captor.capture());
        assertEquals(Moeda.USD, captor.getValue().getMoeda());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getValor());
    }

    @Test
    void deveCalcularSaldosSeparadosPorMoeda() {
        when(lancamentoRepository.findByUsuarioId(1L)).thenReturn(List.of(
                lancamento(TipoLancamento.DEPOSITO, "100.00", null),
                lancamento(TipoLancamento.COMPRA_ACAO, "20.00", Moeda.BRL),
                lancamento(TipoLancamento.DEPOSITO, "50.00", Moeda.USD),
                lancamento(TipoLancamento.COMPRA_ACAO, "10.00", Moeda.USD)
        ));

        SaldoOutputDto saldos = service.calcularSaldos(1L);

        assertEquals(new BigDecimal("80.00"), saldos.getBrl());
        assertEquals(new BigDecimal("40.00"), saldos.getUsd());
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
