package com.curso.controller;

import com.curso.domains.Acao;
import com.curso.dto.AcaoInputDto;
import com.curso.dto.AcaoOutputDto;
import com.curso.mapper.AcaoMapper;
import com.curso.service.AcaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acoes")
public class AcaoController {

    private final AcaoService service;
    private final AcaoMapper mapper;

    public AcaoController(AcaoService service, AcaoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public AcaoOutputDto criar(@RequestBody @Valid AcaoInputDto dto) {
        Acao acao = service.cadastrar(dto);
        return mapper.toDTO(acao);
    }
    @GetMapping
    public List<AcaoOutputDto> listar() {
        return service.listar()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public AcaoOutputDto buscarPorId(@PathVariable Long id) {
        return mapper.toDTO(service.buscarPorId(id));
    }

    @GetMapping("/ticker/{ticker}")
    public AcaoOutputDto buscarPorTicker(@PathVariable String ticker) {
        return mapper.toDTO(service.buscarPorTicker(ticker));
    }

    @PutMapping("/{id}/atualizar-cotacao")
    public AcaoOutputDto atualizar(@PathVariable Long id) {
        return mapper.toDTO(service.atualizarCotacao(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        service.deletar(id); return ResponseEntity.ok("Ação removida com sucesso");
    }
}