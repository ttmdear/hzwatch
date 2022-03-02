package com.example.hzwatch.util;

import com.example.hzwatch.domain.Entity;
import com.example.hzwatch.ui.Resolver;

import java.security.Provider;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class SortUtil {
    private SortUtil() {

    }

    private static int compare(Integer a, Integer b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return 1;
        } else if (b == null) {
            return -1;
        }

        return Integer.compare(a, b);
    }

    public static <T> void sort(List<T> value, Comparator<T> comparator) {
        int pointer = value.size() - 1;
        boolean wasSwap = true;

        while (wasSwap) {
            wasSwap = false;
            for (int i = 0; i < pointer; i++) {
                if (comparator.compare(value.get(i), value.get(i + 1)) > 0) {
                    T tmp = value.get(i);
                    value.set(i, value.get(i + 1));
                    value.set(i + 1, tmp);
                    wasSwap = true;
                }
            }
        }
    }

    public static <T> List<T> sortByDateDesc(List<T> value, Resolver<T, ? extends Date> provider) {
        int pointer = value.size() - 1;
        boolean wasSwap = true;

        while (wasSwap) {
            wasSwap = false;
            for (int i = 0; i < pointer; i++) {
                Date a = provider.resolve(value.get(i));
                Date b = provider.resolve(value.get(i + 1));

                if (a.compareTo(b) < 0) {
                    T tmp = value.get(i);
                    value.set(i, value.get(i + 1));
                    value.set(i + 1, tmp);
                    wasSwap = true;
                }
            }
        }

        return value;
    }

    public static <T> List<T> sortByDateAsc(List<T> value, Resolver<T, ? extends Date> provider) {
        int pointer = value.size() - 1;
        boolean wasSwap = true;

        while (wasSwap) {
            wasSwap = false;
            for (int i = 0; i < pointer; i++) {
                Date a = provider.resolve(value.get(i));
                Date b = provider.resolve(value.get(i + 1));

                if (a.compareTo(b) > 0) {
                    T tmp = value.get(i);
                    value.set(i, value.get(i + 1));
                    value.set(i + 1, tmp);
                    wasSwap = true;
                }
            }
        }

        return value;
    }

    public static <T> List<T> sortByStringAsc(List<T> value, Resolver<T, ? extends String> provider) {
        int pointer = value.size() - 1;
        boolean wasSwap = true;

        while (wasSwap) {
            wasSwap = false;
            for (int i = 0; i < pointer; i++) {
                String a = provider.resolve(value.get(i));
                String b = provider.resolve(value.get(i + 1));

                if (a.compareTo(b) > 0) {
                    T tmp = value.get(i);
                    value.set(i, value.get(i + 1));
                    value.set(i + 1, tmp);
                    wasSwap = true;
                }
            }
        }

        return value;
    }
}
