package com.curso.repository;

import com.curso.domains.Corretora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorretoraRepository extends JpaRepository<Corretora, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Corretora> findByCnpj(String cnpj);
}