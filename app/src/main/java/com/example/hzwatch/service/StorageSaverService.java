package com.example.hzwatch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.hzwatch.util.Util;

import java.util.Date;

public class StorageSaverService extends Service implements Runnable {
    private final Storage storage = Services.getStorage();
    private Thread thread = null;
    private boolean stop = false;

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
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        Date lastCheck = Util.date();

        while (true) {
            if (stop) return;

            if (Util.secondsFrom(lastCheck) > 10) {
                if (storage.isChange()) {
                    storage.save();
                }

                lastCheck = Util.date();
            }

            Util.sleep(500);
        }
    }
}
