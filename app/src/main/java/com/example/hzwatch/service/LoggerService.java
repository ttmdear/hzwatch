package com.example.hzwatch.service;

import com.example.hzwatch.domain.LogEntry;
import com.example.hzwatch.util.Util;

import java.util.List;

public class LoggerService {
    private Storage storage = Services.getStorage();
    private Integer ID_SEQ = 0;

    public List<LogEntry> getLogEntryAll() {
        return storage.findLogEntryAll();
    }

    public void log(String msg) {
        LogEntry logEntry = new LogEntry();

        logEntry.setId(ID_SEQ);
        logEntry.setMsg(msg);
        logEntry.setAt(Util.date());

        storage.create(logEntry);
    }
}
