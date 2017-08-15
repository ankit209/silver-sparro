package com.project.ada.silversparro.data;

import android.graphics.RectF;


/**
 * Created by ankitmaheshwari on 8/13/17.
 */

public class BoundingBox {

    private float imageWidth;

    private float imageHeight;

    private RectF boxRect;

    private String boxClass;

    public BoundingBox(float imageWidth, float imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public float getImageHeight() {
        return imageHeight;
    }

    public RectF getBoxRect() {
        return boxRect;
    }

    public void setBoxRect(RectF boxRect) {
        this.boxRect = boxRect;
    }

    public String getBoxClass() {
        return boxClass;
    }

    public void setBoxClass(String boxClass) {
        this.boxClass = boxClass;
    }
}
