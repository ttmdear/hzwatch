package com.example.hzwatch.worker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.hzwatch.R;
import com.example.hzwatch.domain.HagglezonResponse;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.LoggerService;
import com.example.hzwatch.service.ProductProcessor;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatcherWorker extends Worker {
    public static final String ACTION_CHANGE = "WatcherWorker.Action.Change";
    public static final String ACTION_STATE_CHANGE = "WatcherWorker.Action.State.Change";
    private static final String TAG = "WatcherWorker";
    private static final String WORKER_TAG = "WatcherWorker";
    private static final String WORKER_PERIODIC_TAG = "WatcherPeriodicWorker";

    private final Context context;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    private final HzwatchService hzwatchService = Services.getHzwatchService();
    private final LoggerService loggerService = Services.getLoggerService();
    private final ProductProcessor productProcessor = Services.getProductProcessor();

    private MediaPlayer playerAlarm;
    private MediaPlayer playerBeep;
    private boolean stop = false;

    public WatcherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        playerAlarm = MediaPlayer.create(context, R.raw.alarm);
        playerBeep = MediaPlayer.create(context, R.raw.beep_long);

        this.context = context;
    }

    private void runPriceErrorAlarm() {
        playerBeep.start();
    }

    private boolean isEmptyResponse(HagglezonResponse response) {
        return response == null ||
            response.getData() == null ||
            response.getData().getSearchProducts() == null ||
            response.getData().getSearchProducts().getProducts() == null ||
            response.getData().getSearchProducts().getProducts().isEmpty();
    }

    private void processResponse(String searchKey, HagglezonResponse response) {
        for (HagglezonResponse.Product product : response.getData().getSearchProducts().getProducts()) {
            ProductProcessor.ProcessProductResult result = productProcessor.process(searchKey, product);

            if (result.isPriceError()) {
                sendBroadcastActionChange();
                runPriceErrorAlarm();
            }
        }
    }

    private void processSearch(String searchKey) {
        loggerService.log(String.format("Process search for search key [%s]", searchKey));

        int productsNumber = 0;
        int page = 0;

        while (true) {
            sendBroadcastActionStateChange(String.format("Szukam '%s', liczba produktów %s", searchKey, productsNumber));

            HagglezonResponse response = search(searchKey, ++page);

            if (isEmptyResponse(response)) {
                break;
            }

            productsNumber += response.getData().getSearchProducts().getProducts().size();
            processResponse(searchKey, response);

            Util.sleep((5 + RANDOM.nextInt(10)) * 1000);
        }

        hzwatchService.postSearch(searchKey, productsNumber);

        sendBroadcastActionStateChange(String.format("Przeszukałem %s produktów dla słowa %s", productsNumber, searchKey));
        sendBroadcastActionChange();

        // playerBeep.start();
    }

    private HagglezonResponse search(String search, int page) {
        String body = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"" + search + "\",\"page\":" + page + ",\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

        Request request = new Request.Builder()
            .url("https://graphql.hagglezon.com")
            .addHeader("Accept", "*/*")
            // .addHeader("Accept-encoding", "gzip, deflate, br")
            .addHeader("Accept-Language", "pl,en-US;q=0.7,en;q=0.3")
            .addHeader("Connection", "keep-alive")
            .addHeader("Content-Length", String.valueOf(body.length()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "_ga=GA1.2.1602304383.1646684304; _gid=GA1.2.1510141826.1646684304; _gat=1; __cf_bm=aBfbOheZHZ92h3XdTx7xFBcYHbUaDCt.9WMir5svTlE-1646684303-0-AZ5UM7JQNgdiLOfBoYBv98Lq0OHW2cJzm1Ot30W+Q60ZXHOQlJS/kutpkkjJgGAoyszy2dnJdmdCJti/F/0RRRD+aAys3rbGZa7vryZtOPZFVoZepdNnBGK3II4Feh5C+g==")
            .addHeader("Host", "graphql.hagglezon.com")
            .addHeader("Origin", "https://www.hagglezon.com")
            .addHeader("Referer", "https://www.hagglezon.com/")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-site")
            .addHeader("TE", "trailers")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0")
            .post(RequestBody.create(body, JSON))
            .build();

        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            return OBJECT_MAPPER.readValue(response.body().string(), HagglezonResponse.class);
        } catch (IOException e) {
            loggerService.log(String.format("There is error [%s]", e.getMessage()));
        }

        return null;
    }

    private void sendBroadcastActionChange() {
        Intent intent=new Intent();
        intent.setAction(ACTION_CHANGE);
        context.sendBroadcast(intent);
    }

    private void sendBroadcastActionStateChange(String msg) {
        Intent intent=new Intent();
        intent.setAction(ACTION_STATE_CHANGE);
        intent.putExtra("msg", msg);
        context.sendBroadcast(intent);
    }

    public static void planWork(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatcherWorker.class)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build();

        WorkManager.getInstance(context).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.APPEND, request);

        Services.getLoggerService().log("Next work planned.");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void planPeriodicWork(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(WatcherWorker.class, 15L, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORKER_PERIODIC_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);

        Services.getLoggerService().log("Next periodic work planned.");
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            doWorkInner();
        } catch (Exception exception) {
            loggerService.log(exception.getMessage());
        }

        planWork(context);
        return Result.success();
    }

    public void doWorkInner() {
        if (hzwatchService.isPriceError()) {
            playerBeep.start();
        }

        String searchKey;

        while((searchKey = hzwatchService.getNextSearchKeyToSearch()) != null) {
            processSearch(searchKey);
        }
    }
}
