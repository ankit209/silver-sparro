package com.project.ada.silversparro;

import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextUtils;

import com.project.ada.silversparro.core.SharedPrefsManager;
import com.project.ada.silversparro.core.Utils;
import com.project.ada.silversparro.data.Annotation;
import com.project.ada.silversparro.data.BoundingBox;
import com.project.ada.silversparro.data.Box;

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

    /**
     * Initiates editing for a new box, sets state accordingly
     * Sets state for editing fresh box,
     * @param inputRect
     * @return fresh BoundingBox to be edited
     */
    public BoundingBox startWithFreshBox(RectF inputRect){

        if (isEditing()){
            throw new IllegalStateException("Editing already in progress for index " + activeIndex);
        }
        activeIndex = FRESH_BOX;
        BoundingBox freshBox = new BoundingBox(getImageWidth(), getImageHeight());
        freshBox.setBoxRect(inputRect);
        return freshBox;

    }

    /**
     * If the input point is inside an already existing box in list, it will
     * change the state to Editing setting the activeIndex as the index of the box in the list
     * @param touchX
     * @param touchY
     * @return
     */
    public BoundingBox checkAndStartEditAtPosition(float touchX, float touchY){
        if (isEditing()){
            throw new IllegalStateException("Editing already in progress for index " + activeIndex);
        }
        int ind = getIndexOfBoxAt(touchX, touchY);
        if (ind >= 0){
            // A box is present at the specified position, can start editing it
            activeIndex = ind;
            return getBoundingBoxAtIndex(ind);
        }
        return null;
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
     * @param boundingBox
     */
    public void saveBoundingBox(BoundingBox boundingBox){

        if (TextUtils.isEmpty(boundingBox.getBoxClass())){
            throw new IllegalStateException("Please enter Box Class");
        }

        Box annotaionBox;
        if (activeIndex == FRESH_BOX){
            annotaionBox = new Box();
            annotaionBox.setBoxClass(boundingBox.getBoxClass());
            annotaionBox.setPoints(generatePointsArray(boundingBox.getBoxRect()));
            annotation.getBoxes().add(annotaionBox);
        }else {
            annotaionBox = annotation.getBoxes().get(activeIndex);
            annotaionBox.setBoxClass(boundingBox.getBoxClass());
            annotaionBox.setPoints(generatePointsArray(boundingBox.getBoxRect()));
        }
        activeIndex = IDLE;
        String key = Constants.PREFS_ANNOTATION_PREFIX + annotation.getImageUrl();
        SharedPrefsManager.getInstance().setString(key, Utils.createJSONStringFromObject(annotation));

    }

    public int getIndexOfBoxAt(final float touchX, final float touchY){

        for (int i=0; i<annotation.getBoxes().size(); i++){
            if (isTouchInsideBox(touchX, touchY, annotation.getBoxes().get(i))){
                return i;
            }
        }
        return IDLE;

    }

    public BoundingBox getBoundingBoxAtIndex(int index){
        Box annoBox = annotation.getBoxes().get(index);
        BoundingBox boundingBox = new BoundingBox(getImageWidth(), getImageHeight());
        boundingBox.setBoxClass(annoBox.getBoxClass());
        boundingBox.setBoxRect(generateRectFromPointsArray(annoBox.getPoints()));
        return boundingBox;
    }

    private static boolean isTouchInsideBox(float touchX, float touchY, Box box){
        RectF rect = generateRectFromPointsArray(box.getPoints());
        return rect.contains(touchX, touchY);
    }

    public static RectF generateRectFromPointsArray(List<Point> points){
        if (points == null || points.isEmpty() || points.size() != 4){
            throw new IllegalStateException("InputList should be of size 4");
        }
        return new RectF(points.get(0).x, points.get(0).y, points.get(3).x,
                points.get(3).y);
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
