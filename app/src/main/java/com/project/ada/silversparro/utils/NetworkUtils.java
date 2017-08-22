package com.project.ada.silversparro.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by ankitmaheshwari on 8/18/17.
 */

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static final String HEADER_APP_NAME = "app_name";
    public static final String HEADER_APP_VERSION = "app_version";
    public static final String HEADER_LOGIN_AUTH_TOKEN = "login_auth_token";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String HTTP_HEADER_JSON = "application/json";
    public static final String HTTP_HEADER_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String ENCODING = "charset=utf-8";
    public static final long CONNECTION_TIMEOUT_GET_VALUE = 15;
    public static final long READ_TIMEOUT_GET_VALUE = 15;
    public static final long CONNECTION_TIMEOUT_POST_VALUE = 20;
    public static final long READ_TIMEOUT_POST_VALUE = 20;
    private static final MediaType JSON = MediaType.parse(HTTP_HEADER_JSON + "; " +
            ENCODING);
    private static final MediaType URLENCODED = MediaType.parse(HTTP_HEADER_FORM_URLENCODED + "; " +
            ENCODING);
    public static final String UTF_8 = "UTF-8";
    public static final String ZERO = "0";
    public static final String MD5 = "MD5";

    public static final int UNAUTHORISED_CODE = 401;
    public static final int TOKEN_EXPIRED_CODE = 498;

    private static OkHttpClient myOkHttpClient;

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }


    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getNetworkType(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return tm.getNetworkType();

    }

    private static String getStringResponse(Response response) throws NetworkException{
        try{
            return response.body().string();
        }catch (IOException ioe){
            String message = "IOException while converting response body to string: " + ioe.getMessage();
            Log.e(TAG, message, ioe);
            throw new NetworkException.Builder().cause(ioe).httpStatusCode(response.code())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(Response response, Class<T> tClass) throws NetworkException{

        Gson gson = new Gson();
        String responseString = getStringResponse(response);
        try{
            return gson.fromJson(responseString, tClass);
        }catch(JsonSyntaxException jse){
            String message = "JsonSyntaxException while parsing response string to " + tClass.getSimpleName()
                    + ", responseString: " + responseString;
            Log.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse)
                    .httpStatusCode(response.code())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(Response response, Type typeOfT) throws NetworkException{

        Gson gson = new Gson();
        String responseString = getStringResponse(response);

        try{
            return gson.fromJson(responseString, typeOfT);
        }catch(JsonSyntaxException jse){
            String message = "JsonSyntaxException while parsing response string to " + typeOfT.toString()
                    + ", responseString: " + responseString;
            Log.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse).httpStatusCode(response.code()).errorMessage(message).build();
        }
    }

    private static NetworkException convertResponseToException(Response response) {
        String requestUrl = response.request().urlString();
        String method = response.request().method();
        int responseCode = response.code();
        String messageFromServer = response.message();
        try {
            messageFromServer = response.body().string();
        }catch (IOException ioe){
            Log.e(TAG, "Can't fetch response body");
        }

        String why = null;
        int failureType;
        if (TOKEN_EXPIRED_CODE == responseCode) {
            // Login auth token expired, special handling
            why = "Login Auth Token expired";
            failureType = FailureType.TOKEN_EXPIRED;
        } else {
            why = method + " response not successful for URL: " + requestUrl;
            failureType = FailureType.RESPONSE_FAILURE;
        }
        return new NetworkException.Builder().errorMessage(why).failureType(failureType)
                .httpStatusCode(response.code())
                .messageFromServer(messageFromServer)
                .build();
    }

    public static <T> T handleResponse(Response response, Class<T> mWrapperClass) throws NetworkException {
        T wrapperObj = null;
        if (null != response) {
            if (response.isSuccessful()) {
                wrapperObj = parseSuccessResponse(response, mWrapperClass);
                return wrapperObj;
            } else {
                //Failure Scenarios
                throw convertResponseToException(response);
            }
        } else {
            //response obtained from OkHttp is null
            String why = "Null Response obtained from URL: " + response.request().urlString();
            throw new NetworkException.Builder().errorMessage(why).failureType(FailureType.RESPONSE_FAILURE)
                    .build();
        }
    }

    public static <T> T handleResponse(Response response, Type typeOfT) throws NetworkException {
        T wrapperObj = null;
        if (null != response) {
            if (response.isSuccessful()) {
                wrapperObj = parseSuccessResponse(response, typeOfT);
                return wrapperObj;
            } else {
                //Failure Scenarios
                throw convertResponseToException(response);
            }
        } else {
            //response obtained from OkHttp is null
            String why = "Null Response obtained from URL: " + response.request().urlString();
            throw new NetworkException.Builder().errorMessage(why).failureType(FailureType.RESPONSE_FAILURE)
                    .build();
        }
    }

    public static NetworkException wrapIOException(Request request, IOException ioe){
        String requestUrl = request.urlString();
        String method = request.method();
        return new NetworkException.Builder().cause(ioe).failureType(FailureType.REQUEST_FAILURE)
                .errorMessage(method + " request failed for URL: " +
                        requestUrl).build();
    }

}
