package com.example.hzwatch.domain;

import java.util.Date;

public class PriceError extends Entity {
    private String product;
    private String url;
    private boolean checked;
    private Date at;

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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        notifyChange();
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
        notifyChange();
    }
}
