package com.curso.service;

import com.curso.domains.Usuario;
import com.curso.repository.LancamentoFinanceiroRepository;
import com.curso.repository.OperacaoRepository;
import com.curso.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private LancamentoFinanceiroRepository financeiroRepository;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(
                usuarioRepository,
                operacaoRepository,
                financeiroRepository
        );
    }

    @Test
    void deveExcluirDadosVinculadosAntesDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(40L);
        usuario.setNome("Teste Front 37798630");
        when(usuarioRepository.findById(40L)).thenReturn(Optional.of(usuario));

        service.deletar(40L);

        InOrder ordem = inOrder(
                operacaoRepository,
                financeiroRepository,
                usuarioRepository
        );
        ordem.verify(operacaoRepository).deleteByUsuarioId(40L);
        ordem.verify(financeiroRepository).deleteByUsuarioId(40L);
        ordem.verify(usuarioRepository).delete(usuario);
    }
}
