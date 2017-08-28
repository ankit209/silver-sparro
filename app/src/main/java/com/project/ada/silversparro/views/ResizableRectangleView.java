package com.project.ada.silversparro.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.project.ada.silversparro.R;
import com.project.ada.silversparro.utils.Utils;

import java.util.ArrayList;

import static com.project.ada.silversparro.utils.Utils.drawableToBitmap;

/**
 * Created by ankitmaheshwari on 8/15/17.
 */

public class ResizableRectangleView extends View {

    private static final String TAG = "ResizableRectangleView";

    Point[] points = new Point[4];

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    int groupId = -1;
    private ArrayList<ColorBall> colorballs = new ArrayList<>();
    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;
    Canvas canvas;
    SaveListener listener;


    public ResizableRectangleView(Context context) {
        super(context);
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
    }

    public ResizableRectangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
    }

    public ResizableRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
    }

    public void setListener(SaveListener listener){
        this.listener = listener;
    }

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        if (points.length != 4){
            return;
        }
//        if(points[3]==null) //point4 null when user did not touch and move on screen.
//            return;
//        int left, top, right, bottom;
//        left = points[0].x;
//        top = points[0].y;
//        right = points[0].x;
//        bottom = points[0].y;
//        for (int i = 1; i < points.length; i++) {
//            left = left > points[i].x ? points[i].x:left;
//            top = top > points[i].y ? points[i].y:top;
//            right = right < points[i].x ? points[i].x:right;
//            bottom = bottom < points[i].y ? points[i].y:bottom;
//        }
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5);

        //draw stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#AADB1255"));
        paint.setStrokeWidth(2);

        canvas.drawRect(getCurrentRectF(), paint);
        //fill the rectangle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#55DB1255"));
        paint.setStrokeWidth(0);
        canvas.drawRect(getCurrentRectF() , paint);

        // draw the balls on the canvas
        paint.setColor(Color.BLUE);
        paint.setTextSize(18);
        paint.setStrokeWidth(0);
        for (int i =0; i < colorballs.size(); i ++) {
            ColorBall ball = colorballs.get(i);
            canvas.drawBitmap(ball.getBitmap(), ball.getCenterX() - (ball.getWidthOfBall() / 2),
                    ball.getCenterY() - (ball.getHeightOfBall() / 2), paint);
        }
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        int eventaction = event.getAction();
        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                if (points[0] == null) {
                    Log.d(TAG, "ACTION_DOWN: Do nothing");
                } else {
                    //resize rectangle
                    balID = -1;
                    groupId = -1;
                    for (int i = colorballs.size()-1; i>=0; i--) {
                        ColorBall ball = colorballs.get(i);
                        // check if inside the bounds of the ball (circle)
                        // get the center for the ball
                        int centerX = ball.getCenterX();
                        int centerY = ball.getCenterY();
                        paint.setColor(Color.CYAN);
                        // calculate the radius from the touch to the center of the
                        // ball
                        double radCircle = Math
                                .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                        * (centerY - Y)));

                        if (radCircle < ball.getWidthOfBall()) {

                            balID = ball.getID();
                            if (balID == 1 || balID == 3) {
                                groupId = 2;
                            } else {
                                groupId = 1;
                            }
                            invalidate();
                            break;
                        }
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball


                if (balID > -1) {
                    // move the balls the same as the finger
                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);

                    paint.setColor(Color.CYAN);
                    if (groupId == 1) {
                        colorballs.get(1).setX(colorballs.get(0).getCenterX());
                        colorballs.get(1).setY(colorballs.get(2).getCenterY());
                        colorballs.get(3).setX(colorballs.get(2).getCenterX());
                        colorballs.get(3).setY(colorballs.get(0).getCenterY());
                    } else {
                        colorballs.get(0).setX(colorballs.get(1).getCenterX());
                        colorballs.get(0).setY(colorballs.get(3).getCenterY());
                        colorballs.get(2).setX(colorballs.get(3).getCenterX());
                        colorballs.get(2).setY(colorballs.get(1).getCenterY());
                    }

                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping
                break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }

    public void activateWith(RectF rectF){
        Log.d(TAG, "activateWith: " + rectF);
        //initialize rectangle.
        points[0] = new Point();
        points[0].x = Math.round(rectF.left);
        points[0].y = Math.round(rectF.top);

        points[1] = new Point();
        points[1].x = Math.round(rectF.left);
        points[1].y = Math.round(rectF.bottom);

        points[2] = new Point();
        points[2].x = Math.round(rectF.right);
        points[2].y = Math.round(rectF.bottom);

        points[3] = new Point();
        points[3].x = Math.round(rectF.right);
        points[3].y = Math.round(rectF.top);

        balID = 2;
        groupId = 1;
        // declare each ball with the ColorBall class
        for(int i=0; i<points.length; i++){
            colorballs.add(new ColorBall(getContext(), R.drawable.corner_circle, points[i], i));
        }

        this.setVisibility(VISIBLE);

        invalidate();

    }

    final GestureDetector gestureDetector = new GestureDetector(this.getContext(),
            new GestureDetector.SimpleOnGestureListener() {
        public void onLongPress(MotionEvent e) {
            Log.e(TAG, "onLongPress, x = " + e.getX() + ", y = " + e.getY());
            // Check if point is inside active rectangle, if yes then pass it on to Listener, if not then pass on the event to sibling view
            RectF currentRect = getCurrentRectF();
            Log.d(TAG, "onLongPress, currentRect = " + currentRect);
            if (currentRect.contains(e.getX(), e.getY())){
                // Long press inside rectangle
                if (listener != null){
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    listener.onSaveRect(getCurrentRectF());
                }
            }
        }
    });

    public void deactivate(){
        Log.d(TAG, "deactivate");
        colorballs.removeAll(colorballs);
        this.setVisibility(GONE);
        invalidate();
    }

    public RectF getCurrentRectF(){
        return Utils.convertPointsArrayToRectF(points);
    }

    public static class ColorBall {

        Bitmap bitmap;
        Context mContext;
        Point point;
        int id;

        public ColorBall(Context context, int resourceId, Point point, int id) {
            this.id = id;

            Drawable drawable = ContextCompat.getDrawable(context, resourceId);
            bitmap = drawableToBitmap(drawable);

            mContext = context;
            this.point = point;
        }

        public int getWidthOfBall() {
            return bitmap.getWidth();
        }

        public int getHeightOfBall() {
            return bitmap.getHeight();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getCenterX() {
            return point.x;
        }

        public int getCenterY() {
            return point.y;
        }

        public int getID() {
            return id;
        }

        public void setX(int x) {
            point.x = x;
        }

        public void setY(int y) {
            point.y = y;
        }
    }

    public interface SaveListener{
        void onSaveRect(RectF rectF);
    }

}
