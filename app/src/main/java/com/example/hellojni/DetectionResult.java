package com.example.hellojni;

public class DetectionResult {
    private int size;
    private int category;
    private ImageObject[] imageObjectList;

    public DetectionResult(ImageObject[] imageObjectList){
        this.size = -1;
        this.category = -1;
        this.imageObjectList = imageObjectList;
    }

    public DetectionResult(int size, int category, ImageObject[] imageObjectList) {
        this.size = size;
        this.category = category;
        this.imageObjectList = imageObjectList;
    }

    public int getSize() {
        return size;
    }

    public int getCategory() {
        return category;
    }

    public ImageObject[] getImageObjectList() {
        return imageObjectList;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setImageObjectList(ImageObject[] imageObjectList) {
        this.imageObjectList = imageObjectList;
    }
}
