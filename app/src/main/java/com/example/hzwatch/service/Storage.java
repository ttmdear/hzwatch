package com.example.hzwatch.service;

import android.content.Context;
import android.util.Log;

import com.example.hzwatch.domain.Entity;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchLog;
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

    private String searchKeyList;
    private Boolean priceError;
    private List<PriceError> priceErrorList;
    private List<PriceError> priceErrorDeletedList;
    private List<SearchLog> searchLogList;

    private boolean isChange = false;
    private Context context;

    public void clean() {
        initEmptyStorage();
        save();
    }

    public void loadTestData() {
        PriceError priceError = new PriceError();
        priceError.setId(id());
        priceError.setProduct("Samsung Galaxy S21 5G Smartphone 128GB Phantom Grey Android 11.0 G991B");
        priceError.setUrl("http://o2.pl");
        priceError.setAt(Util.date());
        priceError.setPrice(2034.23);
        priceError.setAvr(40.12);
        priceErrorList.add(priceError);
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

    public void deletePriceError(Integer priceErrorId) {
        PriceError priceError = Util.getById(priceErrorList, priceErrorId);
        priceErrorList = Util.filter(priceErrorList, priceError1 -> !priceError1.getId().equals(priceError.getId()));
        priceErrorDeletedList.add(priceError);
        notifyChange();
    }

    public List<PriceError> findPriceErrorDeleted() {
        return priceErrorDeletedList;
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
        priceErrorDeletedList = new ArrayList<>();
        searchLogList = new ArrayList<>();
    }

    public boolean isChange() {
        return isChange;
    }

    public Boolean getPriceError() {
        return priceError;
    }

    public void setPriceError(Boolean priceError) {
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
        priceError = hzwatchStorage.getPriceError() != null && hzwatchStorage.getPriceError();
        priceErrorList = orEmptyList(hzwatchStorage.getPriceErrorList());
        priceErrorDeletedList = orEmptyList(hzwatchStorage.getPriceErrorDeletedList());
        searchLogList = orEmptyList(hzwatchStorage.getSearchLogList());

        processPostLoad();
        Log.d(TAG, "load: end");
    }

    private <T> List<T> orEmptyList(List<T> list) {
        if (list == null) return new ArrayList<>();
        return list;
    }

    public void notifyChange() {
        isChange = true;
    }

    private void processPostLoad() {
        processPostLoad(priceErrorList);
        processPostLoad(priceErrorDeletedList);
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

        HzwatchStorage hzwatchStorage = new HzwatchStorage();
        hzwatchStorage.setVersion(1);
        hzwatchStorage.setSearchKeyList(searchKeyList);
        hzwatchStorage.setPriceError(priceError);
        hzwatchStorage.setPriceErrorList(priceErrorList);
        hzwatchStorage.setPriceErrorDeletedList(priceErrorDeletedList);
        hzwatchStorage.setSearchLogList(searchLogList);

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
        private Boolean priceError;
        private List<PriceError> priceErrorList;
        private List<PriceError> priceErrorDeletedList;
        private List<SearchLog> searchLogList;

        public HzwatchStorage() {

        }

        public List<PriceError> getPriceErrorDeletedList() {
            return priceErrorDeletedList;
        }

        public void setPriceErrorDeletedList(List<PriceError> priceErrorDeletedList) {
            this.priceErrorDeletedList = priceErrorDeletedList;
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

        public Boolean getPriceError() {
            return priceError;
        }

        public void setPriceError(Boolean priceError) {
            this.priceError = priceError;
        }
    }
}
