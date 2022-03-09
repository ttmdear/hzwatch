package com.example.hzwatch.service;

import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.util.SortUtil;
import com.example.hzwatch.util.Util;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class HzwatchService {
    private static final Random RANDOM = new Random();

    private final Storage storage = Services.getStorage();
    private final Logger logger = Services.getLogger();

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
        logger.log("Get next search");

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
                logger.log("Return key %s (1)", searchKey.getValue());
                return searchKey.getValue();
            }
        }

        if (searchKeyList.isEmpty()) {
            logger.log("No search key (1)");
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
                logger.log("Return key %s (2)", searchKey.getValue());
                return searchKey.getValue();
            }
        }

        logger.log("No search key (2)");
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
        logger.log("Post search [%s], productsNumber [%s]", searchKey, productsNumber);

        SearchLog searchLog = Util.find(storage.findSearchLogAll(), searchLog1 -> searchLog1.getSearchKey().equals(searchKey));

        if (searchLog == null) {
            searchLog = new SearchLog();
            searchLog.setSearchKey(searchKey);
        }

        Date date = Util.date();

        searchLog.setAt(date);
        searchLog.setNextSearchAt(Util.datePlusSeconds(date, (5 * 60) + RANDOM.nextInt(5 * 60)));
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

    public String getSearchKeyString() {
        StringBuilder builder = new StringBuilder();

        for (SearchKey searchKey : getSearchKeyAll()) {
            if (builder.length() > 0) {
                builder.append(",");
            }

            builder.append(searchKey.getValue());
        }

        return builder.toString();
    }

    public void importSearchKey(String string) {
        String[] split = string.split(",");

        for (String s : split) {
            s = s.trim();

            if (s.isEmpty()) continue;

            if (!existsSearchKey(s)) {
                createSearchKey(s);
            }
        }
    }

    public List<SearchLog> getSearchLogAll() {
        return storage.findSearchLogAll();
    }
}
