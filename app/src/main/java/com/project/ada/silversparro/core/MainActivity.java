package com.project.ada.silversparro.core;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.project.ada.silversparro.AnnotationsEditor;
import com.project.ada.silversparro.R;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.data.BoundingRect;
import com.project.ada.silversparro.utils.SharedPrefsManager;
import com.project.ada.silversparro.utils.SilverImageLoader;
import com.project.ada.silversparro.utils.Utils;
import com.project.ada.silversparro.views.DrawableView;
import com.project.ada.silversparro.views.ResizableRectangleView;

import java.util.ArrayList;
import java.util.List;

import static com.project.ada.silversparro.Constants.PREFS_IMAGE_URL_UNDER_PROGRESS;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements View.OnClickListener, OnMatrixChangedListener, DrawableView.Listener,
        ResizableRectangleView.SaveListener {

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

    private View mainContainer;

    private AnnotationsEditor annotationsEditor;

    private Button deleteButton;
    private Button unlockZoomButton;
    private Button saveAndNextButton;

    private DrawState drawState;

    private RectF latestScreenRect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO:if the corresponding text file exists, display the saved boxes

        drawbleView = (DrawableView) findViewById(R.id.drawble_view);
        zoomPanView = (PhotoView) findViewById(R.id.zoom_iv);
        rectangleView = (ResizableRectangleView) findViewById(R.id.rectangle_view);
        mainContainer = (View) findViewById(R.id.main_container);

        /*
        * gives the relative coordinates of the image with respect to the current view
        */
        zoomPanView.setOnMatrixChangeListener(this);
        drawbleView.setListener(this);
        rectangleView.setListener(this);
        drawbleView.setDrawingEnabled(false);
        setDrawState(DrawState.ZOOM_PAN);


        deleteButton = (Button) findViewById(R.id.bt_delete);
        unlockZoomButton = (Button) findViewById(R.id.bt_unlock_zoom);
        saveAndNextButton = (Button) findViewById(R.id.bt_save_and_next);
        deleteButton.setOnClickListener(this);
        unlockZoomButton.setOnClickListener(this);
        saveAndNextButton.setOnClickListener(this);

        load();

    }

    private void load(){
        String imgUrlUnderProgress = SharedPrefsManager.getInstance().getString(PREFS_IMAGE_URL_UNDER_PROGRESS);
        if (TextUtils.isEmpty(imgUrlUnderProgress)){
            // TODO: Need to download next image, this is just a temporary hack
            // Need to download annotation data
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.image2);
            Annotation annotation = Utils.createDummyAnnotation(Utils.drawableToBitmap(drawable));
            annotationsEditor = new AnnotationsEditor(annotation);
            //Setting this image as the current image for which annotations are getting created
            SharedPrefsManager.getInstance().setString(PREFS_IMAGE_URL_UNDER_PROGRESS,
                    annotation.getImageUrl());
            Utils.persistAnnotation(annotation);

        }else {
            Log.d(TAG, "load, fetching persisted annotation at URL: " + imgUrlUnderProgress);
            Annotation annotation = Utils.getPersistedAnnotation(imgUrlUnderProgress);
            annotationsEditor = new AnnotationsEditor(annotation);
        }
        SilverImageLoader.getInstance().loadImage(annotationsEditor.getImageUrl(), zoomPanView);
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
                Log.d(TAG, "sceenwidth = " + Utils.getScreenWidthUsingDisplayMetrics(this)
                        + ", screenHeight = " + Utils.getScreenHeightUsingDisplayMetrics(this));
                Log.d(TAG, "onClick bt_unlock_zoom, Image width = " + annotationsEditor.getImageWidth()
                        + ", height = " + annotationsEditor.getImageHeight());
                Log.d(TAG, "onClick bt_unlock_zoom, view width = " + mainContainer.getWidth()
                        + ", height = " + mainContainer.getHeight());
                Log.d(TAG, "onClick bt_unlock_zoom, view X = " + mainContainer.getX()
                        + ", Y = " + mainContainer.getY());
                Log.d(TAG, "onClick bt_unlock_zoom, lockButton X = " + unlockZoomButton.getX()
                        + ", Y = " + unlockZoomButton.getY());
                Log.d(TAG, "onClick bt_unlock_zoom, deleteButton X = " + deleteButton.getX()
                        + ", Y = " + deleteButton.getY());
                if (unlockZoomButton.getText().equals(getString(R.string.lock))) {
                    lockZoomAndStartPaint();
                } else {
                    unlockZoomAndPan();
                }
                break;

            case R.id.bt_delete:
                annotationsEditor.deleteActiveBox();
                rectangleView.deactivate();
                break;
            
            case R.id.bt_save_and_next:
                // Save all the completed rectangles in Annotations Editor and move to the next rectangle

                break;

            default:
                break;
        }
    }

    /**
     * Unlocks zoom and pan feature by making top rectanleView layer and disabling touch on middle drawableView layer
     * If there are any unsaved rectangles it discards all of them
     */
    private void unlockZoomAndPan(){
        annotationsEditor.deleteActiveBox();
        rectangleView.deactivate();
        drawbleView.setDrawingEnabled(false);
        unlockZoomButton.setText(getString(R.string.lock));
        setDrawState(DrawState.ZOOM_PAN);
    }


    /**
     * Locks Zoom and Pan and allows painting on the canvas to draw bounding boxes
     * It also draws the already existing saved rectangles from AnnotationEditor
     */
    private void lockZoomAndStartPaint(){
        drawbleView.setDrawingEnabled(true);
        annotationsEditor.deleteActiveBox();
        rectangleView.deactivate();
        unlockZoomButton.setText(getString(R.string.unlock));
        setDrawState(DrawState.PAINT_STROKE);
    }

    private void saveActiveRectangle(){
        Log.d(TAG, "saveActiveRectangle");
        RectF activeRect = rectangleView.getCurrentRectF();
        Log.d(TAG, "Will save activeRectangle: "+ activeRect +" at scale: " + zoomPanView.getScale());
        BoundingRect boundingRect = createBoundingRect(convertToOriginalCoordinates(activeRect), "dummy_1");
        annotationsEditor.saveBoundingRect(boundingRect);
        rectangleView.deactivate();
        drawbleView.refresh();
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
       saveActiveRectangle();
    }

//[RectF(376.90726, 850.0, 721.0, 584.2018), RectF(269.56836, 1068.0, 565.0, 817.0), RectF(374.70007, 1310.0, 680.0, 1050.0)]

}