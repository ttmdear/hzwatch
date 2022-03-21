package com.example.hzwatch.service;

import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.domain.WatcherAlarm;
import com.example.hzwatch.util.SortUtil;
import com.example.hzwatch.util.Util;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class HzwatchService {
    private final Storage storage;

    private final Random RANDOM = new Random();
    private final Logger logger = Services.getLogger();

    public HzwatchService(Storage storage) {
        this.storage = storage;
    }

    public void createSearchKey(String searchKeyValue) {
        SearchKey searchKey = new SearchKey();
        searchKey.setId(storage.id());
        searchKey.setValue(searchKeyValue);

        storage.create(searchKey);
    }

    public void deleteSearchKey(Integer searchKeyId) {
        storage.deleteSearchKey(searchKeyId);
    }

    public void deleteWatcherAlarm() {
        storage.deleteWatcherAlarmAll();
    }

    public boolean notExistsSearchKey(String searchKeyString) {
        return Util.find(storage.findSearchKeyAll(), searchKey1 -> searchKey1.getValue().equals(searchKeyString)) == null;
    }

    public List<PriceError> getActivePriceError() {
        return Util.filter(storage.findPriceErrorAll(), priceError -> !priceError.getMoved());
    }

    public List<PriceError> getAllPriceError() {
        return storage.findPriceErrorAll();
    }

    public String getHzUrl(String hzId) {
        return String.format("https://www.hagglezon.com/en/s/%s", hzId);
    }

    public List<PriceError> getMovedPriceError() {
        return Util.filter(storage.findPriceErrorAll(), PriceError::getMoved);
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

        com.ttmdear.toolbox.util.SortUtil.sortAsc(searchLogList, SearchLog::getLastSearchAt);

        for (SearchLog searchLog : searchLogList) {
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

    public List<SearchLog> getSearchLogAll() {
        return storage.findSearchLogAll();
    }

    public void importSearchKey(String string) {
        String[] split = string.split(",");

        for (String s : split) {
            s = s.trim();

            if (s.isEmpty()) continue;

            if (notExistsSearchKey(s)) {
                createSearchKey(s);
            }
        }
    }

    public boolean isPriceError() {
        return storage.getPriceError();
    }

    public boolean isWatcherAlarmPlanned() {
        List<WatcherAlarm> watcherAlarmList = storage.findWatcherAlarmAll();

        if (watcherAlarmList.isEmpty()) {
            return false;
        }

        WatcherAlarm watcherAlarm = watcherAlarmList.get(0);

        Date date = Util.datePlusMinutes(watcherAlarm.getPlanedAt(), 15);

        if (date.before(new Date())) {
            storage.deleteWatcherAlarm(-1);
            return false;
        }

        return true;
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
        searchLog.setNextSearchAt(Util.datePlusMinutes(date, 5 + RANDOM.nextInt(10)));
        searchLog.setLastSearchAt(date);
        searchLog.setProductsNumber(productsNumber);

        if (searchLog.getId() == null) {
            searchLog.setId(storage.id());
            storage.create(searchLog);
        }
    }

    public void saveWatcherAlarmPlanned(Date planedAt) {
        storage.deleteWatcherAlarmAll();

        WatcherAlarm watcherAlarm = new WatcherAlarm();
        watcherAlarm.setId(-1);
        watcherAlarm.setPlanedAt(planedAt);

        storage.create(watcherAlarm);
    }
}
