package com.example.hzwatch.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.hzwatch.R;
import com.example.hzwatch.domain.HagglezonResponse;
import com.example.hzwatch.domain.HagglezonResponse.Price;
import com.example.hzwatch.domain.HagglezonResponse.Product;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.domain.SearchLog;
import com.example.hzwatch.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatcherService extends Service implements Runnable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();
    private MediaPlayer player;

    private static final String TAG = "WatcherService";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Storage storage = Storage.getInstance();
    private Date lastSearch;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        player = MediaPlayer.create(this, R.raw.alarm);
        new Thread(this).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        lastSearch = Util.date();

        while(true) {
            while(Util.find(storage.findPriceError(), priceError -> !priceError.isChecked()) != null) {
                player.start();
            }

            if (Util.secondsFrom(lastSearch) > 10) {
                // runSeach();

                lastSearch = Util.date();
            }

            Util.sleep(500);
        }
    }

    private void runSeach() {
        String searchKeyString = storage.getSearchKeyList();
        searchKeyString = "smartphone,car,phone";

        if (searchKeyString == null || searchKeyString.isEmpty()) {
            return;
        }

        String[] searchKeyArray = searchKeyString.split(",");

        for (String searchKey : searchKeyArray) {
            searchKey = searchKey.trim();

            if (searchKey.isEmpty()) continue;

            SearchLog log = new SearchLog();
            log.setId(storage.id());
            log.setAt(Util.date());
            log.setItemsNumber(0);

            int page = 0;
            while(true) {
                HagglezonResponse response = search(searchKey, ++page);

                if (isEmptyResponse(response)) {
                    break;
                }

                log.setItemsNumber(log.getItemsNumber() + response.getData().getSearchProducts().getProducts().size());

                processResponse(searchKey, response);

                Util.sleep(5 + RANDOM.nextInt(5));
            }

            storage.create(log);
        }
    }

    private boolean isEmptyResponse(HagglezonResponse response) {
        return response == null ||
            response.getData() == null ||
            response.getData().getSearchProducts() == null ||
            response.getData().getSearchProducts().getProducts() == null ||
            response.getData().getSearchProducts().getProducts().isEmpty();
    }

    private void processResponse(String searchKey, HagglezonResponse response) {
        for (Product product : response.getData().getSearchProducts().getProducts()) {
            PriceError priceError = checkPriceError(product);
            if (priceError != null) {
                processPriceError(priceError);
            }
        }
    }

    private void processPriceError(PriceError priceError) {
        storage.create(priceError);
    }

    private PriceError checkPriceError(Product product) {
        if (product.getPrices().size() <= 1) return null;

        List<Price> priceList = Util.filter(product.getPrices(), price -> price.getPrice() != null);

        if (priceList.size() <= 1) return null;

        for(int i=0; i<priceList.size(); i++) {
            double price = priceList.get(i).getPrice();
            double avr = 0;
            int divider = 0;

            for(int j=0; j<priceList.size(); j++) {
                if (i == j) continue;

                divider++;
                avr += priceList.get(j).getPrice();
            }

            if (price <= (avr / divider) * 0.5) {
                PriceError priceError = new PriceError();
                priceError.setId(storage.id());
                priceError.setProduct(product.getTitle());
                priceError.setUrl(priceList.get(i).getUrl());
                priceError.setChecked(false);

                return priceError;
            }
        }

        return null;
    }

    private HagglezonResponse search(String search, int page) {
        String body = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"" + search + "\",\"page\":" + page + ",\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

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
            .addHeader("Referer", "https://www.hagglezon.com/en/s/" + search)
            .addHeader("Sec-Ch-Ua", " Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98")
            .addHeader("Sec-Ch-Ua-Mobile", " ?0")
            .addHeader("Sec-ch-ua-Platform", "Windows")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("User-Agent", "Chrome/98.0.4758.102 Safari/537.36")
            .post(RequestBody.create(body, JSON))
            .build();

       OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            return OBJECT_MAPPER.readValue(response.body().string(), HagglezonResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
