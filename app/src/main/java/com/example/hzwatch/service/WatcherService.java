package com.example.hzwatch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.hzwatch.util.Util;

import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatcherService extends Service implements Runnable {
    private static final String TAG = "WatcherService";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Date lastSearch;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        lastSearch = Util.date();

        while(true) {
            if (Util.secondsFrom(lastSearch) > 10) {
                runSearch();

                lastSearch = Util.date();
            }

            Util.sleep(500);
        }
    }

    private void runSearch() {
        String json = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"smatchpone\",\"page\":6,\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
            .url("https://graphql.hagglezon.com")
            .addHeader("Accept", "*/*")
            // .addHeader("Accept-encoding", "gzip, deflate, br")
            .addHeader("Accept-language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Cache-control", "no-cache")
            .addHeader("Content-Length", "811")
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "_ga=GA1.2.1117157207.1645480775; _gid=GA1.2.195309118.1645480775; _gat=1; __cf_bm=Ub3wU5MyLxCCoNFlL_cd3P_FCAiLj3IMxZ.YRRn8zLU-1645480774-0-AdhMrBwZwZTvCoXumnnpakxBez32tnk9Eruc0Djh2cwYYzYNJGjbiIsRWKKAcNXxrHrASSzi30R1Y66WEnb+0suY3DeAJH9LeRJTjrqhYyHvs2bko9sEKH5hqS1MHP+p+g==")
            .addHeader("Origin", "https://www.hagglezon.com")
            .addHeader("Pragma", "no-cache")
            .addHeader("Referer", "https://www.hagglezon.com/en/s/smatchpone")
            .addHeader("Sec-Ch-Ua", " Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98")
            .addHeader("Sec-Ch-Ua-Mobile", " ?0")
            .addHeader("Sec-ch-ua-Platform", "Windows")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("User-Agent", "Chrome/98.0.4758.102 Safari/537.36")
            .post(body)
            .build();

       OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            Log.d(TAG, responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
