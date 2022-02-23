package com.example.hzwatch.service;

import android.content.Context;
import android.util.Log;

import com.example.hzwatch.domain.Entity;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.ui.MainActivity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private static final String TAG = "Storage";

    private static final String FILE_NAME = "hzwatch.json";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static Storage instance;
    private static int SEQ = 0;

    private String searchKeyList;
    private boolean priceError;
    private List<PriceError> priceErrorList;
    private List<SearchLog> searchLogList;

    private boolean isChange = false;
    private Context context;

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }

        return instance;
    }

    public void create(PriceError priceError) {
        priceError.setStorage(this);
        priceErrorList.add(priceError);
    }

    public void create(SearchLog searchLog) {
        searchLog.setStorage(this);
        searchLogList.add(searchLog);
    }

    public void clean() {
        initEmptyStorage();
        save();
    }

    public List<PriceError> findPriceError() {
        return priceErrorList;
    }

    public List<SearchLog> findSearchLog() {
        return searchLogList;
    }

    public String getSearchKeyList() {
        return searchKeyList;
    }

    public void setSearchKeyList(String searchKeyList) {
        this.searchKeyList = searchKeyList;
        notifyChange();
    }

    public int id() {
        return ++SEQ;
    }

    private void initEmptyStorage() {
        searchKeyList = null;
        priceError = false;
        priceErrorList = new ArrayList<>();
        searchLogList = new ArrayList<>();
    }

    public boolean isChange() {
        return isChange;
    }

    public boolean isPriceError() {
        return priceError;
    }

    public void setPriceError(boolean priceError) {
        this.priceError = priceError;
        notifyChange();
    }

    public void load() {
        Log.d(TAG, "load: begin");
        FileInputStream fileIn;

        try {
            fileIn = context.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            initEmptyStorage();
            return;
        }

        HzwatchStorage hzwatchStorage;

        try {
            hzwatchStorage = JSON_MAPPER.readValue(new InputStreamReader(fileIn, StandardCharsets.UTF_8), HzwatchStorage.class);
        } catch (IOException e) {
            e.printStackTrace();
            initEmptyStorage();
            return;
        }

        searchKeyList = hzwatchStorage.getSearchKeyList();
        priceError = hzwatchStorage.isPriceError();
        priceErrorList = hzwatchStorage.getPriceErrorList();
        searchLogList = hzwatchStorage.getSearchLogList();

        processPostLoad();
        Log.d(TAG, "load: end");
    }

    public void notifyChange() {
        isChange = true;
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

    public void save() {
        Log.d(TAG, "save: begin");

        HzwatchStorage hzwatchStorage = new HzwatchStorage(1, searchKeyList, priceError, priceErrorList, searchLogList);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            String encoded = JSON_MAPPER.writeValueAsString(hzwatchStorage);
            fos.write(encoded.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        isChange = false;
        Log.d(TAG, "save: end");
    }

    public void setContext(MainActivity mainActivity) {
        this.context = mainActivity;
    }

    public static class HzwatchStorage {
        private int version;
        private String searchKeyList;
        private boolean priceError;
        private List<PriceError> priceErrorList;
        private List<SearchLog> searchLogList;

        public HzwatchStorage() {

        }

        public HzwatchStorage(int version, String searchKeyList, boolean priceError, List<PriceError> priceErrorList, List<SearchLog> searchLogList) {
            this.version = version;
            this.searchKeyList = searchKeyList;
            this.priceError = priceError;
            this.priceErrorList = priceErrorList;
            this.searchLogList = searchLogList;
        }

        public List<PriceError> getPriceErrorList() {
            return priceErrorList;
        }

        public void setPriceErrorList(List<PriceError> priceErrorList) {
            this.priceErrorList = priceErrorList;
        }

        public String getSearchKeyList() {
            return searchKeyList;
        }

        public void setSearchKeyList(String searchKeyList) {
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

        public boolean isPriceError() {
            return priceError;
        }

        public void setPriceError(boolean priceError) {
            this.priceError = priceError;
        }
    }
}
