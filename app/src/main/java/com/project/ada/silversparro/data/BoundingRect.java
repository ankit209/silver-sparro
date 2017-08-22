package com.project.ada.silversparro.data;

import android.graphics.RectF;


/**
 * Created by ankitmaheshwari on 8/13/17.
 */

public class BoundingRect {

    private float imageWidth;

    private float imageHeight;

    private RectF rect;

    private String boxClass;

    public BoundingRect(float imageWidth, float imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public float getImageHeight() {
        return imageHeight;
    }

    public RectF getRect() {
        return rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public String getBoxClass() {
        return boxClass;
    }

    public void setBoxClass(String boxClass) {
        this.boxClass = boxClass;
    }
}
