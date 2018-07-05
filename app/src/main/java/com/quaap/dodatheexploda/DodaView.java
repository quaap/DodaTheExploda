package com.quaap.dodatheexploda;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class DodaView extends View {

    private Paint mTextPaint;

    private final List<String> mItems = new ArrayList<>();
    private final List<Point> mLocations = new ArrayList<>();
    private final List<Float> mSizes = new ArrayList<>();
    private final List<Rect> mMeasuredSizes = new ArrayList<>();

    private OnItemTouchListener onItemTouchListener;

    public DodaView(Context context) {
        super(context);
        init();
    }

    public DodaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DodaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void addText(Point location, float size, String text) {
        mItems.add(text);
        mLocations.add(location);
        mSizes.add(size);
        mTextPaint.setTextSize(size);

        Rect a = new Rect();

        mTextPaint.getTextBounds(text,0, text.length(), a);
        mMeasuredSizes.add(a);
        //Log.d("DodaView", a.top+ " " + a.left + " " + a.bottom + " " + a.right);

    }


    public void setTextHeight(float height) {
        if (height != 0) {
            mTextPaint.setTextSize(height);
        }
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        setTextHeight(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void removeAllItems() {
        synchronized (mItems) {
            mItems.clear();
            mLocations.clear();
            mSizes.clear();
            mMeasuredSizes.clear();
        }
    }

    public void pop() {
        synchronized (mItems) {
            mItems.remove(mItems.size()-1);
            mSizes.remove(mSizes.size()-1);
            mLocations.remove(mLocations.size()-1);
            if (mMeasuredSizes.size()>0) mMeasuredSizes.remove(mMeasuredSizes.size()-1);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (mItems) {
            for (int i = 0; i < mItems.size(); i++) {
                Point p = mLocations.get(i);
                String text = mItems.get(i);
                Float size = mSizes.get(i);

                mTextPaint.setTextSize(size);
                canvas.drawText(text, p.x, p.y, mTextPaint);
            }
        }

        super.onDraw(canvas);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        Log.d("DodaView", "xy= " + x + "," + y);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                for (int i = mLocations.size()-1; i >=0 ; i--) {
                    Point p = mLocations.get(i);
                    Rect bounds = mMeasuredSizes.get(i);
                    //if (x>p.x && x<p.x+size && y<p.y && y>p.y-size) {
                    if (x>p.x+bounds.left && x<p.x+bounds.right && y<p.y+bounds.bottom && y>p.y+bounds.top) {
                        if (onItemTouchListener!=null) {
                            onItemTouchListener.onItemClick(mItems.get(i));
                            return true;
                        }
                    }

                }

        }
        performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    public interface OnItemTouchListener {
        void onItemClick(String text);
    }
}
