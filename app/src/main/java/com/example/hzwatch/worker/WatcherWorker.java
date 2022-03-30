package com.example.hzwatch.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.example.hzwatch.R;
import com.example.hzwatch.domain.PriceError;
import com.example.hzwatch.service.FetchService;
import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Logger;
import com.example.hzwatch.domain.Product;
import com.example.hzwatch.service.ProductProcessor;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;
import com.example.hzwatch.util.Util;

import java.util.Date;
import java.util.List;

public class WatcherWorker {
    public static final String ACTION_CHANGE = "WatcherWorker.Action.Change";
    public static final String ACTION_STATE_CHANGE = "WatcherWorker.Action.State.Change";

    private final Context context;
    private final HzwatchService hzwatchService = Services.getHzwatchService();
    private final Logger logger = Services.getLogger();
    private final ProductProcessor productProcessor = Services.getProductProcessor();
    private final FetchService fetchService = Services.getFetchService();
    private final Storage storage = Services.getStorage();

    private final MediaPlayer playerAlarm;
    private final MediaPlayer playerBeep;

    public WatcherWorker(@NonNull Context context) {
        playerAlarm = MediaPlayer.create(context, R.raw.alarm);
        playerBeep = MediaPlayer.create(context, R.raw.beep_long);

        this.context = context;
    }

    public static void planWatcherAlarm(Context context) {
        // co 3 min, 5 co strona, zablokowane po ok 3h
        // co 2 min, 7 co strona
        HzwatchService hzwatchService = Services.getHzwatchService();

        if (hzwatchService.isWatcherAlarmPlanned()) {
            return;
        }

        // Date plannedAt = Util.datePlusMinutes(new Date(), 3);
        Date plannedAt = Util.datePlusSeconds(new Date(), 30);

        hzwatchService.saveWatcherAlarmPlanned(plannedAt);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WatcherAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, plannedAt.getTime(), pendingIntent);

        Services.getLogger().log("Next work planned.");
    }

    public void doWork() {
        try {
            doWorkInner();
        } catch (Exception exception) {
            logger.log(exception.getMessage());
        }
    }

    public void doWorkInner() {
        logger.log("Do work");

        // if (hzwatchService.isPriceError()) {
        //     new Thread(playerBeep::start).start();
        // }

        String searchKey = hzwatchService.getNextSearchKeyToSearch();

        if (searchKey == null) {
            return;
        }

        logger.log("Search for key [%s]", searchKey);


        List<Product> products = fetchService.fetch(searchKey);

        sendBroadcastActionStateChange(String.format("Szukam %s, liczba produktów %s", searchKey, products.size()));

        for (Product product : products) {
            if (!product.isPriceError()) {
                continue;
            }

            processPriceError(searchKey, product);
        }

        hzwatchService.postSearch(searchKey, products.size());

        sendBroadcastActionStateChange(String.format("Przeszukałem %s produktów dla słowa %s", products.size(), searchKey));
        sendBroadcastActionChange();
    }

    private void processPriceError(String searchKey, Product product) {
        PriceError priceError = hzwatchService.getPriceErrorByHzId(product.getId());

        if (priceError == null) {
            priceError = new PriceError();
            priceError.setId(storage.id());

            storage.create(priceError);
        }

        priceError.setHzId(product.getId());
        priceError.setProduct(product.getTitle());
        priceError.setSearchKey(searchKey);
        priceError.setPriceSum(product.getSum());
        priceError.setAt(Util.date());
        priceError.setAvr(product.getAvr());
        priceError.setPrice(0.0);
        priceError.setMoved(false);

        storage.setPriceError(true);
    }

    private void runPriceErrorAlarm() {
        playerBeep.start();
    }

    private void sendBroadcastActionChange() {
        Intent intent = new Intent();
        intent.setAction(ACTION_CHANGE);
        context.sendBroadcast(intent);
    }

    private void sendBroadcastActionStateChange(String msg) {
        Intent intent = new Intent();
        intent.setAction(ACTION_STATE_CHANGE);
        intent.putExtra("msg", msg);
        context.sendBroadcast(intent);
    }
}
