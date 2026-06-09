package com.curso.repository;

import com.curso.domains.HistoricoCotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoCotacaoRepository
        extends JpaRepository<HistoricoCotacao, Long> {

    boolean existsByAcaoId(Long acaoId);

    List<HistoricoCotacao> findByAcaoIdOrderByDataHoraCotacaoDescIdDesc(Long acaoId);

    void deleteByAcaoId(Long acaoId);
}
