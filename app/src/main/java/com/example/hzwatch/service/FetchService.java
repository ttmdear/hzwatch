package com.example.hzwatch.service;

import com.example.hzwatch.domain.Product;

import java.util.List;

public interface FetchService {
    List<Product> fetch(String searchKey);
}
