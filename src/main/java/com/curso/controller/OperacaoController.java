package com.curso.controller;

import com.curso.domains.Operacao;
import com.curso.dto.OperacaoInputDto;
import com.curso.dto.PosicaoDto;
import com.curso.service.OperacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operacoes")
public class OperacaoController {

    private final OperacaoService service;

    public OperacaoController(OperacaoService service) {
        this.service = service;
    }

    @PostMapping
    public Operacao cadastrar(
            @RequestBody @Valid OperacaoInputDto dto) {

        return service.cadastrar(dto);
    }

    @GetMapping
    public List<Operacao> listarOperacoes() {
        return service.listarOperacoes();
    }

    @GetMapping("/posicao/{acaoId}")
    public PosicaoDto posicao(@PathVariable Long acaoId) {
        return service.calcularPosicao(acaoId);
    }

    @GetMapping("/carteira/{usuarioId}")
    public List<PosicaoDto> carteira(
            @PathVariable Long usuarioId) {

        return service.carteira(usuarioId);
    }
}

