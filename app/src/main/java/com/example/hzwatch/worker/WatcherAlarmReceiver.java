package com.example.hzwatch.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.hzwatch.service.HzwatchService;
import com.example.hzwatch.service.Services;
import com.example.hzwatch.service.Storage;

public class WatcherAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            HzwatchService hzwatchService = Services.getHzwatchService();

            WatcherWorker watcherWorker = new WatcherWorker(context);
            watcherWorker.doWork();

            hzwatchService.deleteWatcherAlarm();

            WatcherWorker.planWatcherAlarm(context);
        }).start();
    }
}
