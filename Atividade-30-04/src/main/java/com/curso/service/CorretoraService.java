package com.curso.service;

import com.curso.cliente.CnpjCliente;
import com.curso.cliente.ViaCepCliente;
import com.curso.domains.Corretora;
import com.curso.dto.CepOutputDto;
import com.curso.dto.CnpjClienteOutputDto;
import com.curso.dto.CorretoraInputDto;
import com.curso.exception.CepInvalidoException;
import com.curso.exception.CnpjInvalidoException;
import com.curso.repository.CorretoraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorretoraService {

    private final CorretoraRepository repository;
    private final CnpjCliente cnpjCliente;
    private final ViaCepCliente viaCepClient;

    public CorretoraService(CorretoraRepository repository,
                            CnpjCliente cnpjCliente,
                            ViaCepCliente viaCepClient) {
        this.repository = repository;
        this.cnpjCliente = cnpjCliente;
        this.viaCepClient = viaCepClient;
    }

    private boolean validarCvm(String razaoSocial) {
        if (razaoSocial == null) return false;

        String rs = razaoSocial.toLowerCase();

        return rs.contains("banco")
                || rs.contains("invest")
                || rs.contains("corretora")
                || rs.contains("capital");
    }

    public Corretora cadastrar(CorretoraInputDto dto) {

        if (repository.existsByCnpj(dto.getCnpj())) {
            throw new CnpjInvalidoException("CNPJ já cadastrado");
        }

        CnpjClienteOutputDto cnpj = cnpjCliente.buscarCnpj(dto.getCnpj());

        if (cnpj == null) {
            throw new CnpjInvalidoException("CNPJ inválido");
        }

        CepOutputDto cep = viaCepClient.buscarCep(dto.getCep());

        if (cep == null || cep.getLogradouro() == null) {
            throw new CepInvalidoException("CEP inválido");
        }


        Corretora c = new Corretora();

        c.setCnpj(dto.getCnpj());
        c.setRazaoSocial(cnpj.getRazao_social());

        c.setNomeFantasia(
                cnpj.getNome_fantasia() != null
                        ? cnpj.getNome_fantasia()
                        : cnpj.getRazao_social()
        );

        c.setEmail(
                cnpj.getEmail() != null
                        ? cnpj.getEmail()
                        : "nao_informado@email.com"
        );

        c.setTelefone(
                cnpj.getTelefone() != null
                        ? cnpj.getTelefone()
                        : "000000000"
        );

        c.setCep(dto.getCep());
        c.setLogradouro(cep.getLogradouro());
        c.setBairro(cep.getBairro());
        c.setCidade(cep.getLocalidade());
        c.setUf(cep.getUf());

        c.setNumero(dto.getNumero());
        c.setComplemento(dto.getComplemento());

        c.setSituacaoCadastral(
                cnpj.getDescricao_situacao_cadastral() != null
                        ? cnpj.getDescricao_situacao_cadastral()
                        : "NAO INFORMADO"
        );

        boolean cvmOk = validarCvm(cnpj.getRazao_social());
        c.setValidadaNaCvm(cvmOk);

        return repository.save(c);
    }

    public Corretora buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretora não encontrada"));
    }

    public Corretora buscarPorCnpj(String cnpj) {
        return repository.findByCnpj(cnpj)
                .orElseThrow(() ->
                        new CnpjInvalidoException("CNPJ não encontrado"));
    }

    public List<Corretora> listar() {
        return repository.findAll();
    }


    public void deletar(Long id) {

        Corretora c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretora não encontrada"));

        if (!c.getAcoes().isEmpty()) {
            throw new RuntimeException("Não é possível deletar: existem ações vinculadas");
        }

        repository.delete(c);
    }
}