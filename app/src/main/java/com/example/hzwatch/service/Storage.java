package com.example.hzwatch.service;

import android.content.Context;
import android.util.Log;

import com.example.hzwatch.domain.Entity;
import com.example.hzwatch.domain.LogEntry;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchKey;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.domain.WatcherAlarm;
import com.example.hzwatch.ui.MainActivity;
import com.example.hzwatch.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Storage {
    private static final String TAG = "Storage";

    private static final String FILE_NAME = "hzwatch.json";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static int SEQ = 0;

    private List<SearchKey> searchKeyList;
    private Boolean priceError;
    private List<PriceError> priceErrorList;
    private List<SearchLog> searchLogList;
    private List<LogEntry> logEntryList = new ArrayList<>();
    private List<WatcherAlarm> watcherAlarmList;
    private boolean loaded = false;

    private boolean change = false;
    private Date savedAt = Util.date();
    private Context context;

    public void clean() {
        initEmptyStorage();
        notifyChange();
    }

    public void cleanLogData() {
        logEntryList = new ArrayList<>();
        notifyChange();
    }

    public void cleanHzData() {
        priceError = false;
        priceErrorList = new ArrayList<>();
        searchLogList = new ArrayList<>();
        notifyChange();
    }

    public void create(PriceError priceError) {
        priceError.setStorage(this);
        priceErrorList.add(priceError);
        notifyChange();
    }

    public void create(SearchLog searchLog) {
        searchLog.setStorage(this);
        searchLogList.add(searchLog);
        notifyChange();
    }

    public void create(SearchKey searchKey) {
        searchKey.setStorage(this);
        searchKeyList.add(searchKey);
        notifyChange();
    }

    public void create(LogEntry logEntry) {
        logEntry.setStorage(this);
        logEntryList.add(logEntry);

        if (logEntryList.size() > 100) {
            List<LogEntry> n = new ArrayList<>(50);

            for (int i = 50; i < logEntryList.size(); i++) {
                n.add(logEntryList.get(i));
            }

            logEntryList = n;
        }

        notifyChange();
    }

    public void create(WatcherAlarm watcherAlarm) {
        watcherAlarm.setStorage(this);
        watcherAlarmList.add(watcherAlarm);
        notifyChange();
    }

    public void deleteWatcherAlarmAll() {
        watcherAlarmList.clear();
        notifyChange();
    }

    public List<LogEntry> findLogEntryAll() {
        return logEntryList;
    }

    public List<WatcherAlarm> findWatcherAlarmAll() {
        return watcherAlarmList;
    }

    public void movePriceError(Integer priceErrorId) {
        PriceError priceError = Util.getById(priceErrorList, priceErrorId);
        priceError.setMoved(true);
    }

    public void deleteSearchKey(Integer searchKeyId) {
        searchKeyList = Util.filter(searchKeyList, searchKey -> !searchKey.getId().equals(searchKeyId));
        notifyChange();
    }

    public void deleteWatcherAlarm(Integer watcherAlarmId) {
        watcherAlarmList = Util.filter(watcherAlarmList, watcherAlarm -> !watcherAlarm.getId().equals(watcherAlarmId));
        notifyChange();
    }

    public List<PriceError> findPriceErrorAll() {
        return priceErrorList;
    }

    public List<SearchKey> findSearchKeyAll() {
        return searchKeyList;
    }

    public List<SearchLog> findSearchLogAll() {
        return searchLogList;
    }

    public Boolean getPriceError() {
        return priceError;
    }

    public void setPriceError(Boolean priceError) {
        this.priceError = priceError;
        notifyChange();
    }

    public int id() {
        return ++SEQ;
    }

    private void initEmptyStorage() {
        searchKeyList = new ArrayList<>();
        priceError = false;
        priceErrorList = new ArrayList<>();
        searchLogList = new ArrayList<>();
        logEntryList = new ArrayList<>();
        watcherAlarmList = new ArrayList<>();
    }

    public boolean isChange() {
        return change;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        FileInputStream fileIn;

        try {
            fileIn = context.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            initEmptyStorage();
            loaded = true;
            return;
        }

        HzwatchStorage hzwatchStorage;

        try {
            hzwatchStorage = JSON_MAPPER.readValue(new InputStreamReader(fileIn, StandardCharsets.UTF_8), HzwatchStorage.class);
        } catch (IOException e) {
            initEmptyStorage();
            loaded = true;
            return;
        }

        searchKeyList = Util.emptyListIfNull(hzwatchStorage.getSearchKeyList());
        priceError = Util.falseIfNull(hzwatchStorage.getPriceError());
        priceErrorList = Util.emptyListIfNull(hzwatchStorage.getPriceErrorList());
        searchLogList = Util.emptyListIfNull(hzwatchStorage.getSearchLogList());
        logEntryList = Util.emptyListIfNull(hzwatchStorage.getLogEntryList());
        watcherAlarmList = Util.emptyListIfNull(hzwatchStorage.getWatcherAlarmList());

        processPostLoad();
        loaded = true;
    }

    public void loadTestData() {
        initEmptyStorage();

        PriceError priceError = new PriceError();
        priceError.setId(id());
        priceError.setHzId("HZ-1");
        priceError.setStorage(this);
        priceError.setMoved(false);
        priceError.setProduct("Samsung Galaxy S21 5G Smartphone 128GB Phantom Grey Android 11.0 G991B");
        priceError.setAt(Util.date());
        priceError.setPrice(2034.23);
        priceError.setAvr(40.12);
        priceErrorList.add(priceError);
    }

    public void notifyChange() {
        if (Util.secondsFrom(savedAt) >= 50) {
            save();
            savedAt = Util.date();
        }
    }

    private void processPostLoad() {
        processPostLoad(priceErrorList);
        processPostLoad(searchLogList);
    }

    private void processPostLoad(List<? extends Entity> entities) {
        for (Entity entity : entities) {
            entity.setStorage(this);
            if (entity.getId() > SEQ) {
                SEQ = entity.getId();
            }
        }
    }

    public synchronized void save() {
        Log.d(TAG, "save: begin");

        HzwatchStorage hzwatchStorage = new HzwatchStorage();
        hzwatchStorage.setVersion(1);
        hzwatchStorage.setSearchKeyList(searchKeyList);
        hzwatchStorage.setPriceError(priceError);
        hzwatchStorage.setPriceErrorList(priceErrorList);
        hzwatchStorage.setSearchLogList(searchLogList);
        hzwatchStorage.setLogEntryList(logEntryList);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            String encoded = JSON_MAPPER.writeValueAsString(hzwatchStorage);
            fos.write(encoded.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        change = false;
        Log.d(TAG, "save: end");
    }

    public void setContext(MainActivity mainActivity) {
        this.context = mainActivity;
    }

    public static class HzwatchStorage {
        private int version;
        private List<SearchKey> searchKeyList;
        private Boolean priceError;
        private List<PriceError> priceErrorList;
        private List<SearchLog> searchLogList;
        private List<LogEntry> logEntryList;
        private List<WatcherAlarm> watcherAlarmList;

        public HzwatchStorage() {

        }

        public List<LogEntry> getLogEntryList() {
            return logEntryList;
        }

        public void setLogEntryList(List<LogEntry> logEntryList) {
            this.logEntryList = logEntryList;
        }

        public Boolean getPriceError() {
            return priceError;
        }

        public void setPriceError(Boolean priceError) {
            this.priceError = priceError;
        }

        public List<PriceError> getPriceErrorList() {
            return priceErrorList;
        }

        public void setPriceErrorList(List<PriceError> priceErrorList) {
            this.priceErrorList = priceErrorList;
        }

        public List<SearchKey> getSearchKeyList() {
            return searchKeyList;
        }

        public void setSearchKeyList(List<SearchKey> searchKeyList) {
            this.searchKeyList = searchKeyList;
        }

        public List<SearchLog> getSearchLogList() {
            return searchLogList;
        }

        public void setSearchLogList(List<SearchLog> searchLogList) {
            this.searchLogList = searchLogList;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public List<WatcherAlarm> getWatcherAlarmList() {
            return watcherAlarmList;
        }

        public void setWatcherAlarmList(List<WatcherAlarm> watcherAlarmList) {
            this.watcherAlarmList = watcherAlarmList;
        }
    }
}
