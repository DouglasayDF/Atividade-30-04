package com.curso.service;

import com.curso.cliente.CnpjCliente;
import com.curso.cliente.ViaCepCliente;
import com.curso.domains.Corretora;
import com.curso.dto.CepOutputDto;
import com.curso.dto.CnpjClienteOutputDto;
import com.curso.dto.CorretoraInputDto;
import com.curso.dto.CorretoraPadraoDto;
import com.curso.dto.ValidacaoCorretoraDto;
import com.curso.exception.CepInvalidoException;
import com.curso.exception.CnpjInvalidoException;
import com.curso.exception.CnpjJaCadastradoException;
import com.curso.exception.CorretoraNaoEncontradaException;
import com.curso.repository.CorretoraRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Transactional
@Service
public class CorretoraService {

    private static final Map<String, CorretoraPadraoDto> CORRETORAS_PADRAO = Map.of(
            "00000000000191", new CorretoraPadraoDto(
                    "Banco do Brasil",
                    "00000000000191",
                    "01310100",
                    "122",
                    "12"
            ),
            "60746948000112", new CorretoraPadraoDto(
                    "Banco Bradesco",
                    "60746948000112",
                    "06029900",
                    "100",
                    ""
            ),
            "90400888000142", new CorretoraPadraoDto(
                    "Banco Santander",
                    "90400888000142",
                    "04543011",
                    "2236",
                    ""
            ),
            "60701190000104", new CorretoraPadraoDto(
                    "Itau Unibanco",
                    "60701190000104",
                    "04344902",
                    "3500",
                    ""
            )
    );

    private static final Set<String> TERMOS_VALIDOS = Set.of(
            "banco",
            "corretora",
            "corretora de valores",
            "distribuidora de titulos",
            "distr. de titulos",
            "dtvm",
            "ctvm",
            "invest",
            "capital",
            "asset",
            "financeira"
    );

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

    private ValidacaoCorretoraDto validarCvm(String cnpj, String razaoSocial) {
        if (cnpj != null && CORRETORAS_PADRAO.containsKey(cnpj)) {
            return new ValidacaoCorretoraDto(true, "CNPJ encontrado na lista padrão");
        }

        if (razaoSocial == null || razaoSocial.isBlank()) {
            return new ValidacaoCorretoraDto(false, "Razão social não informada pela API de CNPJ");
        }

        String rs = normalizar(razaoSocial);

        boolean termoEncontrado = TERMOS_VALIDOS.stream()
                .map(this::normalizar)
                .anyMatch(rs::contains);

        if (termoEncontrado) {
            return new ValidacaoCorretoraDto(true, "Razão social contém termo financeiro reconhecido");
        }

        return new ValidacaoCorretoraDto(false, "Não encontrada na lista padrão e sem termo financeiro reconhecido");
    }

    private String normalizar(String valor) {
        return valor == null
                ? ""
                : valor.toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("à", "a")
                .replace("ã", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c");
    }

    public Corretora cadastrar(CorretoraInputDto dto) {

        if (repository.existsByCnpj(dto.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
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

        ValidacaoCorretoraDto validacao = validarCvm(dto.getCnpj(), cnpj.getRazao_social());
        c.setValidadaNaCvm(validacao.isValida());

        return repository.save(c);
    }

    public ValidacaoCorretoraDto validar(Long id) {
        Corretora corretora = buscarPorId(id);
        return validarCvm(corretora.getCnpj(), corretora.getRazaoSocial());
    }

    public List<CorretoraPadraoDto> listarPadrao() {
        return CORRETORAS_PADRAO.values()
                .stream()
                .toList();
    }

    public List<Corretora> cadastrarPadrao() {
        return CORRETORAS_PADRAO.values()
                .stream()
                .filter(item -> !repository.existsByCnpj(item.getCnpj()))
                .map(item -> {
                    CorretoraInputDto dto = new CorretoraInputDto();
                    dto.setCnpj(item.getCnpj());
                    dto.setCep(item.getCep());
                    dto.setNumero(item.getNumero());
                    dto.setComplemento(item.getComplemento());
                    return cadastrar(dto);
                })
                .toList();
    }

    public Corretora buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new CorretoraNaoEncontradaException("Corretora não encontrada"));
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
                .orElseThrow(() ->
                        new CorretoraNaoEncontradaException("Corretora não encontrada"));

        if (!c.getAcoes().isEmpty()) {
            throw new RuntimeException("Não é possível deletar: existem ações vinculadas");
        }

        repository.delete(c);
    }
}
