package com.example.hzwatch.service;

import com.example.hzwatch.domain.HagglezonResponse;
import com.example.hzwatch.domain.HagglezonResponse.Product;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.util.SortUtil;
import com.example.hzwatch.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HzwatchService {
    private final Storage storage = Services.getStorage();

    public String getNextSearchKeyToSearch() {
        List<SearchLog> searchLogList = storage.findSearchLogAll();
        List<String> searchKeyList = storage.getSearchKeyList();

        for (String searchKey : searchKeyList) {
            boolean found = false;

            for (SearchLog searchLog : searchLogList) {
                if (searchLog.getSearchKey().equals(searchKey)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return searchKey;
            }
        }

        if (searchKeyList.isEmpty()) {
            return null;
        }

        SortUtil.sortAsc(searchLogList, SearchLog::getNextSearchAt);

        Date date = Util.date();

        for (SearchLog searchLog : searchLogList) {
            if (date.after(searchLog.getNextSearchAt())) {
                return searchLog.getSearchKey();
            }
        }

        return null;
    }


    public List<String> getSearchKeyList() {
        return storage.getSearchKeyList();
    }

    public boolean isPriceError() {
        return storage.getPriceError();
    }

    public void postSearch(String searchKey, Integer productsNumber) {
        SearchLog searchLog = Util.find(storage.findSearchLogAll(), searchLog1 -> searchLog1.getSearchKey().equals(searchKey));

        if (searchLog == null) {
            searchLog = new SearchLog();
            searchLog.setSearchKey(searchKey);
        }

        Date date = Util.date();

        searchLog.setAt(date);
        searchLog.setNextSearchAt(Util.datePlusSeconds(date, 60 * 5));
        searchLog.setProductsNumber(productsNumber);

        if (searchLog.getId() == null) {
            searchLog.setId(storage.id());
            storage.create(searchLog);
        }
    }

    public void processPriceError(PriceError priceError) {
        storage.create(priceError);
        storage.setPriceError(true);
    }

    public void updateSearchKeyList(List<String> searchKeyList) {
        storage.setSearchKeyList(searchKeyList);
    }

    public String getHzUrl(String hzId) {
        return String.format("https://www.hagglezon.com/en/s/%s", hzId);
    }
}
