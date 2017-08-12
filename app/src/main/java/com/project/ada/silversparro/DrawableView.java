package com.project.ada.silversparro;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ada on 23/6/17.
 */

public class DrawableView extends View {

    public int width;
    public  int height;
    private boolean isEditable;
    private Path drawPath;
    private Paint canvasPaint;
    private Paint drawPaint;
    Paint rectPaint = new Paint();
    Paint finalPaint = new Paint();
    private int paintColor = R.color.colorPrimaryDark;
    private int rectPaintColor = R.color.colorAccent;
    private int finalPaintColor = R.color.black;

    public ArrayList<Path> paths = new ArrayList<>();
    public ArrayList<Float> xCoordinates = new ArrayList<>();
    public ArrayList<Float> yCoordinates = new ArrayList<>();
    double xMin = Integer.MAX_VALUE;
    double yMin = Integer.MAX_VALUE;
    double xMax = Integer.MIN_VALUE;
    double yMax = Integer.MIN_VALUE;
    RectF rect = new RectF();

    public float[] rectangle = new float[4];

    public ArrayList<RectF> allRectF = new ArrayList<>();
    public ArrayList<RectF> savedRectF = new ArrayList<>();

    //changingRectF stores the coordinates of the boxes on changing scale (for rendering)
    public ArrayList<RectF> changingRectF = new ArrayList<>();
    //savedScales stores the scale at the same index as that of its corresponding saved box.
    public ArrayList<Float> savedScales = new ArrayList<>();

    //savedScreenRectF stores the coordinates of the screen for all the boxes.
//    public ArrayList<RectF> screenRectF = new ArrayList<>();
//    //savedScreenRectF stores the coordinates of the screen at which a box was saved.
//    public ArrayList<RectF> savedScreenRectF = new ArrayList<>();


    public DrawableView(Context context) {
        super(context);
    }
    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.canvasPaint = new Paint(Paint.DITHER_FLAG);
        setupDrawing(context);
    }
    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height = h;
        this.width = w;
        Resources res = getResources();
    }


    private void setupDrawing(Context context) {
        drawPath = new Path();
        drawPaint = new Paint();


        //cursor attributes
        drawPaint.setColor(ContextCompat.getColor(context,paintColor));
        drawPaint.setAlpha(80);
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
//        drawPaint.setStrokeJoin(Paint.Join.ROUND);
//        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(80);

        rectPaint.setColor(ContextCompat.getColor(context,rectPaintColor));
        rectPaint.setAlpha(150);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);

        finalPaint.setColor(ContextCompat.getColor(context, finalPaintColor));
        finalPaint.setAlpha(150);
        finalPaint.setStyle(Paint.Style.STROKE);
        finalPaint.setStrokeWidth(5);
    }

    public void setDrawingEnabled(boolean isEditable){
        this.isEditable = isEditable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Path p : paths) {                                  //for undo functionality
            canvas.drawPath(p, drawPaint);

        }
        if(rect!=null) {
            canvas.drawRect(rect, rectPaint);
                invalidate();
        }
        for (int i=0;i<changingRectF.size();i++){
            canvas.drawRect(changingRectF.get(i), finalPaint);
            invalidate(); 
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {            //add to paths
        if(isEditable){
            float touchX = event.getX();
            xCoordinates.add(touchX);
            float touchY = event.getY();
            yCoordinates.add(touchY);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("DrawableView", "ACTION_DOWN");
                    //clear x ,y coordinates ,rectangle etc of previous instance
                    xCoordinates.clear();
                    yCoordinates.clear();
                    rectangle = new float[4];
                    xMax = yMax = Integer.MIN_VALUE;
                    xMin = yMin = Integer.MAX_VALUE;

                    drawPath = new Path();
                    drawPath.moveTo(touchX, touchY);
                    paths.add(drawPath);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:

                case MotionEvent.ACTION_MOVE:
//                    Log.d("DrawableView", "ACTION_MOVE");
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("DrawableView", "ACTION_UP");
                    drawPath.lineTo(touchX, touchY);
                    rectangle[0] = Collections.min(xCoordinates) - drawPaint.getStrokeWidth()/4;
                    rectangle[1] = Collections.min(yCoordinates) - drawPaint.getStrokeWidth()/2;
                    rectangle[2] = Collections.max(xCoordinates)+ drawPaint.getStrokeWidth()/4;
                    rectangle[3] = Collections.max(yCoordinates)+ drawPaint.getStrokeWidth()/2;
                    rect = new RectF(rectangle[0],rectangle[1],rectangle[2],rectangle[3]);
                    allRectF.add(rect);
                    System.out.println("------------rect-------" +rect);
                    System.out.println("------------allRectF-------"+ allRectF);
                    System.out.println("------------savedRectF-------"+ savedRectF);
//                    System.out.println(paths);
                    break;
                default:
                    return false;
            }
        } else{
            return false;
        }
        invalidate();
        return true;
    }


    public void onClickUndo() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
            System.out.println("paths **************** " + paths);
            allRectF.remove(allRectF.size()-1);
            invalidate();
        } else {
            Toast.makeText(getContext(), "Invalid Operation", Toast.LENGTH_SHORT).show();
            //TODO: toast the user
        }

    }


    public void onClickBound(){
        Canvas canvas = new Canvas();
//        Paint paint = new Paint();
//        canvas.drawRect(rect,paint);
//
        System.out.println(rect);
    }
    

    public void onClickSave(float scale){
        savedScales.add(scale);
        changingRectF.add(rect);
        savedRectF.add(rect);
        paths.remove(paths.size()-1);

//        System.out.println("________________savedRectF_______________ " + savedRectF);
//        System.out.println("________________changingRectF____________ " + changingRectF);
//        System.out.println("________________savedRectF_______________ "+ savedScales);
    }


    public void onClickReset(){
//        originalRectF.removeAll(originalRectF);
        changingRectF.removeAll(changingRectF);
//        screenRectF.removeAll(screenRectF);
//        savedScreenRectF.removeAll(savedScreenRectF);
        savedRectF.removeAll(savedRectF);
        savedScales.removeAll(savedScales);
        paths.removeAll(paths);
        rect.setEmpty();
        System.out.println("__________savedRectF_________ " + savedRectF);
        System.out.println("------------savedRectF------- "+ savedScales);
//        System.out.println("------------screenRectF------- "+ screenRectF);
//        System.out.println("paths__________" + paths);
    }

}