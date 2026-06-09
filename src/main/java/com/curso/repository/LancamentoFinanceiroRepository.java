package com.curso.repository;

import com.curso.domains.LancamentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LancamentoFinanceiroRepository
        extends JpaRepository<LancamentoFinanceiro, Long> {

    void deleteByUsuarioId(Long usuarioId);

    List<LancamentoFinanceiro> findByUsuarioId(Long usuarioId);
}
