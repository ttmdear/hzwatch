package com.example.hzwatch.domain;

public class SearchEntry extends Entity {
    private String search;

    public SearchEntry(Integer id, Integer order, String search) {
        super(id, order);
        this.search = search;
    }
}