package com.example.hzwatch.service;

import com.example.hzwatch.domain.Product;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HtmlFetchService implements FetchService {
    private final ParseHtmlService parseHtmlService;

    public HtmlFetchService(ParseHtmlService parseHtmlService) {
        this.parseHtmlService = parseHtmlService;
    }

    @Override
    public List<Product> fetch(String searchKey) {
        Request request = new Request.Builder()
            .url("https://www.hagglezon.com/en/s/" + searchKey)
            .addHeader("Accept", "text/html")
            .addHeader("Accept-Language", "pl,en-US;q=0.7,en;q=0.3")
            .build();

        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            return parseHtmlService.parse(response.body().string());
        } catch (IOException e) {
            // logger.log("IO exception for key [%s], page %s, message [%s]", search, page, e.getMessage());
        }

        return null;
    }
}
