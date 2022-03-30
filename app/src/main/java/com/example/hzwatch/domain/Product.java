package com.example.hzwatch.domain;

import java.util.List;

public class Product {
    private final String id;
    private final String title;
    private final List<Double> prices;

    public Product(String id, String title, List<Double> prices) {
        this.id = id;
        this.title = title;
        this.prices = prices;
    }

    public static String id(String title) {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Double> getPrices() {
        return prices;
    }

    public boolean isPriceError() {
        for (int i = 0; i < prices.size(); i++) {
            double price = prices.get(i);
            double avr = 0;
            int divider = 0;

            for (int j = 0; j < prices.size(); j++) {
                if (i == j) continue;

                divider++;
                avr += prices.get(j);
            }

            avr = avr / divider;

            if (price <= avr * 0.90) {
                return true;
            }
        }

        return false;
    }

    public double getSum() {
        double sum = 0;

        for (Double price : prices) {
            sum += price;
        }

        return sum;
    }

    public double getAvr() {
        if (prices.isEmpty()) {
            return 0;
        }

        return getSum() / prices.size();
    }
}
