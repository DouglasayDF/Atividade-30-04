package com.curso.mapper;

import com.curso.domains.Acao;
import com.curso.dto.AcaoOutputDto;
import com.curso.repository.HistoricoCotacaoRepository;
import org.springframework.stereotype.Component;

@Component
public class AcaoMapper {

    private final HistoricoCotacaoRepository historicoRepository;

    public AcaoMapper(HistoricoCotacaoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public AcaoOutputDto toDTO(Acao a) {
        AcaoOutputDto dto = new AcaoOutputDto();
        dto.setId(a.getId());
        dto.setTicker(a.getTicker());
        dto.setNomeEmpresa(a.getNomeEmpresa());
        dto.setMercado(a.getMercado());
        dto.setMoeda(a.getMoeda());
        dto.setCotacaoAtual(a.getCotacaoAtual());
        dto.setDataHoraCotacao(a.getDataHoraCotacao());
        dto.setTemHistorico(historicoRepository.existsByAcaoId(a.getId()));
        return dto;
    }
}
