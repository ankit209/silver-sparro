package com.project.ada.silversparro.data;

import android.graphics.Point;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/13/17.
 */

public class Box {


    @SerializedName("box_class")
    private String boxClass;

    @SerializedName("points")
    private List<Point> points;


    public String getBoxClass() {
        return boxClass;
    }

    public void setBoxClass(String boxClass) {
        this.boxClass = boxClass;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
