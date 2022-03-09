package com.example.hzwatch.worker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Data;
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
import com.example.hzwatch.service.Logger;
import com.example.hzwatch.service.ProductProcessor;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.UiService;
import com.example.hzwatch.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Random;
import java.util.TreeMap;
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
    private final Logger logger = Services.getLogger();
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
        logger.log("Process search for key [%s]", searchKey);

        int productsNumber = 0;
        int page = 0;

        while (true) {
            sendBroadcastActionStateChange(String.format("Szukam %s, liczba produktów %s", searchKey, productsNumber));

            HagglezonResponse response = search(searchKey, ++page);

            if (isEmptyResponse(response)) {
                logger.log("Response is empty.");
                break;
            }

            productsNumber += response.getData().getSearchProducts().getProducts().size();
            processResponse(searchKey, response);

            try {
                Thread.sleep((2 + RANDOM.nextInt(5)) * 1000);
            } catch (InterruptedException e) {
                logger.log("Thread interrupted. Message [%s].", e.getMessage());
            }
        }

        hzwatchService.postSearch(searchKey, productsNumber);

        sendBroadcastActionStateChange(String.format("Przeszukałem %s produktów dla słowa %s", productsNumber, searchKey));
        sendBroadcastActionChange();

        // playerBeep.start();
    }

    private HagglezonResponse search(String search, int page) {
        logger.log("Search key [%s], page [%s]", search, page);
        String body = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"" + search + "\",\"page\":" + page + ",\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

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

        try (Response response = client.newCall(request).execute()) {
            String content = response.body().string();
            logger.log("Response for key [%s], page %s, content [%s]", search, page, content);
            return OBJECT_MAPPER.readValue(content, HagglezonResponse.class);
        } catch (IOException e) {
            logger.log("IO exception for key [%s], page %s, message [%s]", search, page, e.getMessage());
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
        Data data = new Data.Builder()
            .putString("MODE", "NORMAL")
            .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatcherWorker.class)
            .setInitialDelay(60, TimeUnit.SECONDS)
            .setInputData(data)
            .build();

        WorkManager.getInstance(context).enqueueUniqueWork(WORKER_TAG, ExistingWorkPolicy.KEEP, request);
        // WorkManager.getInstance(context).enqueue(request);

        Services.getLogger().log("Next work planned.");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void planPeriodicWork(Context context) {
        // Constraints constraints = new Constraints.Builder()
        //     .setRequiresDeviceIdle(true)
        //     .build();

        Data data = new Data.Builder()
            .putString("MODE", "PERIODIC")
            .build();

        // PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(WatcherWorker.class, 15L, TimeUnit.MINUTES)

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(WatcherWorkerPeriodic.class, 15L, TimeUnit.MINUTES)
            // .setConstraints(constraints)
            .setInputData(data)
            .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORKER_PERIODIC_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);

        Services.getLogger().log("Next periodic work planned.");
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            doWorkInner(getInputData().getString("MODE"));
        } catch (Exception exception) {
            logger.log(exception.getMessage());
        }


        new Thread(() -> {
            Util.sleep(1000);
            planWork(context);
        }).start();

        return Result.success();
    }

    public void doWorkInner(String mode) {
        logger.log("Do work in mode %s", mode);

        if (hzwatchService.isPriceError()) {
            playerBeep.start();
        }

        String searchKey;

        while((searchKey = hzwatchService.getNextSearchKeyToSearch()) != null) {
            processSearch(searchKey);
        }
    }
}
