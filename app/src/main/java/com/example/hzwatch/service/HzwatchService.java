package com.example.hzwatch.service;

import com.example.hzwatch.domain.PriceError;

import java.util.List;

public class HzwatchService {
    private Storage storage = Services.getStorage();

    public List<PriceError> getPriceErrorAll() {
        return storage.findPriceError();
    }

    public List<PriceError> getPriceErrorDeleted() {
        return storage.findPriceErrorDeleted();
    }

    public void deletePriceError(Integer priceErrorId) {

    }
}
