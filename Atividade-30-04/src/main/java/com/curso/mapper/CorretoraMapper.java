package com.curso.mapper;

import com.curso.domains.Corretora;
import com.curso.dto.CorretoraOutputDto;
import org.springframework.stereotype.Component;

@Component
public class CorretoraMapper {

    public CorretoraOutputDto toDTO(Corretora c) {

        CorretoraOutputDto dto = new CorretoraOutputDto();

        dto.setId(c.getId());
        dto.setCnpj(c.getCnpj());
        dto.setNomeFantasia(c.getNomeFantasia());
        dto.setRazaoSocial(c.getRazaoSocial());
        dto.setEmail(c.getEmail());
        dto.setTelefone(c.getTelefone());

        dto.setCep(c.getCep());
        dto.setLogradouro(c.getLogradouro());
        dto.setNumero(c.getNumero());
        dto.setComplemento(c.getComplemento());
        dto.setBairro(c.getBairro());
        dto.setCidade(c.getCidade());
        dto.setUf(c.getUf());

        dto.setSituacaoCadastral(c.getSituacaoCadastral());
        dto.setValidadaNaCvm(c.getValidadaNaCvm());
        dto.setDataCadastro(c.getDataCadastro());

        return dto;
    }
}