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

    public PriceError() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
        notifyChange();
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
        notifyChange();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
    }

    public Double getPriceSum() {
        return priceSum;
    }

    public void setPriceSum(Double priceSum) {
        this.priceSum = priceSum;
    }

    public Boolean getMoved() {
        return moved;
    }

    public void setMoved(Boolean moved) {
        this.moved = moved;
    }
}
