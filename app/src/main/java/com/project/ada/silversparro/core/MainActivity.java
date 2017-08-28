package com.project.ada.silversparro.core;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.project.ada.silversparro.AnnotationsEditor;
import com.project.ada.silversparro.Constants;
import com.project.ada.silversparro.R;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.data.BoundingRect;
import com.project.ada.silversparro.utils.NetworkAsyncCallback;
import com.project.ada.silversparro.utils.NetworkDataProvider;
import com.project.ada.silversparro.utils.NetworkException;
import com.project.ada.silversparro.utils.SilverImageLoader;
import com.project.ada.silversparro.utils.Utils;
import com.project.ada.silversparro.views.DrawableView;
import com.project.ada.silversparro.views.ResizableRectangleView;

import java.util.ArrayList;
import java.util.List;

import static com.project.ada.silversparro.core.MainApplication.getContext;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements
        View.OnClickListener, OnMatrixChangedListener, DrawableView.Listener,
        ResizableRectangleView.SaveListener, ClassSelectionAdapter.ItemClickListener {

    private static final String TAG = "SilverSparroActivity";

    /**
     * Middle layer in Drawing views hierarchy
     * It draws all the saved rectangles on canvas, their position changes when zoomPanView Matrix changes
     */
    private DrawableView drawbleView;
    /**
     * Bottom most layer in Drawing views hierarchy
     * It draws the background image and translates and zooms it when zoom is unlocked
     */
    private PhotoView zoomPanView;
    /**
     * Topmost layer in Drawing views hierarchy
     */
    private ResizableRectangleView rectangleView;

    private AnnotationsEditor annotationsEditor;

    private Button deleteButton;
    private Button unlockZoomButton;
    private Button saveAndNextButton;
    private Button datasetButton;
    private FrameLayout mainContainer;
    private ProgressBar mProgressBar;
    private Button retryButton;
    private RelativeLayout retryContainer;

    private DrawState drawState;

    private RectF latestScreenRect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TextUtils.isEmpty(Persistence.getDataSetName())){
            // Need to get DataSetName
            startDatasetActivity();
            return;
        }
        setContentView(R.layout.activity_main);

        drawbleView = (DrawableView) findViewById(R.id.drawble_view);
        zoomPanView = (PhotoView) findViewById(R.id.zoom_iv);
        rectangleView = (ResizableRectangleView) findViewById(R.id.rectangle_view);
        mainContainer = (FrameLayout) findViewById(R.id.main_container);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        retryContainer = (RelativeLayout) findViewById(R.id.container_retry);
        retryButton = (Button) findViewById(R.id.bt_retry);
        datasetButton = (Button) findViewById(R.id.bt_dataset);

        /*
        * gives the relative coordinates of the image with respect to the current view
        */
        zoomPanView.setOnMatrixChangeListener(this);
