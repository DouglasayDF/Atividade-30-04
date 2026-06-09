package com.curso.controller;

import com.curso.domains.Acao;
import com.curso.dto.AcaoInputDto;
import com.curso.dto.AcaoOutputDto;
import com.curso.dto.BrapiListResponseDto;
import com.curso.dto.HistoricoCotacaoOutputDto;
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

    @GetMapping("/brapi/list")
    public BrapiListResponseDto listarAcoesBrapi(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String subType,
            @RequestParam(required = false) String token) {
        return service.listarAcoesBrapi(
                search,
                sortBy,
                sortOrder,
                limit,
                page,
                sector,
                type,
                subType,
                token
        );
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

    @GetMapping("/{id}/historico")
    public List<HistoricoCotacaoOutputDto> listarHistorico(@PathVariable Long id) {
        return service.listarHistorico(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        service.deletar(id); return ResponseEntity.ok("Ação removida com sucesso");
    }
}
