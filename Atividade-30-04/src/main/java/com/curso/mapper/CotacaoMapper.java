package com.curso.mapper;

import com.curso.dto.AlphaOutputDto;
import com.curso.dto.CotacaoOutputDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CotacaoMapper {

    public static CotacaoOutputDto fromAlpha(AlphaOutputDto response) {

        var q = response.getGlobalQuote();

        CotacaoOutputDto dto = new CotacaoOutputDto();
        dto.setTicker(q.getSimbolo());
        dto.setMoeda("USD");
        dto.setCotacao(Double.parseDouble(q.getPreco()));
        dto.setDataHora(LocalDateTime.now());

        return dto;
    }
}