package com.curso.controller;

import com.curso.dto.DepositoDto;
import com.curso.service.FinanceiroService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final FinanceiroService service;

    public FinanceiroController(
            FinanceiroService service) {

        this.service = service;
    }

    @PostMapping("/deposito/{usuarioId}")
    public void depositar(
            @PathVariable Long usuarioId,
            @RequestBody DepositoDto dto) {

        service.depositar(usuarioId, dto);
    }

    @GetMapping("/saldo/{usuarioId}")
    public BigDecimal saldo(
            @PathVariable Long usuarioId) {

        return service.calcularSaldo(usuarioId);
    }
}