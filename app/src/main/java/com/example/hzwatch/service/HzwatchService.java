package com.example.hzwatch.service;

import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.util.SortUtil;
import com.example.hzwatch.util.Util;

import java.util.Date;
import java.util.List;

public class HzwatchService {
    private final Storage storage = Services.getStorage();

    public void createSearchKey(String searchKeyValue) {
        SearchKey searchKey = new SearchKey();
        searchKey.setId(storage.id());
        searchKey.setValue(searchKeyValue);

        storage.create(searchKey);
    }

    public void deleteSearchKey(Integer searchKeyId) {
        storage.deleteSearchKey(searchKeyId);
    }

    public boolean existsSearchKey(String searchKeyString) {
        return Util.<SearchKey>find(storage.findSearchKeyAll(), searchKey -> searchKey.getValue().equals(searchKeyString)) != null;
    }

    public List<PriceError> getAllPriceError() {
        return storage.findPriceErrorAll();
    }

    public String getNextSearchKeyToSearch() {
        List<SearchLog> searchLogList = storage.findSearchLogAll();
        List<SearchKey> searchKeyList = storage.findSearchKeyAll();

        for (SearchKey searchKey : searchKeyList) {
            boolean found = false;

            for (SearchLog searchLog : searchLogList) {
                if (searchLog.getSearchKey().equals(searchKey.getValue())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return searchKey.getValue();
            }
        }

        if (searchKeyList.isEmpty()) {
            return null;
        }

        SortUtil.sortByDateAsc(searchLogList, SearchLog::getNextSearchAt);

        Date date = Util.date();

        for (SearchLog searchLog : searchLogList) {
            if (!date.after(searchLog.getNextSearchAt())) {
                continue;
            }

            SearchKey searchKey = Util.find(searchKeyList, searchKey1 -> searchKey1.getValue().equals(searchLog.getSearchKey()));

            if (searchKey != null) {
                return searchKey.getValue();
            }
        }

        return null;
    }

    public PriceError getPriceErrorByHzId(String hzId) {
        return Util.find(storage.findPriceErrorAll(), priceError1 -> priceError1.getHzId().equals(hzId));
    }

    public List<SearchKey> getSearchKeyAll() {
        return storage.findSearchKeyAll();
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

    public List<PriceError> getActivePriceError() {
        return Util.filter(storage.findPriceErrorAll(), priceError -> !priceError.getMoved());
    }

    public List<PriceError> getMovedPriceError() {
        return Util.filter(storage.findPriceErrorAll(), PriceError::getMoved);
    }

    public String getHzUrl(String hzId) {
        return String.format("https://www.hagglezon.com/en/s/%s", hzId);
    }
}
