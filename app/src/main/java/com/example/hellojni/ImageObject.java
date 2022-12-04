package com.example.hellojni;



public class ImageObject {

    private int object;
    private float score;
    private float left_x;
    private float top_y;
    private float width;
    private float height;
    private float mid_x;
    private float mid_y;

    ImageObject(){
        this.object = -1;
        this.score = -1;
        this.left_x = -1;
        this.top_y = -1;
        this.width = -1;
        this.height = -1;
        this.mid_x = -1;
        this.mid_y = -1;
    }

    ImageObject(int object, float score){
        this.object = object;
        this.score = score;
    }

    public int getObject() {
        return object;
    }

    public float getScore() {
        return score;
    }

    public float getTop_y() {
        return top_y;
    }

    public float getLeft_x() {
        return left_x;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMid_x() {
        return mid_x;
    }

    public float getMid_y() {
        return mid_y;
    }

    public void setObject(int object) {
        this.object = object;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setLeft_x(float left_x) {
        this.left_x = left_x;
    }

    public void setTop_y(float top_y) {
        this.top_y = top_y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void calMidXY(){
        if (left_x == -1 || top_y == -1) return;
        this.mid_x = this.left_x + this.width/2;
        this.mid_y = this.top_y + this.height/2;
    }
}
