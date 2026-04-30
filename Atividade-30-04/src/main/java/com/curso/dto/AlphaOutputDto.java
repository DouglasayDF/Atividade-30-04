package com.curso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlphaOutputDto {

    @JsonProperty("Global Quote")
    private AlphaInputDto globalQuote;

    public AlphaInputDto getGlobalQuote() {
        return globalQuote;
    }

    public void setGlobalQuote(AlphaInputDto globalQuote) {
        this.globalQuote = globalQuote;
    }
}