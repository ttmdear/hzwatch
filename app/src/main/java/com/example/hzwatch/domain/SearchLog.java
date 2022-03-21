package com.example.hzwatch.domain;

import java.util.Date;

public class SearchLog extends Entity {
    private Date at;
    private Date nextSearchAt;
    private String searchKey;
    private Date lastSearchAt;
    private int productsNumber;

    public SearchLog() {
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
        notifyChange();
    }

    public Date getNextSearchAt() {
        return nextSearchAt;
    }

    public void setNextSearchAt(Date nextSearchAt) {
        this.nextSearchAt = nextSearchAt;
        notifyChange();
    }

    public int getProductsNumber() {
        return productsNumber;
    }

    public void setProductsNumber(int productsNumber) {
        this.productsNumber = productsNumber;
        notifyChange();
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
        notifyChange();
    }

    public Date getLastSearchAt() {
        return lastSearchAt;
    }

    public void setLastSearchAt(Date lastSearchAt) {
        this.lastSearchAt = lastSearchAt;
    }
}