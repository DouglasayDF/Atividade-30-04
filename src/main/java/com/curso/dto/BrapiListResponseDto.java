package com.curso.dto;

import java.util.List;

public class BrapiListResponseDto {
    private List<BrapiListIndexDto> indexes;
    private List<BrapiListStockDto> stocks;
    private List<String> availableSectors;
    private List<String> availableStockTypes;
    private List<String> availableSubTypeTypes;
    private Integer currentPage;
    private Integer totalPages;
    private Integer itemsPerPage;
    private Integer totalCount;
    private Boolean hasNextPage;
    private String requestedAt;
    private Long took;

    public List<BrapiListIndexDto> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<BrapiListIndexDto> indexes) {
        this.indexes = indexes;
    }

    public List<BrapiListStockDto> getStocks() {
        return stocks;
    }

    public void setStocks(List<BrapiListStockDto> stocks) {
        this.stocks = stocks;
    }

    public List<String> getAvailableSectors() {
        return availableSectors;
    }

    public void setAvailableSectors(List<String> availableSectors) {
        this.availableSectors = availableSectors;
    }

    public List<String> getAvailableStockTypes() {
        return availableStockTypes;
    }

    public void setAvailableStockTypes(List<String> availableStockTypes) {
        this.availableStockTypes = availableStockTypes;
    }

    public List<String> getAvailableSubTypeTypes() {
        return availableSubTypeTypes;
    }

    public void setAvailableSubTypeTypes(List<String> availableSubTypeTypes) {
        this.availableSubTypeTypes = availableSubTypeTypes;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }
}
