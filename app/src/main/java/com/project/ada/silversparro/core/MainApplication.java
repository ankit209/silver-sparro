package com.project.ada.silversparro.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.project.ada.silversparro.utils.SharedPrefsManager;

import static com.project.ada.silversparro.Constants.PREF_APP_VERSION;

/**
 * Created by ankitmaheshwari on 8/15/17.
 */

public class MainApplication extends Application{

    private static final String TAG = "MainApplication";

    private static MainApplication instance;

    //generally for singleton class constructor is made private but since this class is registered
    //in manifest and extends Application constructor is public so OS can instantiate it
    //Note: Developers should not call constructor. Should use getInstance method instead
    public MainApplication() {
        instance = this;
    }

    public static MainApplication getInstance() {
        if (instance == null) {
            Log.e(TAG, "Main application instance should never be null");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefsManager.initialize(this);
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    /**
     * @return a {@link Handler} tied to the main thread.
     */
    public static Handler getMainThreadHandler() {
        return new Handler(Looper.getMainLooper());
    }

    private Toast appToast;

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     */
    public static void showToast(final String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread. uses resource id of the
     * message string
     */
    public static void showToast(int stringId) {
        showToast(getContext().getResources().getString(stringId));
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     */
    public static void showToast(final String message, final int duration) {
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(message) == false) {
                    if (getInstance().appToast == null){
                        getInstance().appToast = Toast.makeText(getContext(), message, duration);
                    }else {
                        getInstance().appToast.setDuration(duration);
                        getInstance().appToast.setText(message);
                    }
                    getInstance().appToast.show();
                }
            }
        });
    }

    public void updateAppVersionInPrefs(){
        try {
            String currentVersion = "";
            PackageInfo pInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            currentVersion = pInfo.versionName;

            String storedVersion = SharedPrefsManager.getInstance().getString(PREF_APP_VERSION);

            SharedPrefsManager.getInstance().setString(PREF_APP_VERSION, currentVersion);

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

}
