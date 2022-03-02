package com.example.hzwatch.service;

import com.example.hzwatch.domain.HagglezonResponse.Price;
import com.example.hzwatch.domain.HagglezonResponse.Product;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductProcessor {
    private final Storage storage = Services.getStorage();

    private final List<String> PRICE_COUNTRY_TO_OMIT = Collections.singletonList("se");

    private Double calcPriceSum(Product product) {
        Double sum = 0.0;

        for (Price price : product.getPrices()) {
            if (price.getPrice() != null) {
                sum += price.getPrice();
            }
        }

        return sum;
    }

    private boolean isProductToProcess(Product product) {
        if (product.getPrices() == null || product.getPrices().size() <= 1) {
            return false;
        }

        List<PriceError> priceErrorWholeList = new ArrayList<>();
        priceErrorWholeList.addAll(storage.findPriceErrorAll());
        priceErrorWholeList.addAll(storage.findPriceErrorDeletedAll());

        PriceError priceError = Util.find(priceErrorWholeList, priceError1 -> priceError1.getHzId().equals(product.getId()));

        if (priceError == null) {
            return true;
        }

        Double productPriceSum = calcPriceSum(product);

        return !productPriceSum.equals(priceError.getPriceSum());
    }

    public ProcessProductResult process(Product product) {
        removeUnwantedPrices(product);

        if (!isProductToProcess(product)) {
            return new ProcessProductResult(false);
        }

        List<Price> priceList = product.getPrices();

        for (int i = 0; i < priceList.size(); i++) {
            double price = priceList.get(i).getPrice();
            double avr = 0;
            int divider = 0;

            for (int j = 0; j < priceList.size(); j++) {
                if (i == j) continue;

                divider++;
                avr += priceList.get(j).getPrice();
            }

            avr = avr / divider;

            if (price <= avr * 0.5) {
                PriceError priceError = new PriceError();
                priceError.setId(storage.id());
                priceError.setHzId(product.getId());
                priceError.setProduct(product.getTitle());
                priceError.setPriceSum(calcPriceSum(product));
                priceError.setUrl(priceList.get(i).getUrl());
                priceError.setAt(Util.date());
                priceError.setAvr(avr);
                priceError.setPrice(price);

                return new ProcessProductResult(true);
            }
        }

        return new ProcessProductResult(false);
    }

    private void removeUnwantedPrices(Product product) {
        if (product.getPrices() == null) return;

        product.setPrices(Util.filter(product.getPrices(), price ->
            !PRICE_COUNTRY_TO_OMIT.contains(price.getCountry())));
    }

    public static class ProcessProductResult {
        private final boolean priceError;

        public ProcessProductResult(boolean priceError) {
            this.priceError = priceError;
        }

        public boolean isPriceError() {
            return priceError;
        }
    }
}
