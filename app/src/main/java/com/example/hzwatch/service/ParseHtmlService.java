package com.example.hzwatch.service;

import com.example.hzwatch.domain.Product;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParseHtmlService {
    public List<Product> parse(String html) {
        List<Product> products = new ArrayList<>();

        Document document = Jsoup.parse(html);

        Elements cards = document.getElementsByClass("card-body");

        for (Element card : cards) {
            List<Double> prices = new ArrayList<>();

            String title = card.getElementsByClass("text-wrapper").get(0).text();
            Elements priceValues = card.getElementsByClass("price-value");

            if (priceValues.size() <= 1) {
                continue;
            }

            for (Element priceValue : priceValues) {
                Double price = parseDouble(priceValue.text());
                prices.add(price);
            }

            products.add(new Product(Product.id(title), title, prices));
        }

        return products;
    }

    private Double parseDouble(String substring) {
        substring = substring.substring(1);
        substring = substring.replaceAll(",", "");

        return Double.parseDouble(substring);
    }
}
