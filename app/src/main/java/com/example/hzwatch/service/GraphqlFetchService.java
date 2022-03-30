package com.example.hzwatch.service;

import com.example.hzwatch.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GraphqlFetchService implements FetchService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<Product> fetch(String searchKey) {
        int page = 1;
        String body = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"" + searchKey + "\",\"page\":" + page + ",\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

        Request request = new Request.Builder()
            .url("https://graphql.hagglezon.com")
            .addHeader("Accept", "*/*")
            // .addHeader("Accept-encoding", "gzip, deflate, br")
            .addHeader("Accept-Language", "pl,en-US;q=0.7,en;q=0.3")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Connection", "keep-alive")
            .addHeader("Content-Length", String.valueOf(body.length()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "_ga=GA1.2.1390425837.1645716030; _gid=GA1.2.1091289632.1646823835; _gat=1; __cf_bm=RlQUmieUxB2TNW8yP0lvEFJMNDYlsGZCaKfJhAJK4pU-1646823835-0-AWmwu34jggxGAP4FeMKqI9MXtDV3slMYAxF1Qi9WE9mWoWYf5p221qqKJgKFbude3wMEsGMAhpuNdoq/eKB+SbYDGnF7AbXEkaFzBw8ANSwRh1wMFT+w+pxalPB0XIETxQ==")
            .addHeader("Host", "graphql.hagglezon.com")
            .addHeader("Origin", "https://www.hagglezon.com")
            .addHeader("Referer", "https://www.hagglezon.com/")
            .addHeader("Pragma", "no-cache")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-site")
            .addHeader("Sec-Ch-Ua", " Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99")
            .addHeader("Sec-Ch-Ua-mobile", "?0")
            .addHeader("Sec-Ch-Ua-platform", "Windows")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0")
            .post(RequestBody.create(body, JSON))
            .build();

        OkHttpClient client = new OkHttpClient();

        // try (Response response = client.newCall(request).execute()) {
        //     // String content = response.body().string();
        //     // logger.log("Response for key [%s], page %s, content [%s]", search, page, content);
        //     // return OBJECT_MAPPER.readValue(content, HagglezonResponse.class);
        // } catch (IOException e) {
        //     // logger.log("IO exception for key [%s], page %s, message [%s]", search, page, e.getMessage());
        // }

        return null;
    }
}
