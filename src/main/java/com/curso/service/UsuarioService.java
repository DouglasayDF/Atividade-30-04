package com.curso.service;

import com.curso.domains.Usuario;
import com.curso.dto.UsuarioInputDto;
import com.curso.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario criar(UsuarioInputDto dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
    }
}
