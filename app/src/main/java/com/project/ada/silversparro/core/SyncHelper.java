package com.project.ada.silversparro.core;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;
import com.project.ada.silversparro.data.Annotation;

import static com.project.ada.silversparro.Constants.ANNOTATION_DATA_JSON;
import static com.project.ada.silversparro.Constants.TASK_UPLOAD_ANNOTATION;

/**
 * Created by ankitmaheshwari on 8/28/17.
 */

public class SyncHelper {

    private static final String TAG = "SyncHelper";

    public static void uploadAnnotation(Annotation annotation) {
        Gson gson = new Gson();
        String annotationJson = gson.toJson(annotation);
        Log.d(TAG, "uploadAnnotation: " + annotationJson);
        Bundle bundle = new Bundle();
        bundle.putString(ANNOTATION_DATA_JSON, annotationJson);
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TASK_UPLOAD_ANNOTATION)
                .setExtras(bundle)
                /*
                    Mandatory setter for creating a one-off task.
                    You specify the earliest point in time in the future from which your task might start executing,
                    as well as the latest point in time in the future at which your task must have executed.
                 */
                .setExecutionWindow(0L, 10L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext());
        mGcmNetworkManager.schedule(task);
    }

}
