package com.project.ada.silversparro.core;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.data.ServerResponse;
import com.project.ada.silversparro.utils.NetworkDataProvider;
import com.project.ada.silversparro.utils.NetworkException;

import static com.project.ada.silversparro.Constants.ANNOTATION_DATA_JSON;
import static com.project.ada.silversparro.Constants.BASE_URL;
import static com.project.ada.silversparro.Constants.STATUS_SUCCESS;
import static com.project.ada.silversparro.Constants.TASK_UPLOAD_ANNOTATION;

/**
 * Created by ankitmaheshwari on 8/28/17.
 */

public class SyncService extends GcmTaskService {

    private static final String TAG = "SyncService";

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d(TAG, "runtask started: " + taskParams.getTag());
        if (TASK_UPLOAD_ANNOTATION.equals(taskParams.getTag())){
            Bundle extras = taskParams.getExtras();
            String annotationJson = extras.getString(ANNOTATION_DATA_JSON);
            return uploadAnnotationData(annotationJson);
        }
        return GcmNetworkManager.RESULT_FAILURE;
    }

    private int uploadAnnotationData(String annotationJson){
        if (TextUtils.isEmpty(annotationJson)){
            Log.d(TAG, "Can't push annotationJson in TaskParams is empty");
            return GcmNetworkManager.RESULT_FAILURE;
        }
        try {
            Log.d(TAG, "Will : uploadAnnotationData: " + annotationJson);
            Gson gson = new Gson();
            Annotation annotation = gson.fromJson(annotationJson, Annotation.class);
            String postUrl = BASE_URL + "/" + annotation.getDataSet();
            ServerResponse response = NetworkDataProvider.doPostCall(postUrl, annotationJson, ServerResponse.class);
            if (STATUS_SUCCESS.equals(response.getStatus())){
                Log.d(TAG, "Successfully uploaded annotation data");
                return GcmNetworkManager.RESULT_SUCCESS;
            } else {
                String log= "Couldn't post AnnotationData on URL "+ postUrl +", annotationData:"
                        + annotationJson;
                Log.e(TAG, log);
                Log.e(TAG, "Error response while uploading Annotationdata: " + response.getError());
                return GcmNetworkManager.RESULT_RESCHEDULE;
            }
        }catch (NetworkException ne){
            String log= "Couldn't post AnnotationData: " + annotationJson;
            Log.e(TAG, log);
            Log.e(TAG, "NetworkException while uploading Annotationdata: " + ne);
            ne.printStackTrace();
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }catch (Exception ex){
            String log= "Couldn't post AnnotationData: " + annotationJson;
            Log.e(TAG, log);
            Log.e(TAG, "Exception while uploading AnnotationData: " + ex.getMessage());
            ex.printStackTrace();
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    @Override
    public void onInitializeTasks() {
        Log.d(TAG, "onInitializeTasks");
        super.onInitializeTasks();
        // TODO: Start pending synctasks again
    }

}
