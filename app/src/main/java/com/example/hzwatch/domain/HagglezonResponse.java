package com.example.hzwatch.domain;

import android.content.Intent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HagglezonResponse {
    private Data data;

    public HagglezonResponse() {

    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private SearchProducts searchProducts;

        public Data() {

        }

        public SearchProducts getSearchProducts() {
            return searchProducts;
        }

        public void setSearchProducts(SearchProducts searchProducts) {
            this.searchProducts = searchProducts;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchProducts {
        List<Product> products;

        public SearchProducts() {

        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {
        private String id;
        private String title;
        private List<Price> prices;

        public Product() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Price> getPrices() {
            return prices;
        }

        public void setPrices(List<Price> prices) {
            this.prices = prices;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        private String country;
        private Double price;
        private String currency;
        private String url;

        public Price() {

        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
