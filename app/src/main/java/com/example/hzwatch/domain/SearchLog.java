package com.example.hzwatch.domain;

import java.util.Date;

public class SearchLog extends Entity {
    private Date at;
    private String searchKey;
    private int itemsNumber;

    public SearchLog() {
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
        notifyChange();
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
        notifyChange();
    }

    public int getItemsNumber() {
        return itemsNumber;
    }

    public void setItemsNumber(int itemsNumber) {
        this.itemsNumber = itemsNumber;
        notifyChange();
    }
}