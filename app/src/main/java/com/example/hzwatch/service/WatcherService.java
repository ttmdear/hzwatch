package com.example.hzwatch.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hzwatch.R;
import com.example.hzwatch.domain.HagglezonResponse;
import com.example.hzwatch.domain.HagglezonResponse.Product;
import com.example.hzwatch.service.ProductProcessor.ProcessProductResult;
import com.example.hzwatch.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatcherService extends Service implements Runnable {

    public static final String ACTION_CHANGE = "WatcherService.Action.Change";
    private static final String TAG = "WatcherService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    private final Logger logger = Services.getLogger();
    private final HzwatchService hzwatchService = Services.getHzwatchService();
    private final ProductProcessor productProcessor = Services.getProductProcessor();
    private MediaPlayer playerAlarm;
    private MediaPlayer playerBeep;
    private Thread thread;
    private boolean stop = false;

    private void checkPriceErrorState() {
        while (hzwatchService.isPriceError()) {
            playerBeep.start();
            Util.sleep(1000);
        }
    }

    private boolean isEmptyResponse(HagglezonResponse response) {
        return response == null ||
            response.getData() == null ||
            response.getData().getSearchProducts() == null ||
            response.getData().getSearchProducts().getProducts() == null ||
            response.getData().getSearchProducts().getProducts().isEmpty();
    }

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
        playerAlarm = MediaPlayer.create(this, R.raw.alarm);
        playerBeep = MediaPlayer.create(this, R.raw.beep_long);

        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void processResponse(String searchKey, HagglezonResponse response) {
        for (Product product : response.getData().getSearchProducts().getProducts()) {
            ProcessProductResult result = productProcessor.process(searchKey, product);

            if (result.isPriceError()) {
                sendBroadcastActionChange();
                checkPriceErrorState();
            }
        }
    }

    private void processSearch() {
        String searchKey = hzwatchService.getNextSearchKeyToSearch();

        if (searchKey == null) return;

        logger.log(String.format("Process search for search key [%s]", searchKey));

        int productsNumber = 0;
        int page = 0;

        // String hzCookie = resolveHzCookie();

        while (true) {
            HagglezonResponse response = null;
            response = search(searchKey, ++page);

            if (isEmptyResponse(response)) {
                break;
            }

            productsNumber += response.getData().getSearchProducts().getProducts().size();
            processResponse(searchKey, response);

            Util.sleep(5 + RANDOM.nextInt(20));
        }

        hzwatchService.postSearch(searchKey, productsNumber);
    }

    // public String resolveHz2Cookie() throws IOException {
    //     URL url = new URL("http://www.android.com/");
    //     HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    //     try {
    //         InputStream in = new BufferedInputStream(urlConnection.getInputStream());
    //         readStream(in);
    //     } finally {
    //         urlConnection.disconnect();
    //     }

    //     return ""
    // }

    // // Plain Java
    // private static String convertInputStreamToString(InputStream is) throws IOException {
    //     ByteArrayOutputStream result = new ByteArrayOutputStream();
    //     byte[] buffer = new byte[8192];
    //     int length;
    //     while ((length = is.read(buffer)) != -1) {
    //         result.write(buffer, 0, length);
    //     }

    //     // Java 1.1
    //     //return result.toString(StandardCharsets.UTF_8.name());

    //     return result.toString("UTF-8");

    //     // Java 10
    //     //return result.toString(StandardCharsets.UTF_8);
    // }

    public String resolveHzCookie() {
        Request request = new Request.Builder()
            .url("https://www.hagglezon.com/")

            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept-language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Cache-control", "no-cache")
            .addHeader("Pragma", "no-cache")
            // .addHeader("Sec-Ch-Ua", " Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98")
            // .addHeader("Sec-Ch-Ua-Mobile", " ?0")
            // .addHeader("Sec-ch-ua-Platform", "Windows")
            // .addHeader("Sec-Fetch-Dest", "document")
            // .addHeader("Sec-Fetch-Mode", "navigate")
            // .addHeader("Sec-Fetch-Site", "none")
            // .addHeader("Sec-Fetch-User", "?1")
            // .addHeader("Upgrade-Insecure-Requests", "1")
            // .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .build();

        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            System.out.printf(response.body().toString());
            return "213";
            // return OBJECT_MAPPER.readValue(response.body().string(), HagglezonResponse.class);
        } catch (IOException e) {
            logger.log(String.format("There is error [%s]", e.getMessage()));
        }

        return "";
    }

    @Override
    public void run() {
        // Delay to wait for UI thread
        Util.sleep(5000);

        while (true) {
            if (stop) return;

            checkPriceErrorState();
            processSearch();

            Util.sleep(1000);
        }
    }

    private HagglezonResponse search2(String search, int page) throws IOException {
        String body = "{\"operationName\":\"SearchResults\",\"variables\":{\"lang\":\"en\",\"currency\":\"EUR\",\"filters\":{},\"search\":\"" + search + "\",\"page\":" + page + ",\"country\":\"de\"},\"query\":\"query SearchResults($search: String!, $country: String, $currency: String!, $lang: String!, $page: Int, $filters: SearchFilters) {\\n  searchProducts(searchTerm: $search, country: $country, productConfig: {language: $lang, currency: $currency}, page: $page, filters: $filters) {\\n    products {\\n      id\\n      title\\n      brand\\n      tags\\n      related_items\\n      prices {\\n        country\\n        price\\n        currency\\n        url\\n        __typename\\n      }\\n      all_images {\\n        medium\\n        large\\n        __typename\\n      }\\n      __typename\\n    }\\n    next {\\n      country\\n      page\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";

        URL myURL = new URL("https://graphql.hagglezon.com");
        HttpURLConnection myURLConnection = (HttpURLConnection)myURL.openConnection();

        // String userCredentials = "username:password";
        // String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        myURLConnection.setRequestMethod("GET");
        myURLConnection.setRequestProperty("Accept", "*/*");
        // myURLConnection.setRequestProperty("Accept-encoding", "gzip, deflate, br");
        myURLConnection.setRequestProperty("Accept-language", "pl,en-US;q=0.7,en;q=0.3");
        // myURLConnection.setRequestProperty("Connection", "keep-alive");
        myURLConnection.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
        myURLConnection.setRequestProperty("Content-type", "application/json");
        myURLConnection.setRequestProperty("Cookie", "_ga=GA1.2.1772071147.1646316036; _gid=GA1.2.692908942.1646316036; _gat=1; __cf_bm=zi6RxMLd14EM94zxSh4sV7606H6PkoTHimHsauvMPvQ-1646316034-0-ASy94XLJVL6xCMTNjMEgemJsJqOAp8IuyRmDhLzOkXYnQJU9LcyUH8ILpz9RyXt6dWIdQAFXr+UurqulxnQfev3dKURAJ0mrELVCmAZ6gp+T/UuK6JuCYuN1T0FGrDUnHA==");
        myURLConnection.setRequestProperty("Host", "graphql.hagglezon.com");
        myURLConnection.setRequestProperty("Origin", "https://www.hagglezon.com");
        myURLConnection.setRequestProperty("Referer", "https://www.hagglezon.com/");
        myURLConnection.setRequestProperty("Sec-Fetch-Dest", "empty");
        myURLConnection.setRequestProperty("Sec-Fetch-Mode", "cors");
        myURLConnection.setRequestProperty("Sec-Fetch-Site", "same-site");
        // myURLConnection.setRequestProperty("TE", "trailers");
        myURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0");
        // myURLConnection.setRequestProperty("User-Agent", "Firefox/90.0");

        // myURLConnection.setUseCaches(false);
        // myURLConnection.setDoInput(true);
        // myURLConnection.setDoOutput(true);

        BufferedReader br = null;
        if (myURLConnection.getResponseCode() == 200) {
            br = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    System.out.println(strCurrentLine);
                }
        } else {
            br = new BufferedReader(new InputStreamReader(myURLConnection.getErrorStream()));
            String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    System.out.println(strCurrentLine);
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
            .addHeader("Accept-Language", "pl,en-US;q=0.7,en;q=0.3")
            .addHeader("Connection", "keep-alive")
            .addHeader("Content-Length", String.valueOf(body.length()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Cookie", "_ga=GA1.2.1772071147.1646316036; _gid=GA1.2.692908942.1646316036; __cf_bm=PH80s6kX5nq0ek5.yysxNjLhf2Na_5jJSUf8c2cpKGo-1646317369-0-ATtdPufzytY608+eBOyNrp2XHXzOgdfNjWVhAkt9GtVE10S+EoNYJCNdHRe3p4Vpy0NRlmUW2UJOr+oJbx1B5Cfdwaum0vthN1yGLwAXLk0cHLfgKavUQ/wqtz1WVa3yWA==; _gat=1")
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
            logger.log(String.format("There is error [%s]", e.getMessage()));
        }

        return null;
    }

    private void sendBroadcastActionChange() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_CHANGE));
    }
}
