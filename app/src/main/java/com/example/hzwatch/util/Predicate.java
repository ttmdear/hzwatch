package com.example.hzwatch.util;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
