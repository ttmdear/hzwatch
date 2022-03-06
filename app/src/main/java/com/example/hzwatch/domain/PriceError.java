package com.example.hzwatch.domain;

import java.util.Date;

public class PriceError extends Entity {
    private String product;
    private String hzId;
    private Date at;
    private Double price;
    private Double avr;
    private Double priceSum;
    private Boolean moved;
    private String searchKey;

    public PriceError() {
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
        notifyChange();
    }

    public Double getAvr() {
        return avr;
    }

    public void setAvr(Double avr) {
        this.avr = avr;
        notifyChange();
    }

    public String getHzId() {
        return hzId;
    }

    public void setHzId(String hzId) {
        this.hzId = hzId;
        notifyChange();
    }

    public Boolean getMoved() {
        return moved;
    }

    public void setMoved(Boolean moved) {
        this.moved = moved;
        notifyChange();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
        notifyChange();
    }

    public Double getPriceSum() {
        return priceSum;
    }

    public void setPriceSum(Double priceSum) {
        this.priceSum = priceSum;
        notifyChange();
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
        notifyChange();
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
        notifyChange();
    }
}
