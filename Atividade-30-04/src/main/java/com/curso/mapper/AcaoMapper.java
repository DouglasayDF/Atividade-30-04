package com.curso.mapper;

import com.curso.domains.Acao;
import com.curso.dto.AcaoOutputDto;
import org.springframework.stereotype.Component;

@Component
public class AcaoMapper {

    public AcaoOutputDto toDTO(Acao a) {
        AcaoOutputDto dto = new AcaoOutputDto();
        dto.setId(a.getId());
        dto.setTicker(a.getTicker());
        dto.setNomeEmpresa(a.getNomeEmpresa());
        dto.setMercado(a.getMercado());
        dto.setMoeda(a.getMoeda());
        dto.setCotacaoAtual(a.getCotacaoAtual());
        dto.setDataHoraCotacao(a.getDataHoraCotacao());
        return dto;
    }
}