package com.curso.repository;

import com.curso.domains.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository
        extends JpaRepository<Compra, Long> {
}