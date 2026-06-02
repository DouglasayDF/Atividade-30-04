    package com.curso.controller;

    import com.curso.domains.Compra;
    import com.curso.dto.CompraInputDto;
    import com.curso.service.CompraService;
    import jakarta.validation.Valid;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/compras")
    public class CompraController {

        private final CompraService service;

        public CompraController(CompraService service) {
            this.service = service;
        }

        @PostMapping
        public Compra comprar(
                @RequestBody @Valid CompraInputDto dto) {

            return service.comprar(dto);
        }
    }