package com.example.hzwatch.service;

import com.example.hzwatch.domain.HagglezonResponse;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Storage {
    private static Storage instance;
    private static int SEQ = 0;

    private String searchKeyList;
    private List<PriceError> priceErrorList;
    private List<SearchLog> searchLogList;

    public void create(PriceError priceError) {
        priceErrorList.add(priceError);
    }

    public void create(SearchLog searchLog) {
        searchLogList.add(searchLog);
    }

    public List<PriceError> findPriceError() {
        return priceErrorList;
    }

    public List<SearchLog> findSearchLog() {
        return searchLogList;
    }

    public void load() {
        priceErrorList = new ArrayList<>();
        searchLogList = new ArrayList<>();

        SearchLog searchLog = new SearchLog();
        searchLog.setId(id());
        searchLog.setSearchKey("ABC");
        searchLogList.add(searchLog);

        PriceError priceError = new PriceError();
        priceError.setId(id());
        priceError.setProduct("Test");
        priceErrorList.add(priceError);
    }

    public int id() {
        return ++SEQ;
    }

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
