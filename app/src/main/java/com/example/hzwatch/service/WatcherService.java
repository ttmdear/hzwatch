package com.example.hzwatch.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatcherService extends Service implements Runnable {
    private static final String TAG = "WatcherService";

    public static final String ACTION_CHANGE = "WatcherService.Action.Change";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();
    private MediaPlayer playerAlarm;
    private MediaPlayer playerBeep;
    private Thread thread;
    private boolean stop = false;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Storage storage = Services.getStorage();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: begin");
        playerAlarm = MediaPlayer.create(this, R.raw.alarm);
        playerBeep = MediaPlayer.create(this, R.raw.beep_long);

        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        Date lastSearch = Util.date();

        while(true) {
            if (stop) return;

            while(storage.getPriceError()) {
                playerBeep.start();
                Util.sleep(100);
            }

            if (Util.secondsFrom(lastSearch) > 120) {
                runSearch();
                playerBeep.start();
                lastSearch = Util.date();
                sendBroadcastActionChange();
            }

            Util.sleep(500);
        }
    }

    private void runSearch() {
        Log.d(TAG, "runSearch: begin");

        String searchKeyString = storage.getSearchKeyList();
        Log.d(TAG, "runSearch: searchKeyString " + searchKeyString);

        if (searchKeyString == null || searchKeyString.isEmpty()) {
            return;
        }

        String[] searchKeyArray = searchKeyString.split(",");

        for (String searchKey : searchKeyArray) {
            searchKey = searchKey.trim();
            Log.d(TAG, "runSearch: searchKey " + searchKey);

            if (searchKey.isEmpty()) continue;

            SearchLog log = new SearchLog();
            log.setSearchKey(searchKey);
            log.setId(storage.id());
            log.setAt(Util.date());
            log.setProductsNumber(0);

            int page = 0;
            while(true) {
                HagglezonResponse response = search(searchKey, ++page);
                Log.d(TAG, "runSearch: response " + response);

                if (isEmptyResponse(response)) {
                    break;
                }

                log.setProductsNumber(log.getProductsNumber() + response.getData().getSearchProducts().getProducts().size());

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
        Log.d(TAG, "processResponse: for searchKey " + searchKey);

        for (Product product : response.getData().getSearchProducts().getProducts()) {
            PriceError priceError = checkPriceError(product);
            if (priceError != null) {
                processPriceError(priceError);
            }
        }
    }

    private void processPriceError(PriceError priceError) {
        storage.create(priceError);
        storage.setPriceError(true);
        sendBroadcastActionChange();
    }

    private void sendBroadcastActionChange() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_CHANGE));
    }

    private PriceError checkPriceError(Product product) {
        if (product.getPrices().size() <= 1) return null;

        boolean saved = Util.find(storage.findPriceError(), o -> o.getProduct().equals(product.getTitle())) != null;

        if (saved) return null;

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
                priceError.setAt(Util.date());
                priceError.setPrice(price);
                priceError.setAvr(avr);

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
            .addHeader("Cookie", "_ga=GA1.2.1390425837.1645716030; _gid=GA1.2.1282820129.1645716030; _gat=1; __cf_bm=VcyJK_IqrV3vEH0LXAsTpOc4na6pC1t2_IaVhaGv88o-1645716029-0-AdEGnr2mLzVEQ0FF6hkpSNiI+1HSQwRHz2H+wtuFSh2Ln2S7MHL2ZE07K+fuP+q9rwXBzZP2gh2Vgb/x08tRaFAthpNtmCqLPfZnPEgEIpySjJmNHyD4RAZ80YumCfy8ng==")
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
