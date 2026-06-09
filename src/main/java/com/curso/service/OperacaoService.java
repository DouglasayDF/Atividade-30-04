package com.curso.service;

import com.curso.domains.Acao;
import com.curso.domains.LancamentoFinanceiro;
import com.curso.domains.Operacao;
import com.curso.domains.Usuario;
import com.curso.dto.OperacaoInputDto;
import com.curso.dto.PosicaoDto;
import com.curso.enums.Moeda;
import com.curso.enums.TipoLancamento;
import com.curso.enums.TipoOperacao;
import com.curso.repository.AcaoRepository;
import com.curso.repository.LancamentoFinanceiroRepository;
import com.curso.repository.OperacaoRepository;
import com.curso.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class OperacaoService {

    public List<Operacao> listarOperacoes() {
        return repository.findAll();
    }

    private final OperacaoRepository repository;
    private final AcaoRepository acaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoFinanceiroRepository financeiroRepository;
    private BigDecimal calcularSaldo(Long usuarioId, Moeda moeda) {

        List<LancamentoFinanceiro> lancamentos =
                financeiroRepository.findByUsuarioId(usuarioId);

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

    public OperacaoService(
            OperacaoRepository repository,
            AcaoRepository acaoRepository, UsuarioRepository usuarioRepository, LancamentoFinanceiroRepository financeiroRepository) {

        this.repository = repository;
        this.acaoRepository = acaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.financeiroRepository = financeiroRepository;
    }

    public Operacao cadastrar(OperacaoInputDto dto) {

        Acao acao = acaoRepository.findById(dto.getAcaoId())
                .orElseThrow();

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow();

        BigDecimal valorOperacao =
                dto.getPrecoUnitario()
                        .multiply(
                                BigDecimal.valueOf(
                                        dto.getQuantidade()
                                )
                        );

        if (dto.getTipo() == TipoOperacao.COMPRA) {

            BigDecimal saldo =
                    calcularSaldo(usuario.getId(), acao.getMoeda());

            if (saldo.compareTo(valorOperacao) < 0) {
                throw new RuntimeException(
                        "Saldo insuficiente em " + acao.getMoeda()
                );
            }

            LancamentoFinanceiro lancamento =
                    new LancamentoFinanceiro();

            lancamento.setUsuario(usuario);
            lancamento.setTipo(TipoLancamento.COMPRA_ACAO);
            lancamento.setValor(valorOperacao);
            lancamento.setMoeda(acao.getMoeda());
            lancamento.setDescricao(
                    "Compra de " + acao.getTicker()
            );

            financeiroRepository.save(lancamento);
        }

        if (dto.getTipo() == TipoOperacao.VENDA) {

            int quantidadeAtual = calcularQuantidadeAtual(
                    usuario.getId(),
                    acao.getId()
            );

            if (quantidadeAtual < dto.getQuantidade()) {
                throw new RuntimeException(
                        "Quantidade insuficiente para venda"
                );
            }

            Operacao compraOrigem = validarCompraOrigem(dto, usuario, acao);

            LancamentoFinanceiro lancamento =
                    new LancamentoFinanceiro();

            lancamento.setUsuario(usuario);
            lancamento.setTipo(TipoLancamento.VENDA_ACAO);
            lancamento.setValor(valorOperacao);
            lancamento.setMoeda(acao.getMoeda());
            lancamento.setDescricao(
                    "Venda de " + acao.getTicker()
            );

            financeiroRepository.save(lancamento);

            dto.setCompraOrigemId(compraOrigem.getId());
        }

        Operacao op = new Operacao();

        op.setUsuario(usuario);
        op.setAcao(acao);
        op.setTipo(dto.getTipo());
        op.setQuantidade(dto.getQuantidade());
        op.setPrecoUnitario(dto.getPrecoUnitario());
        op.setCompraOrigemId(dto.getCompraOrigemId());

        return repository.save(op);
    }

    private Operacao validarCompraOrigem(
            OperacaoInputDto dto,
            Usuario usuario,
            Acao acao) {

        if (dto.getCompraOrigemId() == null) {
            throw new RuntimeException(
                    "Selecione uma compra disponível para realizar a venda"
            );
        }

        Operacao compra = repository.findById(dto.getCompraOrigemId())
                .orElseThrow(() -> new RuntimeException(
                        "Compra de origem não encontrada"
                ));

        boolean compraValida =
                compra.getTipo() == TipoOperacao.COMPRA
                        && compra.getUsuario() != null
                        && compra.getAcao() != null
                        && compra.getUsuario().getId().equals(usuario.getId())
                        && compra.getAcao().getId().equals(acao.getId());

        if (!compraValida) {
            throw new RuntimeException(
                    "A compra selecionada não pertence a esta carteira ou ação"
            );
        }

        int quantidadeVendida = repository
                .findByCompraOrigemId(compra.getId())
                .stream()
                .filter(operacao -> operacao.getTipo() == TipoOperacao.VENDA)
                .mapToInt(Operacao::getQuantidade)
                .sum();
        int quantidadeRestante = compra.getQuantidade() - quantidadeVendida;

        if (quantidadeRestante <= 0) {
            throw new RuntimeException("Esta compra já foi vendida");
        }

        if (dto.getQuantidade() > quantidadeRestante) {
            throw new RuntimeException(
                    "Quantidade disponível nesta compra: " + quantidadeRestante
            );
        }

        return compra;
    }

    private int calcularQuantidadeAtual(Long usuarioId, Long acaoId) {

        List<Operacao> operacoes =
                repository.findByUsuarioIdAndAcaoId(usuarioId, acaoId);

        int quantidadeAtual = 0;

        for (Operacao op : operacoes) {
            if (op.getTipo() == TipoOperacao.COMPRA) {
                quantidadeAtual += op.getQuantidade();
            } else {
                quantidadeAtual -= op.getQuantidade();
            }
        }

        return quantidadeAtual;
    }

    public PosicaoDto calcularPosicao(Long acaoId) {

        List<Operacao> operacoes =
                repository.findByAcaoId(acaoId);

        return calcularPosicao(operacoes, acaoId);
    }

    private PosicaoDto calcularPosicao(List<Operacao> operacoes, Long acaoId) {

        if (operacoes.isEmpty()) {
            throw new RuntimeException(
                    "Não existem operações para esta ação"
            );
        }

        Acao acao = acaoRepository.findById(acaoId)
                .orElseThrow();

        int quantidadeAtual = 0;

        BigDecimal custoTotalCompras = BigDecimal.ZERO;

        int quantidadeComprada = 0;

        for (Operacao op : operacoes) {

            if (op.getTipo() == TipoOperacao.COMPRA) {

                quantidadeAtual += op.getQuantidade();

                quantidadeComprada += op.getQuantidade();

                custoTotalCompras =
                        custoTotalCompras.add(
                                op.getPrecoUnitario()
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        op.getQuantidade()
                                                )
                                        )
                        );

            } else {

                quantidadeAtual -= op.getQuantidade();
            }
        }

        if (quantidadeAtual < 0) {
            throw new RuntimeException(
                    "Posição inválida: vendas maiores que compras"
            );
        }

        if (quantidadeComprada == 0) {
            throw new RuntimeException(
                    "Nenhuma compra encontrada"
            );
        }

        BigDecimal precoMedio =
                custoTotalCompras.divide(
                        BigDecimal.valueOf(quantidadeComprada),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal valorInvestido =
                precoMedio.multiply(BigDecimal.valueOf(quantidadeAtual)
                );

        BigDecimal valorAtual =
                acao.getCotacaoAtual()
                        .multiply(BigDecimal.valueOf(quantidadeAtual)
                        );

        BigDecimal lucroPrejuizo = valorAtual.subtract(valorInvestido);

        BigDecimal rentabilidade = BigDecimal.ZERO;

        if (valorInvestido.compareTo(BigDecimal.ZERO) > 0) {

            rentabilidade =
                    lucroPrejuizo.multiply(BigDecimal.valueOf(100))
                            .divide(valorInvestido, 2, RoundingMode.HALF_UP);
        }

        PosicaoDto dto = new PosicaoDto();

        dto.setTicker(acao.getTicker());
        dto.setNomeEmpresa(acao.getNomeEmpresa());
        dto.setQuantidade(quantidadeAtual);
        dto.setPrecoMedio(precoMedio);
        dto.setValorInvestido(valorInvestido);
        dto.setCotacaoAtual(acao.getCotacaoAtual());
        dto.setValorAtual(valorAtual);
        dto.setLucroPrejuizo(lucroPrejuizo);
        dto.setRentabilidade(rentabilidade);

        return dto;
    }

    public List<PosicaoDto> carteira(Long usuarioId) {



        List<Operacao> operacoes =
                repository.findByUsuarioId(usuarioId);

        List<Long> idsAcoes =
                operacoes.stream()
                        .map(op -> op.getAcao().getId())
                        .distinct()
                        .toList();

        return idsAcoes.stream()
                .map(acaoId -> calcularPosicao(
                        repository.findByUsuarioIdAndAcaoId(usuarioId, acaoId),
                        acaoId
                ))
                .toList();


    }
}
