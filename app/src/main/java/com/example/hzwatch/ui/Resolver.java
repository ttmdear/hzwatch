package com.example.hzwatch.ui;

@FunctionalInterface
public interface Resolver<T, R> {
    R resolve(T value);
}
