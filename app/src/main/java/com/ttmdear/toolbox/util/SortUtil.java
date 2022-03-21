package com.ttmdear.toolbox.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class SortUtil {
    private SortUtil() {

    }

    public static <T> List<T> sort(List<T> list, Comparator<T> comparator) {
        int pointer = list.size() - 1;
        boolean wasSwap = true;

        while (wasSwap) {
            wasSwap = false;
            for (int i = 0; i < pointer; i++) {
                if (comparator.compare(list.get(i), list.get(i + 1)) > 0) {
                    T tmp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, tmp);
                    wasSwap = true;
                }
            }
        }

        return list;
    }

    @SuppressWarnings("rawtypes")
    public static <T> List<T> sortAsc(List<T> list, Function<T, ? super Comparable<?>> field) {
        return sort(list, (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null) return -1;
            else if (o2 == null) return 1;

            Comparable a = (Comparable) field.apply(o1);
            Comparable b = (Comparable) field.apply(o2);

            if (a == null) return -1;
            else if (b == null) return 1;

            return a.compareTo(b);
        });
    }

    public static <T extends Comparable<T>> List<T> sortAsc(List<T> list) {
        return sort(list, (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null) return -1;
            else if (o2 == null) return 1;

            return o1.compareTo(o2);
        });
    }

    @SuppressWarnings("rawtypes")
    public static <T> List<T> sortDesc(List<T> list, Function<T, ? super Comparable<?>> field) {
        return sort(list, (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null) return 1;
            else if (o2 == null) return -1;

            Comparable a = (Comparable) field.apply(o1);
            Comparable b = (Comparable) field.apply(o2);

            if (a == null) return 1;
            else if (b == null) return -1;

            return revertCompare(a.compareTo(b));
        });
    }

    public static <T extends Comparable<T>> List<T> sortDesc(List<T> list) {
        return sort(list, (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null) return 1;
            else if (o2 == null) return -1;

            return revertCompare(o1.compareTo(o2));
        });
    }

    private static int revertCompare(int result) {
        if (result < 0) return 1;
        if (result > 0) return -1;

        return 0;
    }
}
