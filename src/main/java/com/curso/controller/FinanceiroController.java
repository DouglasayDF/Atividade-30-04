package com.curso.controller;

import com.curso.dto.DepositoDto;
import com.curso.dto.SaldoOutputDto;
import com.curso.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody @Valid DepositoDto dto) {

        service.depositar(usuarioId, dto);
    }

    @GetMapping("/saldo/{usuarioId}")
    public SaldoOutputDto saldo(
            @PathVariable Long usuarioId) {

        return service.calcularSaldos(usuarioId);
    }
}
