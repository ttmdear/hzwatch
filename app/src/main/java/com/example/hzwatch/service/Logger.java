package com.example.hzwatch.service;

import android.util.Log;

import com.example.hzwatch.domain.LogEntry;
import com.example.hzwatch.util.Util;

import java.util.List;

public class Logger {
    private static final String TAG = Logger.class.getCanonicalName();

    private final Storage storage = Services.getStorage();
    private Integer ID_SEQ = 0;

    public List<LogEntry> getLogEntryAll() {
        return storage.findLogEntryAll();
    }

    public LogEntry getLogEntry(Integer id) {
        return Util.find(storage.findLogEntryAll(), logEntry -> logEntry.getId().equals(id));
    }

    public void log(String msg) {
        LogEntry logEntry = new LogEntry();

        logEntry.setId(++ID_SEQ);
        logEntry.setMsg(msg);
        logEntry.setAt(Util.date());

        storage.create(logEntry);

        Log.d(TAG, logEntry.getMsg());
    }

    public void log(String msg, Object ...objects) {
        LogEntry logEntry = new LogEntry();

        logEntry.setId(++ID_SEQ);
        logEntry.setMsg(String.format(msg, objects));
        logEntry.setAt(Util.date());

        storage.create(logEntry);

        Log.d(TAG, logEntry.getMsg());
    }
}