//        drawbleView.setListener(this);
        rectangleView.setListener(this);
        drawbleView.setDrawingEnabled(false);
        setDrawState(DrawState.ZOOM_PAN);


        deleteButton = (Button) findViewById(R.id.bt_delete);
        unlockZoomButton = (Button) findViewById(R.id.bt_unlock_zoom);
        saveAndNextButton = (Button) findViewById(R.id.bt_save_and_next);
        deleteButton.setOnClickListener(this);
        unlockZoomButton.setOnClickListener(this);
        saveAndNextButton.setOnClickListener(this);
        retryButton.setOnClickListener(this);
        datasetButton.setOnClickListener(this);

        load();

    }

    private void startDatasetActivity() {
        Intent intent = new Intent(this, DataSetActivity.class);
        startActivity(intent);
        finish();
    }

    private void load(){
        String imgUrlUnderProgress = Persistence.getImgUrlUnderProgress();
        if (TextUtils.isEmpty(imgUrlUnderProgress)){
            // Need to download annotation data
            showProgressDialog();
            String getUrl = Constants.BASE_URL + "/" + Persistence.getDataSetName();
            Log.d(TAG, "Will fetch AnnotationData at: " + getUrl);
            NetworkDataProvider.doGetCallAsync(getUrl, new NetworkAsyncCallback<Annotation>() {

                @Override
                public void onNetworkFailure(NetworkException ne) {
                    hideProgressDialog(false);
                    MainApplication.showToast(R.string.network_error);
                    Log.d(TAG, "Unable to fetch annotation: " + ne);
                    ne.printStackTrace();
                }

                @Override
                public void onNetworkSuccess(Annotation annotation) {
                    if (TextUtils.isEmpty(annotation.getImageUrl())){
                        // It is actually a Failure
                        MainApplication.showToast(R.string.cant_load);
                        hideProgressDialog(false);
                    }else {
                        Gson gson = new Gson();
                        Log.d(TAG, "Successfully fetched Annotation data " + gson.toJson(annotation));
                        annotationsEditor = new AnnotationsEditor(annotation);
                        Persistence.persistAnnotation(annotation);
                        Persistence.setImgUrlUnderProgress(annotation.getImageUrl());
                        hideProgressDialog(true);
                        SilverImageLoader.getInstance().loadImage(annotationsEditor.getImageUrl(), zoomPanView);
                        drawbleView.setListener(MainActivity.this);
                    }
                }
            });

        }else {
            Log.d(TAG, "load, fetching persisted annotation at URL: " + imgUrlUnderProgress);
            Annotation annotation = Persistence.getPersistedAnnotation(imgUrlUnderProgress);
            Gson gson = new Gson();
            Log.d(TAG, "Loading persisted Annotation: " + gson.toJson(annotation));
            annotationsEditor = new AnnotationsEditor(annotation);
            SilverImageLoader.getInstance().loadImage(annotationsEditor.getImageUrl(), zoomPanView);
            drawbleView.setListener(this);
        }

    }

    protected void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
        retryContainer.setVisibility(View.GONE);
        mainContainer.setVisibility(View.INVISIBLE);
    }

    protected void hideProgressDialog(boolean imageLoadSuccess) {
        mProgressBar.setVisibility(View.GONE);
        if (imageLoadSuccess){
            retryContainer.setVisibility(View.GONE);
            mainContainer.setVisibility(View.VISIBLE);
        }else {
            retryContainer.setVisibility(View.VISIBLE);
            mainContainer.setVisibility(View.INVISIBLE);
        }
    }


    public DrawState getDrawState() {
        return drawState;
    }

    public void setDrawState(DrawState drawState) {
        this.drawState = drawState;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "################# onClick #################");
        switch (id) {
            case R.id.bt_unlock_zoom:
                resolveZoomToggle();
                break;

            case R.id.bt_delete:
                resolveDelete();
                break;
            
            case R.id.bt_save_and_next:
                resolveSaveAndNext();
                break;

            case R.id.bt_retry:
                // Retry load
                load();
                break;

            case R.id.bt_dataset:
                // Open DataSetActivity
                startDatasetActivity();
                break;

            default:
                break;
        }
    }

    private void resolveDelete(){
        if (annotationsEditor != null){
            annotationsEditor.deleteActiveBox();
        }
        rectangleView.deactivate();
    }

    AlertDialog saveAndNextDialog;

    private void resolveSaveAndNext(){
        if (annotationsEditor != null && !isFinishing()){
            if (saveAndNextDialog == null || !saveAndNextDialog.isShowing()){
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.are_you_sure));
                alertDialog.setMessage(getString(R.string.save_and_next_message));
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Step: discard any unsaved boxes and enable zoom mode
                        unlockZoomAndPan();
                        // Uploads the Annotation for current image and remove it from persistent storage
                        annotationsEditor.uploadAndFinish();
                        // clear annotationsEditor
                        annotationsEditor = null;
                        drawbleView.setListener(null);
                        drawbleView.refresh();
                        // Download and display next image
                        load();
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                saveAndNextDialog = alertDialog.show();
            }
        }
    }

    private void resolveZoomToggle(){
        if (annotationsEditor == null){
            // Do nothing
            return;
        }
        if (unlockZoomButton.getText().equals(getString(R.string.lock))) {
            lockZoomAndStartPaint();
        } else {
            unlockZoomAndPan();
        }
    }

    /**
     * Unlocks zoom and pan feature by making top rectanleView layer and disabling touch on middle drawableView layer
     * If there are any unsaved rectangles it discards all of them
     */
    private void unlockZoomAndPan(){
        if (annotationsEditor == null){
            return;
        }
        annotationsEditor.deleteActiveBox();
        rectangleView.deactivate();
        drawbleView.setDrawingEnabled(false);
        if (classSelectionRecyclerView != null && classSelectionRecyclerView.getParent() == mainContainer){
            mainContainer.post(new Runnable() {
                @Override
                public void run() {
                    mainContainer.removeView(classSelectionRecyclerView);
                }
            });
        }
        unlockZoomButton.setText(getString(R.string.lock));
        setDrawState(DrawState.ZOOM_PAN);
    }


    /**
     * Locks Zoom and Pan and allows painting on the canvas to draw bounding boxes
     * It also draws the already existing saved rectangles from AnnotationEditor
     */
    private void lockZoomAndStartPaint(){
        if (annotationsEditor == null){
            return;
        }
        drawbleView.setDrawingEnabled(true);
        annotationsEditor.deleteActiveBox();
        rectangleView.deactivate();
        unlockZoomButton.setText(getString(R.string.unlock));
        setDrawState(DrawState.PAINT_STROKE);
    }


    private void saveActiveRectangle(String className){
        Log.d(TAG, "saveActiveRectangle");
        RectF activeRect = rectangleView.getCurrentRectF();
        Log.d(TAG, "Will save activeRectangle: "+ activeRect +" at scale: " + zoomPanView.getScale());
        BoundingRect boundingRect = createBoundingRect(convertToOriginalCoordinates(activeRect), className);
        annotationsEditor.saveBoundingRect(boundingRect);
        rectangleView.deactivate();
        drawbleView.refresh();
    }

    RecyclerView classSelectionRecyclerView;

    private void drawClassSelectionView(final RectF activeRectangle){
        Log.d(TAG, "drawClassSelectionView, activeRectangle = " + activeRectangle);

        classSelectionRecyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.class_selection_recycler_view, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        classSelectionRecyclerView.setLayoutManager(layoutManager);

        // Set Adapter
        ClassSelectionAdapter adapter = new ClassSelectionAdapter(annotationsEditor.getBoxClasses(), this);
        classSelectionRecyclerView.setAdapter(adapter);


        int numClasses = annotationsEditor.getBoxClasses().size();
        int mainContainerHeight = mainContainer.getHeight();
        int recyclerViewEstimatedHeight = (int) Utils.convertDpToPixel(getApplicationContext(), 32*numClasses);
        Log.d(TAG, "recyclerView estimated height = " + recyclerViewEstimatedHeight
                + ", mainContainerHeight = " + mainContainerHeight
                + ", activeRectangle.top = " + activeRectangle.top);

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int)activeRectangle.left;
        if (activeRectangle.bottom > mainContainerHeight - recyclerViewEstimatedHeight){
            // RecyclerView is going beyond the views bottom
            layoutParams.topMargin = (int)activeRectangle.top - recyclerViewEstimatedHeight;
        }else {
            layoutParams.topMargin = (int)activeRectangle.bottom;
        }
        mainContainer.addView(classSelectionRecyclerView, layoutParams);

    }

    private BoundingRect createBoundingRect(RectF rectF, String boxClass){
        BoundingRect boundingRect = new BoundingRect(annotationsEditor.getImageWidth(), annotationsEditor.getImageHeight());
        boundingRect.setBoxClass(boxClass);
        boundingRect.setRect(rectF);
        return boundingRect;
    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static boolean verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onMatrixChanged(RectF rect) {
        Log.d(TAG, "onMatrixChanged:" + rect);
        latestScreenRect = new RectF(rect.left,rect.top,rect.right,rect.bottom);

        Log.d(TAG, "onMatrixChanged, rectF = " + rect);
        if (drawbleView.isDrawingEnabled()){
            // Drawing was under progress but somehow PhotoView's image matrix got reset, lets discard activeBox, disableDrawing and unlockZoom
            unlockZoomAndPan();
        }
        // Update the Saved boxes in DrawableView
        //iteration over only those boxes which are saved.
        drawbleView.refresh();
    }


    @Override
    public void onPaintRectangle(RectF paitedRectangle) {
        Log.d(TAG, "onPaintRectangle: " + paitedRectangle);
        // Need to activate RectangleView with it
        rectangleView.activateWith(paitedRectangle);
        annotationsEditor.startWithFreshBox(paitedRectangle);
    }

    @Override
    public List<BoundingRect> fetchSavedRectanglesToDraw() {
        List<BoundingRect> scaledList = new ArrayList<>();
        if (latestScreenRect != null){
            for (BoundingRect origRect : annotationsEditor.getAllSavedRectanglesExceptActiveOne()){
                BoundingRect scaledRect = new BoundingRect(annotationsEditor.getImageWidth(),
                        annotationsEditor.getImageHeight());
                scaledRect.setBoxClass(origRect.getBoxClass());
                scaledRect.setRect(scaleAndTranslateOrigCoordinates(origRect.getRect()));
                scaledList.add(scaledRect);
            }
        }
        return scaledList;
    }

    @Override
    public float getScale() {
        return zoomPanView.getScale();
    }


    @Override
    public boolean highlightBoundingRectangle(Point point) {
        // transform Point's coordinates as per original image
        Log.d(TAG, "highlightBoundingRectangle, longPressed point = " + point);
        Point origPoint = convertToOriginalCoordinates(point);
        Log.d(TAG, "highlightBoundingRectangle, longPressed orig image point = " + origPoint);
        BoundingRect rect = annotationsEditor.checkAndStartEditAtPosition(origPoint.x, origPoint.y);
        if (rect != null){
            // There is a rectangle bounding the point
            rectangleView.activateWith(scaleAndTranslateOrigCoordinates(rect.getRect()));
            drawbleView.refresh();
            return true;
        }
        return false;
    }

    private RectF scaleAndTranslateOrigCoordinates(RectF rectF){
        float imageWidth = annotationsEditor.getImageWidth();
        float imageHeight = annotationsEditor.getImageHeight();
        float left = latestScreenRect.left + ((rectF.left * (latestScreenRect.right - latestScreenRect.left)) / imageWidth);
        float top = latestScreenRect.top + ((rectF.top * (latestScreenRect.bottom - latestScreenRect.top)) / imageHeight);
        float right = latestScreenRect.left + ((rectF.right * (latestScreenRect.right - latestScreenRect.left)) / imageWidth);
        float bottom = latestScreenRect.top + ((rectF.bottom * (latestScreenRect.bottom - latestScreenRect.top)) / imageHeight);
        return new RectF(left, top, right, bottom);
    }

    private Point convertToOriginalCoordinates(Point point){
        float imageWidth = annotationsEditor.getImageWidth();
        float imageHeight = annotationsEditor.getImageHeight();
        int origX = Math.round(imageWidth * ( (point.x - latestScreenRect.left) / (latestScreenRect.right - latestScreenRect.left) ));
        int origY = Math.round(imageHeight * ( (point.y - latestScreenRect.top) / (latestScreenRect.bottom - latestScreenRect.top) ));
        return new Point( origX, origY);
    }

    private RectF convertToOriginalCoordinates(RectF rectF){
        float imageWidth = annotationsEditor.getImageWidth();
        float imageHeight = annotationsEditor.getImageHeight();
        float origLeft = imageWidth * ( (rectF.left - latestScreenRect.left) / (latestScreenRect.right - latestScreenRect.left) );
        float origTop = imageHeight * ( (rectF.top - latestScreenRect.top) / (latestScreenRect.bottom - latestScreenRect.top) );
        float origRight = imageWidth * ( (rectF.right - latestScreenRect.left) / (latestScreenRect.right - latestScreenRect.left) );
        float origBottom = imageHeight * ( (rectF.bottom - latestScreenRect.top) / (latestScreenRect.bottom - latestScreenRect.top) );
        return new RectF(origLeft, origTop, origRight, origBottom);
    }

    /**
     * Callback from ResizableRectangleView, called when a long press is detected inside the active rectangle
     * @param rectF
     */
    @Override
    public void onSaveRect(RectF rectF) {
        drawClassSelectionView(rectF);
    }

    @Override
    public void onClassSelected(String className) {
        // Save the activeRectangle with className, then remove recyclerView from view hierarchy
        saveActiveRectangle(className);
        mainContainer.removeView(classSelectionRecyclerView);
        classSelectionRecyclerView = null;
    }

//[RectF(376.90726, 850.0, 721.0, 584.2018), RectF(269.56836, 1068.0, 565.0, 817.0), RectF(374.70007, 1310.0, 680.0, 1050.0)]

}