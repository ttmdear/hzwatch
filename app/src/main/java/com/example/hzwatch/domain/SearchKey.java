package com.example.hzwatch.domain;

public class SearchKey extends Entity {
    private String value;

    public SearchKey() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        notifyChange();
    }
}
