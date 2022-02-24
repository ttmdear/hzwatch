package com.example.hzwatch.service;

public class Services {
    private static Services instance;

    private Storage storage = null;
    private HzwatchService hzwatchService = null;
    private UiService uiService;

    private Services() {

    }

    public static Services getInstance() {
        if (instance == null) {
            instance = new Services();
        }

        return instance;
    }

    public static Storage getStorage() {
        Services services = getInstance();

        if (services.storage == null) {
            services.storage = new Storage();
        }

        return services.storage;
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
