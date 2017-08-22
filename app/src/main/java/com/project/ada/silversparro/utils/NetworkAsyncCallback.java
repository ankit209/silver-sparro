package com.project.ada.silversparro.utils;

/**
 * Created by ankitmaheshwari on 8/18/17.
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 Used to make async network calls using OkHttp.
 Provides two callbacks, first <code>onNetworkFailure</code> for all the failure scenarios
 including request failure, IO failure, non success response, error in parsing response; second
 <code>onNetworkSuccess</code> when the response is succes and is successfully parsed into
 desired object. Both of them are fired on the main thread.
 Created by ankitmaheshwari1 on 22/12/15.
 */
public abstract class NetworkAsyncCallback<Wrapper> implements Callback {

    private static final String TAG = "NetworkAsyncCallback";

    private final Class<Wrapper> mWrapperClass;
    private boolean isCancelled = false;
    private static Handler mainThreadHandler;

    protected NetworkAsyncCallback() {
        super();
        this.mWrapperClass = (Class) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public static Handler getMainThreadHandler(){
        if (mainThreadHandler == null){
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mainThreadHandler;
    }

    @Override
    public final void onFailure(final Request request, final IOException e) {
        Log.d(TAG, "onFailure");
        if (isCancelled()){
            // Callback was cancelled, do nothing with the request
            return;
        }
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                onNetworkFailure(NetworkUtils.wrapIOException(request, e));
            }
        });
    }

    @Override
    public final void onResponse(final Response response) throws IOException {
        Log.d(TAG, "onResponse");
        if (isCancelled()){
            // Callback was cancelled, do nothing with the response
            return;
        }
        try {
            final Wrapper responseObject = NetworkUtils.handleResponse(response, mWrapperClass);
            getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    onNetworkSuccess(responseObject);
                }
            });
        } catch (final NetworkException ne) {
            getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "NetworkException: " + ne.getMessage(), ne);
                    onNetworkFailure(ne);
                    if (ne.getFailureType() == FailureType.TOKEN_EXPIRED){
                        onNetworkTokenExpired(ne);
                    }
                }
            });
        }
    }

    public void cancelCallback(){
        synchronized (this){
            isCancelled = true;
        }
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                onCancelCallback();
            }
        });
    }

    public synchronized boolean isCancelled(){
        return isCancelled;
    }

    public abstract void onNetworkFailure(NetworkException ne);

    public abstract void onNetworkSuccess(Wrapper wrapper);

    public void onCancelCallback(){
        //Do nothing, caller will override this if it needs to handle cancellation
    }

    public void onNetworkTokenExpired(NetworkException ne) {
        //TODO: Login Auth Token Expired, initiate Logout Flow.
        // Classes extending NetworkAsyncCallback might not necessarily implement this method

    }
}
