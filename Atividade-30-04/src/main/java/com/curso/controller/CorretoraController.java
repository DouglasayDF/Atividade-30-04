package com.curso.controller;

import com.curso.dto.CorretoraInputDto;
import com.curso.dto.CorretoraOutputDto;
import com.curso.mapper.CorretoraMapper;
import com.curso.service.CorretoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corretoras")
public class CorretoraController {

    private final CorretoraService service;
    private final CorretoraMapper mapper;

    public CorretoraController(CorretoraService service,
                               CorretoraMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public CorretoraOutputDto criar(@RequestBody CorretoraInputDto dto) {
        return mapper.toDTO(service.cadastrar(dto));
    }

    @GetMapping
    public List<CorretoraOutputDto> listar() {
        return service.listar()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public CorretoraOutputDto buscarPorId(@PathVariable Long id) {
        return mapper.toDTO(service.buscarPorId(id));
    }

    @GetMapping("/cnpj/{cnpj}")
    public CorretoraOutputDto buscarPorCnpj(@PathVariable String cnpj) {
        return mapper.toDTO(service.buscarPorCnpj(cnpj));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        service.deletar(id); return ResponseEntity.ok("Corretora removida com sucesso");
    }

}