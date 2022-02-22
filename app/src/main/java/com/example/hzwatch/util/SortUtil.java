package com.example.hzwatch.util;

import com.example.hzwatch.domain.Entity;

import java.util.Comparator;
import java.util.List;

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
}
