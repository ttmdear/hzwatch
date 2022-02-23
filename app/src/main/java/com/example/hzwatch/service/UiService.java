package com.example.hzwatch.service;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class UiService {

    private static final DateFormat READ_DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
    private static final DateFormat READ_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());


    public String formatReadDateTime(Date date) {
        return READ_DATE_TIME_FORMAT.format(date);
    }

    public String formatReadDate(Date date) {
        return READ_DATE_FORMAT.format(date);
    }
}
