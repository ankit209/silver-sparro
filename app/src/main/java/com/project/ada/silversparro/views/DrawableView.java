package com.project.ada.silversparro.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.project.ada.silversparro.R;
import com.project.ada.silversparro.data.BoundingRect;
import com.project.ada.silversparro.utils.Utils;

import java.util.List;

import static com.project.ada.silversparro.Constants.PAINT_STROKE_WIDTH;
import static com.project.ada.silversparro.Constants.RECT_LINE_STROKE_WIDTH;
import static com.project.ada.silversparro.Constants.TEXT_SIZE_IN_DP;

/**
 * Created by ada on 23/6/17.
 */

public class DrawableView extends View {

    private static final String TAG = "DrawableView";

    public int width;
    public  int height;
    private boolean isEditable;
    private Path drawPath;
    private Paint drawPaint;
    private Paint rectPaint;
    private Paint textPaint;
    private int paintColor = R.color.colorPrimaryDark;
    float left = Float.MAX_VALUE;
    float top = Float.MAX_VALUE;
    float right = 0;
    float bottom = 0;

    float origTextSizeInPx = Utils.convertDpToPixel(getContext(), TEXT_SIZE_IN_DP);
    float origSavedRectStrokeWidthInPx = Utils.convertDpToPixel(getContext(), RECT_LINE_STROKE_WIDTH);

    Listener listener;


    public DrawableView(Context context) {
        super(context);
        setupDrawing(context);
    }
    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing(context);
    }
    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupDrawing(context);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height = h;
        this.width = w;
    }


    private void setupDrawing(Context context) {

        drawPaint = new Paint();

        //cursor attributes
        drawPaint.setColor(ContextCompat.getColor(context,paintColor));
        drawPaint.setAlpha(80);
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(Utils.convertDpToPixel(getContext(), PAINT_STROKE_WIDTH));

        rectPaint = new Paint();

        //cursor attributes
        rectPaint.setColor(ContextCompat.getColor(context,R.color.red));
        rectPaint.setAlpha(80);
        rectPaint.setAntiAlias(true);
        rectPaint.setDither(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(Utils.convertDpToPixel(getContext(), RECT_LINE_STROKE_WIDTH));

        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);

    }

    public void setDrawingEnabled(boolean isEditable){
        this.isEditable = isEditable;
    }

    public boolean isDrawingEnabled(){
        return isEditable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawPath != null){
            canvas.drawPath(drawPath, drawPaint);
        }
        if (listener != null){
            for (BoundingRect rect : listener.fetchSavedRectanglesToDraw()){
                // Draw rectangle
                float strokeWidth = listener.getScale() * origSavedRectStrokeWidthInPx;
                rectPaint.setStrokeWidth(strokeWidth);
                canvas.drawRect(rect.getRect(), rectPaint);

                // Draw label
                float scaledTextSize = listener.getScale() * origTextSizeInPx;
                textPaint.setTextSize(scaledTextSize);
                float labelWidth = textPaint.measureText(rect.getBoxClass());
                canvas.drawText(rect.getBoxClass(), rect.getRect().right - labelWidth + strokeWidth,
                        rect.getRect().top - strokeWidth, textPaint);
            }

        }
    }

    public void refresh(){
        invalidate();
    }

    private void resetRectanlgeCoordinates(){
        left = Integer.MAX_VALUE;
        top = Integer.MAX_VALUE;
        right = Integer.MIN_VALUE;
        bottom = Integer.MIN_VALUE;
    }

    final GestureDetector gestureDetector = new GestureDetector(this.getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                public void onLongPress(MotionEvent e) {
                    Log.e(TAG, "onLongPress, x = " + e.getX() + ", y = " + e.getY());
                    // Transform the point to original image's scale and then check if it is inside one of the saved rectangles
                    Point point = new Point(Math.round(e.getX()), Math.round(e.getY()));
                    if (listener != null && listener.highlightBoundingRectangle(point)){
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        longPressConsumed = true;
                    }
                }
            });

    boolean longPressConsumed = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {            //add to paths
        if(isEditable){
            gestureDetector.onTouchEvent(event);
            float touchX = event.getX() - 10;
            float touchY = event.getY() - 10;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("DrawableView", "ACTION_DOWN");
                    //clear x ,y coordinates ,rectangle etc of previous instance
                    resetRectanlgeCoordinates();
                    drawPath = new Path();
                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    if (!longPressConsumed){
                        drawPath.lineTo(touchX, touchY);
                        float[] rectangle = new float[4];
                        rectangle[0] = left
                                - drawPaint.getStrokeWidth()/2;
                        rectangle[1] = top
                                - drawPaint.getStrokeWidth()/2;
                        rectangle[2] = right
                                + drawPaint.getStrokeWidth()/2;
                        rectangle[3] = bottom
                                + drawPaint.getStrokeWidth()/2;
                        RectF freshRect = new RectF(rectangle[0],rectangle[1],rectangle[2],rectangle[3]);
                        Log.d(TAG, "Freshly baked Rect: " + freshRect);
                        if (listener != null){
                            listener.onPaintRectangle(freshRect);
                        }
                        drawPath = null;
                    }else {
                        drawPath = null;
                        longPressConsumed = false;
                    }
                    break;
                default:
                    return false;
            }

            if (touchX <= left) {
                left = touchX;
            }
            if (touchX >= right){
                right = touchX;
            }
            if (touchY <= top){
                top = touchY;
            }
            if (touchY >= bottom){
                bottom = touchY;
            }

        } else{
            return false;
        }
        invalidate();
        return true;
    }

    public interface Listener{

        /**
         * Draws the bounding rectangle immediately after user finishes stroking by taking his/her finger up
         * @param paitedRectangle
         */
        void onPaintRectangle(RectF paitedRectangle);

        /**
         * Fetches the transformed coordinated of all the saved rectangles which are currently not activated
         * @return
         */
        List<BoundingRect> fetchSavedRectanglesToDraw();

        /**
         * Fetched current scale of the image
         * @return
         */
        float getScale();

        /**
         * It the input point lies inside a saved rectangle, it will get highlighted i.e. edit mode will be activated
         * @param point inputPoint
         * @return true if input point does lie inside a saved rectangle and bounding rectangle got highlighted
         */
        boolean highlightBoundingRectangle(Point point);
    }
}