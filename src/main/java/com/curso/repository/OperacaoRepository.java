package com.curso.repository;

import com.curso.domains.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacaoRepository
        extends JpaRepository<Operacao, Long> {

    boolean existsByAcaoId(Long acaoId);

    void deleteByUsuarioId(Long usuarioId);

    List<Operacao> findByAcaoId(Long acaoId);

    List<Operacao> findByUsuarioId(Long usuarioId);

    List<Operacao> findByCompraOrigemId(Long compraOrigemId);

    List<Operacao> findByUsuarioIdAndAcaoId(
            Long usuarioId,
            Long acaoId
    );
}
