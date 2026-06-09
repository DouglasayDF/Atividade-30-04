package com.curso.service;

import com.curso.domains.Usuario;
import com.curso.dto.UsuarioInputDto;
import com.curso.repository.LancamentoFinanceiroRepository;
import com.curso.repository.OperacaoRepository;
import com.curso.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final OperacaoRepository operacaoRepository;
    private final LancamentoFinanceiroRepository financeiroRepository;

    public UsuarioService(
            UsuarioRepository repository,
            OperacaoRepository operacaoRepository,
            LancamentoFinanceiroRepository financeiroRepository) {
        this.repository = repository;
        this.operacaoRepository = operacaoRepository;
        this.financeiroRepository = financeiroRepository;
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

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        operacaoRepository.deleteByUsuarioId(id);
        financeiroRepository.deleteByUsuarioId(id);
        repository.delete(usuario);
    }
}
