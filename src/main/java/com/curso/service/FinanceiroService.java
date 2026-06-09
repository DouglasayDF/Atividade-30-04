package com.curso.service;

import com.curso.domains.LancamentoFinanceiro;
import com.curso.domains.Usuario;
import com.curso.dto.DepositoDto;
import com.curso.dto.SaldoOutputDto;
import com.curso.enums.Moeda;
import com.curso.enums.TipoLancamento;
import com.curso.repository.LancamentoFinanceiroRepository;
import com.curso.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinanceiroService {

    private final UsuarioRepository usuarioRepository;
    private final LancamentoFinanceiroRepository repository;

    public FinanceiroService(
            UsuarioRepository usuarioRepository,
            LancamentoFinanceiroRepository repository) {

        this.usuarioRepository = usuarioRepository;
        this.repository = repository;
    }

    public void depositar(Long usuarioId, DepositoDto dto) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        LancamentoFinanceiro lancamento =
                new LancamentoFinanceiro();

        lancamento.setUsuario(usuario);
        lancamento.setTipo(TipoLancamento.DEPOSITO);
        lancamento.setValor(dto.getValor());
        lancamento.setMoeda(dto.getMoeda());
        lancamento.setDescricao("Depósito em " + dto.getMoeda());

        repository.save(lancamento);
    }

    public SaldoOutputDto calcularSaldos(Long usuarioId) {
        return new SaldoOutputDto(
                calcularSaldo(usuarioId, Moeda.BRL),
                calcularSaldo(usuarioId, Moeda.USD)
        );
    }

    public BigDecimal calcularSaldo(Long usuarioId, Moeda moeda) {

        List<LancamentoFinanceiro> lancamentos =
                repository.findByUsuarioId(usuarioId);

        BigDecimal saldo = BigDecimal.ZERO;

        for (LancamentoFinanceiro l : lancamentos) {
            Moeda moedaLancamento = l.getMoeda() == null ? Moeda.BRL : l.getMoeda();
            if (moedaLancamento != moeda) {
                continue;
            }

            switch (l.getTipo()) {

                case DEPOSITO:
                case VENDA_ACAO:
                    saldo = saldo.add(l.getValor());
                    break;

                case SAQUE:
                case COMPRA_ACAO:
                    saldo = saldo.subtract(l.getValor());
                    break;
            }
        }

        return saldo;
    }
}
