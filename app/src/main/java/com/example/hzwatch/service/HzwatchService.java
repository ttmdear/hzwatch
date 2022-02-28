package com.example.hzwatch.service;

import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.util.SortUtil;

import java.util.List;

public class HzwatchService {
    private static final Storage storage = Services.getStorage();

    public String getSearchLogToProcess() {
        List<SearchLog> searchLogList = storage.findSearchLogAll();

        if (!searchLogList.isEmpty()) {

        } else {

        }

        SortUtil.sortDesc(searchLogList, SearchLog::getNextSearchAt);

    }

    private List<String> prepareSearchKeyList() {
        String searchKeyList = storage.getSearchKeyList();

        if (searchKeyList == null || searchKeyList.isEmpty()) {
            return null;
        }

        String[] searchKeyArray = searchKeyList.split(",");
    }
}
