package com.example.hzwatch.domain;

import java.util.Date;

public class PriceError extends Entity {
    private String product;
    private String url;
    private Date at;
    private Double price;
    private Double avr;

    public PriceError() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
        notifyChange();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
