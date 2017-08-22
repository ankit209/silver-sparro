package com.project.ada.silversparro.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitmaheshwari on 8/13/17.
 */

public class Annotation {

    private String dataSet;

    @SerializedName("classes")
    private List<String> boxClasses;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("image_width")
    private int imageWidth;

    @SerializedName("image_height")
    private int imageHeight;

    @SerializedName("boxes")
    private List<Box> boxes = new ArrayList<>();

    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    public List<String> getBoxClasses() {
        return boxClasses;
    }

    public void setBoxClasses(List<String> boxClasses) {
        this.boxClasses = boxClasses;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }
}
