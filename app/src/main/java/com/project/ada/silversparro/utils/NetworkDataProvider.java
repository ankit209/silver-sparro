package com.project.ada.silversparro.utils;

import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Created by ankitmaheshwari on 8/18/17.
 */

public class NetworkDataProvider {

    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String HTTP_HEADER_JSON = "application/json";
    public static final String HTTP_HEADER_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String ENCODING = "charset=utf-8";
    public static final long CONNECTION_TIMEOUT_POST_VALUE = 30;
    public static final long READ_TIMEOUT_POST_VALUE = 30;
    public static final long WRITE_TIMEOUT_POST_VALUE = 30;
    private static final MediaType JSON = MediaType.parse(HTTP_HEADER_JSON + "; " +
            ENCODING);
    private static final MediaType URLENCODED = MediaType.parse(HTTP_HEADER_FORM_URLENCODED + "; " +
            ENCODING);

    private static OkHttpClient myOkHttpClient;

    private static final String TAG = "NetworkDataProvider";

    /**
     * Class based get call. Use this when expecting the response as plain array of json
     *
     * @param url           to be hit
     * @param responseClass class of the response object
     * @param <T>           response object type
     * @return ServerResponse object wrapping desired object of type T
     * @throws NetworkException while handling you must check for httpStatusCode
     */

    public static <T> T doGetCall(String url, Class<T> responseClass)
            throws NetworkException {
        Response response = getResponseForGetCall(url);
        T responseObject = NetworkUtils.handleResponse(response, responseClass);
        return responseObject;
    }

    public static <R> void doGetCallAsync(String url, NetworkAsyncCallback<R> cb) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    /**
     * Returns the raw response for GET call
     */
    public static Response getResponseForGetCall(String url)
            throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Request.Builder requestBuilder = new Request.Builder().url(url);
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        Log.d(TAG, "Url for GET request: " + url);
        try {
            Response response = call.execute();
            return response;
        } catch (IOException e) {
            throw NetworkUtils.wrapIOException(request, e);
        }
    }

    public static <T> T doPostCall(String url, JSONObject jsonData,
                                   Class<T> responseClass) throws NetworkException {
        return doPostCall(url, convertJSONdataToBody(jsonData), responseClass);
    }

    public static <T> T doPostCall(String url, String jsonString,
                                   Class<T> responseClass) throws NetworkException {
        return doPostCall(url, convertStringdataToBody(jsonString), responseClass);
    }

    public static <R> void doPostCallAsync(String url, String jsonString,
                                                                NetworkAsyncCallback<R> cb) {
        RequestBody body = RequestBody.create(JSON, jsonString);
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .header(CONTENT_TYPE_TAG, HTTP_HEADER_JSON)
                .post(body);
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    private static RequestBody convertJSONdataToBody(JSONObject jsonData) {
        RequestBody body;
        if (jsonData != null) {
            body = RequestBody.create(JSON, jsonData.toString());
        } else {
            body = RequestBody.create(JSON, "");
        }
        return body;
    }

    private static RequestBody convertStringdataToBody(String jsonData) {
        RequestBody body;
        if (jsonData != null) {
            body = RequestBody.create(JSON, jsonData);
        } else {
            body = RequestBody.create(JSON, "");
        }
        return body;
    }

    private static <T> T doPostCall(String url, RequestBody body,
                                    Class<T> responseClass) throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Log.d(TAG, "Url for POST request: " + url);
        Response response = getResponseForPostCall(url, body);
        T responseObject = NetworkUtils.handleResponse(response, responseClass);
        return responseObject;
    }

    private static Response getResponseForPostCall(String url, RequestBody body)
            throws NetworkException {
        Request.Builder builder = new Request.Builder().url(url)
                .addHeader(CONTENT_TYPE_TAG, HTTP_HEADER_JSON)
                .post(body);
        Request request = builder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        Log.d(TAG, "Url for POST request: " + url + " Header " + request.headers().toString());
        try {
            Response response = call.execute();
            return response;
        } catch (IOException ioe) {
            throw NetworkUtils.wrapIOException(request, ioe);
        }
    }

    public static synchronized OkHttpClient getSingleOkHttpClient() {
        if (myOkHttpClient == null) {
            myOkHttpClient = new OkHttpClient();
            myOkHttpClient.setConnectTimeout(CONNECTION_TIMEOUT_POST_VALUE, TimeUnit.SECONDS);
            myOkHttpClient.setReadTimeout(READ_TIMEOUT_POST_VALUE, TimeUnit.SECONDS);
            myOkHttpClient.setWriteTimeout(WRITE_TIMEOUT_POST_VALUE, TimeUnit.SECONDS);
        }
        return myOkHttpClient;
    }

}
