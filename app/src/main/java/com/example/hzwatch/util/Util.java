package com.example.hzwatch.util;

import com.example.hzwatch.domain.Entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Util {
    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final int SECONDS_IN_HOUR = 60 * 60;
    public static final int SECONDS_IN_MINUTE = 60;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Util() {

    }

    public static Date date(String s) {
        try {
            return DATE_FORMATTER.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date date() {
        return new Date();
    }

    public static Date datePlusSeconds(Date date, Integer time) {
        return new Date(date.getTime() + time * 1000);
    }

    public static <T> List<T> filter(List<T> value, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();

        for (T t : value) {
            if (predicate.test(t)) {
                result.add(t);
            }
        }

        return result;
    }

    public static <T extends String> T nullIfEmpty(T str) {
        return str == null ? null : str.isEmpty() ? null : str;
    }

    public static <T> List<T> emptyListIfNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    public static Boolean falseIfNull(Boolean value) {
        return value != null && value;
    }

    public static <T> T find(List<T> value, Predicate<T> predicate) {
        for (T t : value) {
            if (predicate.test(t)) {
                return t;
            }
        }

        return null;
    }

    public static <T extends Entity> T findById(List<T> value, Integer id) {
        for (T t : value) {
            if (t.getId().equals(id)) return t;
        }

        return null;
    }

    public static <T extends Entity> int findIndexById(List<T> value, Integer id) {
        int i = 0;

        for (T t : value) {
            if (t.getId().equals(id)) return i;
            i++;
        }

        return -1;
    }

    public static <T extends Entity> T getById(List<T> value, Integer id) {
        for (T t : value) {
            if (t.getId().equals(id)) return t;
        }

        throw new RuntimeException(String.format("Not found entity row with %s", id));
    }

    public static <T> int findIndex(List<T> value, Predicate<T> predicate) {
        int i = 0;

        for (T t : value) {
            if (predicate.test(t)) return i;
            i++;
        }

        return -1;
    }

    public static <T extends Entity> int getIndexById(List<T> value, Integer id) {
        int i = 0;

        for (T t : value) {
            if (t.getId().equals(id)) {
                return i;
            }

            i++;
        }

        throw new RuntimeException(String.format("Not found entity row with %s", id));
    }

    public static <T> List<T> limit(List<T> list, int number) {
        List<T> result = new ArrayList<>(number);

        if (list.isEmpty()) return list;

        int i = 0;
        for (T o : list) {
            result.add(o);
            i++;
            if (i >= number) {
                return result;
            }
        }

        return result;
    }

    public static Integer minutesToSeconds(int minutes) {
        return minutes * 60;
    }

    public static Integer seconds() {
        return Long.valueOf(new Date().getTime() / 1000L).intValue();
    }

    public static Integer seconds(Date date) {
        return Long.valueOf(date.getTime() / 1000L).intValue();
    }

    public static Integer secondsFrom(Date date) {
        return seconds() - seconds(date);
    }

    public static Integer secondsTo(Date date) {
        return seconds(date) - seconds();
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void throwIllegalState() {
        throw new IllegalStateException();
    }

    public static void throwIllegalStateIf(boolean value) {
        if (value) throw new IllegalStateException();
    }

    public static <T> T last(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    public static <T> T first(List<T> list) {
        if (list.isEmpty()) return null;

        return list.get(0);
    }

    public static int secondsToMinutes(Integer timer) {
        return timer / 60;
    }
}
