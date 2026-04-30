package com.curso.dto;

import java.util.List;

public class BrapiInputDto {
    private List<BrapiOutputDto> results;

    public List<BrapiOutputDto> getResults() {
        return results;
    }

    public void setResults(List<BrapiOutputDto> results) {
        this.results = results;
    }
}