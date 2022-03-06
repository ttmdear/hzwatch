package com.example.hzwatch.service;

import androidx.annotation.NonNull;

import java.lang.Thread.UncaughtExceptionHandler;

public class Services {
    private static Services instance;

    private Storage storage = null;
    private HzwatchService hzwatchService = null;
    private UiService uiService = null;
    private LoggerService loggerService = null;
    private ProductProcessor productProcessor = null;
    private final UncaughtExceptionHandler uncaughtExceptionHandler;

    private Services() {
        UncaughtExceptionHandler defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        uncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                loggerService.log(e.getMessage());

                if (defaultExceptionHandler != null) {
                    defaultExceptionHandler.uncaughtException(t, e);
                }
            }
        };
    }

    public void initDefaultUncaughtExceptionHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() != uncaughtExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
    }

    public static Services getInstance() {
        if (instance == null) {
            instance = new Services();
        }

        return instance;
    }

    public static LoggerService getLoggerService() {
        Services services = getInstance();

        if (services.loggerService == null) {
            services.loggerService = new LoggerService();
        }

        return services.loggerService;
    }

    public static Storage getStorage() {
        Services services = getInstance();

        if (services.storage == null) {
            services.storage = new Storage();
        }

        return services.storage;
    }

    public static ProductProcessor getProductProcessor() {
        Services services = getInstance();

        if (services.productProcessor == null) {
            services.productProcessor = new ProductProcessor();
        }

        return services.productProcessor;
    }

    public static HzwatchService getHzwatchService() {
        Services services = getInstance();

        if (services.hzwatchService == null) {
            services.hzwatchService = new HzwatchService();
        }

        return services.hzwatchService;
    }

    public static UiService getUiService() {
        Services services = getInstance();

        if (services.uiService == null) {
            services.uiService = new UiService();
        }

        return services.uiService;
    }
}
