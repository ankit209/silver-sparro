package com.project.ada.silversparro;

import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.project.ada.silversparro.core.Persistence;
import com.project.ada.silversparro.core.SyncHelper;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.data.BoundingRect;
import com.project.ada.silversparro.data.Box;
import com.project.ada.silversparro.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitmaheshwari on 8/13/17.
 */

public class AnnotationsEditor {

    private static final String TAG = "AnnotationsEditor";

    private Annotation annotation;

    private int activeIndex;

    private static final int FRESH_BOX = -1;
    private static final int IDLE = -2;

    public AnnotationsEditor(Annotation annotation) {
        this.annotation = annotation;
        activeIndex = IDLE;
    }

    public float getImageWidth(){
        return annotation.getImageWidth();
    }

    public float getImageHeight(){
        return annotation.getImageHeight();
    }

    public boolean isEditing(){
        return activeIndex != IDLE;
    }

    public List<String> getBoxClasses(){
        return annotation.getBoxClasses();
    }

    public int getNumClasses(){
        return getBoxClasses().size();
    }

    public String getDefaultClassName(){
        if (getNumClasses() > 0){
            return getBoxClasses().get(0);
        }
        return "";
    }

    /**
     * Initiates editing for a new box, sets state accordingly
     * Sets state for editing fresh box,
     */
    public void startWithFreshBox(){
        if (isEditing()){
            throw new IllegalStateException("Editing already in progress for index " + activeIndex);
        }
        activeIndex = FRESH_BOX;
    }

    /**
     * If the input point is inside an already existing box in list, it will
     * change the state to Editing setting the activeIndex as the index of the box in the list
     * @param touchX
     * @param touchY
     * @return Saved Rectangle bounding the point which just got activated (highlighted), NULL if there is no rectangle bounding the point
     */
    public BoundingRect checkAndStartEditAtPosition(float touchX, float touchY){
        if (isEditing()){
            throw new IllegalStateException("Editing already in progress for index " + activeIndex);
        }
        int ind = getIndexOfBoxAt(touchX, touchY);
        if (ind >= 0){
            // A box is present at the specified position, can start editing it
            activeIndex = ind;
            return getSavedRectAtIndex(ind);
        }
        return null;
    }

    /**
     * Removes the box at current active index from list of boxes and then persist Annotation
     * Sets state to IDLE.
     */
    public void deleteActiveBox(){
        if (!isEditing()){
            Log.d(TAG, "Can't find active box to delete, will do nothing");
            return;
        }
        if (activeIndex > -1){
            // Some saved box is currently being edited, need to remove it
            synchronized (AnnotationsEditor.class){
                annotation.getBoxes().remove(activeIndex);
                Persistence.persistAnnotation(annotation);
            }
        }
        discardEditing();
    }

    /**
     * Change the state to IDLE, called when the current highlighted box is removed from UI
     */
    public void discardEditing(){
        activeIndex = IDLE;
    }

    /**
     * Saves bounding box at activeIndex if active Index is FRESH_BOX then it adds the new box to the list
     * Changes state to IDLE
     * Persists the newly edited Annotation object
     * @param boundingRect
     */
    public void saveBoundingRect(BoundingRect boundingRect){

        if (!isEditing()){
            throw new IllegalStateException("A box can only be saved in Editing state");
        }

        if (TextUtils.isEmpty(boundingRect.getBoxClass())){
            throw new IllegalStateException("Please enter Box Class");
        }

        Box annotaionBox;
        if (activeIndex == FRESH_BOX){
            annotaionBox = new Box();
            annotaionBox.setBoxClass(boundingRect.getBoxClass());
            annotaionBox.setPoints(generatePointsArray(boundingRect.getRect()));
            annotation.getBoxes().add(annotaionBox);
        }else {
            annotaionBox = annotation.getBoxes().get(activeIndex);
            annotaionBox.setBoxClass(boundingRect.getBoxClass());
            annotaionBox.setPoints(generatePointsArray(boundingRect.getRect()));
        }
        discardEditing();
        Persistence.persistAnnotation(annotation);
    }

    /**
     * Triggers background task to upload current Annotation it to server and then remove it from persistence
     */
    public void uploadAndFinish(){
        Log.d(TAG, "uploadAndFinish, will upload Annotation");
        SyncHelper.uploadAnnotation(annotation);
        Persistence.removePersistedAnnotation(annotation.getImageUrl());
        Persistence.setImgUrlUnderProgress(null);
        annotation = null;
        activeIndex = IDLE;
    }

    public int getIndexOfBoxAt(final float touchX, final float touchY){
        synchronized (AnnotationsEditor.class){
            for (int i=0; i<annotation.getBoxes().size(); i++){
                if (isTouchInsideBox(touchX, touchY, annotation.getBoxes().get(i))){
                    return i;
                }
            }
            return IDLE;
        }
    }

    public BoundingRect getSavedRectAtIndex(int index){
        Box annoBox = annotation.getBoxes().get(index);
        return convertBoxToRect(annoBox);
    }

    /**
     * Gets the list of all bounding rectangles which are saved for this annotation
     * @return
     */
    public List<BoundingRect> getAllSavedRectangles(){
        List<BoundingRect> list = new ArrayList<>();
        for (Box annoBox : annotation.getBoxes()){
            list.add(convertBoxToRect(annoBox));
        }
        return list;
    }

    /**
     * Gets the list of all bounding rectangles which are saved for this annotation
     * @return
     */
    public List<BoundingRect> getAllSavedRectanglesExceptActiveOne(){
        List<BoundingRect> list = new ArrayList<>();
        for (int i=0; i < annotation.getBoxes().size(); i++){
            if (i != activeIndex){
                list.add(convertBoxToRect(annotation.getBoxes().get(i)));
            }
        }
        return list;
    }

    public String getImageUrl(){
        return annotation.getImageUrl();
    }

    private BoundingRect convertBoxToRect(Box annoBox){
        BoundingRect boundingRect = new BoundingRect(getImageWidth(), getImageHeight());
        boundingRect.setBoxClass(annoBox.getBoxClass());
        boundingRect.setRect(generateRectFromPointsArray(annoBox.getPoints()));
        return boundingRect;
    }

    private static boolean isTouchInsideBox(float touchX, float touchY, Box box){
        RectF rect = generateRectFromPointsArray(box.getPoints());
        return rect.contains(touchX, touchY);
    }

    public static RectF generateRectFromPointsArray(List<Point> points){
        if (points == null || points.isEmpty() || points.size() != 4){
            throw new IllegalStateException("InputList should be of size 4");
        }
        return Utils.convertPointsListToRectF(points);
    }

    /**
         topLeft = left, top
         topRight = right, top
         bottomLeft = left, bottom
         bottomRight = right, bottom
     * @param rectF
     * @return
     */
    public static List<Point> generatePointsArray(RectF rectF){
        List<Point> points = new ArrayList<>();
        points.add(new Point(Math.round(rectF.left), Math.round(rectF.top)));
        points.add(new Point(Math.round(rectF.right), Math.round(rectF.top)));
        points.add(new Point(Math.round(rectF.left), Math.round(rectF.bottom)));
        points.add(new Point(Math.round(rectF.right), Math.round(rectF.bottom)));
        return points;
    }
}
