package com.example.hzwatch.service;

import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchEntry;

import java.util.List;

public class Storage {
    private static Storage instance;

    private String searchKeyList;

    private List<PriceError> priceErrorList;
    private List<SearchEntry> searchEntryList;

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }

        return instance;
    }

    public String getSearchKeyList() {
        return searchKeyList;
    }

    public void setSearchKeyList(String searchKeyList) {
        this.searchKeyList = searchKeyList;
    }
}
