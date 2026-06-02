package com.curso.repository;

import com.curso.domains.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacaoRepository
        extends JpaRepository<Operacao, Long> {

    List<Operacao> findByAcaoId(Long acaoId);

    List<Operacao> findByUsuarioId(Long usuarioId);

    List<Operacao> findByUsuarioIdAndAcaoId(
            Long usuarioId,
            Long acaoId
    );
}