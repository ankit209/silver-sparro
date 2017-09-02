package com.project.ada.silversparro.core;

import android.util.Log;

import com.project.ada.silversparro.Constants;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.utils.SharedPrefsManager;
import com.project.ada.silversparro.utils.Utils;

import static com.project.ada.silversparro.Constants.PREFS_DATASET_NAME;
import static com.project.ada.silversparro.Constants.PREFS_DISABLE_RESIZE_BOX;
import static com.project.ada.silversparro.Constants.PREFS_IMAGE_URL_UNDER_PROGRESS;

/**
 * Created by ankitmaheshwari on 8/28/17.
 */

public class Persistence {

    private static final String TAG = "Persistence";

    public static void persistAnnotation(Annotation annotation){
        String key = Constants.PREFS_ANNOTATION_PREFIX + annotation.getImageUrl();
        Log.d(TAG, "Persisting Annotation at key = " + key
                + ", annotation = " + Utils.createJSONStringFromObject(annotation));
        SharedPrefsManager.getInstance().setString(key, Utils.createJSONStringFromObject(annotation));
    }

    public static Annotation getPersistedAnnotation(String imgUrl){
        String key = Constants.PREFS_ANNOTATION_PREFIX + imgUrl;
        return SharedPrefsManager.getInstance().getObject(key, Annotation.class);
    }

    public static void removePersistedAnnotation(String imgUrl){
        String key = Constants.PREFS_ANNOTATION_PREFIX + imgUrl;
        SharedPrefsManager.getInstance().removeKey(key);
    }

    public static String getImgUrlUnderProgress(){
        return SharedPrefsManager.getInstance().getString(PREFS_IMAGE_URL_UNDER_PROGRESS);
    }

    public static void setImgUrlUnderProgress(String imgUrl){
        SharedPrefsManager.getInstance().setString(PREFS_IMAGE_URL_UNDER_PROGRESS, imgUrl);
    }

    public static String getDataSetName(){
        return SharedPrefsManager.getInstance().getString(PREFS_DATASET_NAME);
    }

    public static void setDataSetName(String dataSetName){
        SharedPrefsManager.getInstance().setString(PREFS_DATASET_NAME, dataSetName);
    }

    public static void setDisableResizeBox(boolean disableResizeBox){
        SharedPrefsManager.getInstance().setBoolean(PREFS_DISABLE_RESIZE_BOX, disableResizeBox);
    }

    public static boolean isResizeBoxDisabled(){
        return SharedPrefsManager.getInstance().getBoolean(PREFS_DISABLE_RESIZE_BOX, false);
    }


}
