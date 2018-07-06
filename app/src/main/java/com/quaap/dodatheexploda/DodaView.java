package com.quaap.dodatheexploda;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;


public class DodaView extends View {

    private Paint mTextPaint;

    private final List<String> mItems = new ArrayList<>();
    private final List<Point> mLocations = new ArrayList<>();
    //private final List<Float> mSizes = new ArrayList<>();
    private final List<Rect> mMeasuredSizes = new ArrayList<>();
    private final List<Bitmap> mBitmaps = new ArrayList<>();

    private OnItemTouchListener onItemTouchListener;

    private int mHighlight = -1;

    private AnimationDrawable mPlode;
    private int mPlodeTime;


    public DodaView(Context context) {
        super(context);
        init(context);
    }

    public DodaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DodaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        setTextHeight(0);
        if (Build.VERSION.SDK_INT>=21) {
            mPlode = (AnimationDrawable) context.getResources().getDrawable(R.drawable.explosion, null);
        } else {
            mPlode = (AnimationDrawable) context.getResources().getDrawable(R.drawable.explosion);
        }

        mPlode.setAlpha(180);
        mPlodeTime = 0;
        for (int i = 0; i < mPlode.getNumberOfFrames(); i++) {
            mPlodeTime += mPlode.getDuration(i);
        }


    }


    public void addText(Point location, float size, String text) {
        synchronized (mItems) {
            mItems.add(text);
            mLocations.add(location);
            //mSizes.add(size);

            mTextPaint.setTextSize(size);

            Rect r = new Rect();

            mTextPaint.getTextBounds(text, 0, text.length(), r);

            r.set(0,0, r.right-r.left, (int)(r.bottom-r.top + Math.abs(mTextPaint.descent())));

            mMeasuredSizes.add(r);

            Canvas c = new Canvas();
            Bitmap b = Bitmap.createBitmap(r.right, r.bottom, Bitmap.Config.ARGB_8888);

            c.setBitmap(b);
            c.drawColor(Color.TRANSPARENT);
            c.drawText(text, 0, c.getHeight()-mTextPaint.descent(), mTextPaint);
            //return mItems.size() - 1;
            //Log.d("DodaView", a.top+ " " + a.left + " " + a.bottom + " " + a.right);

            mBitmaps.add(b);
        }
        postInvalidate();

    }


//    private Bitmap getBitmapFromText(String text, int fsize) {
//
//
//
//        mTextPaint.setTextSize(fsize);
//
//        Rect r = new Rect();
//
//        mTextPaint.getTextBounds(text, 0, text.length(), r);
//
//        r.set(0,0, r.right-r.left, (int)(r.bottom-r.top + Math.abs(mTextPaint.descent())));
//
//        mMeasuredSizes.add(r);
//
//        Canvas c = new Canvas();
//        Bitmap b = Bitmap.createBitmap(r.left, r.bottom, Bitmap.Config.ARGB_8888);
//
//        c.setBitmap(b);
//        c.drawColor(Color.TRANSPARENT);
//        c.drawText(text, 0, c.getHeight()-mTextPaint.descent(), mTextPaint);
//
//        return b;
//    }


//    private Bitmap getBitmap(int i) {
//
//
//        Float size = mSizes.get(i);
//        String text = mItems.get(i);
//        mTextPaint.setTextSize(size);
//
//        Rect r = mMeasuredSizes.get(i);
//
//        Canvas c = new Canvas();
//        Bitmap b = Bitmap.createBitmap(r.right-r.left, (int)(r.bottom-r.top + Math.abs(mTextPaint.descent())), Bitmap.Config.ARGB_8888);
//
//        c.setBitmap(b);
//        c.drawColor(Color.TRANSPARENT);
//        c.drawText(text, 0, c.getHeight()-mTextPaint.descent(), mTextPaint);
//
//        return b;
//    }


    public void setTextHeight(float height) {
        if (height != 0) {
            mTextPaint.setTextSize(height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void removeAllItems() {
        synchronized (mItems) {
            mItems.clear();
            mLocations.clear();
            mBitmaps.clear();
            mMeasuredSizes.clear();
        }
        invalidate();
    }

    public void pop() {
        synchronized (mItems) {
            if (mItems.size()>0) {
                mItems.remove(mItems.size()-1);
                mBitmaps.remove(mBitmaps.size()-1);
                mLocations.remove(mLocations.size()-1);
                if (mMeasuredSizes.size()>0) mMeasuredSizes.remove(mMeasuredSizes.size()-1);
            }
        }
        invalidate();
    }

    public String peek() {
        synchronized (mItems) {
            if (mItems.size()>0) {
                return mItems.get(mItems.size() - 1);
            }
            return null;
        }
    }

    public void highlightTop() {
        highlight(mItems.size()-1);
    }

    public void highlight(String text) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).equals(text)) {
                highlight(i);
                break;
            }
        }

    }

    public void highlightOff() {
        mHighlight = -1;
        invalidate();
    }


    private void highlight(final int h) {
        mHighlight = h;
        invalidate();
        long millis = 1000;

        postDelayed(new Runnable() {
            @Override
            public void run() {
                highlightOff();
            }
        }, millis);

    }

    public int count() {
        synchronized (mItems) {
            return mItems.size();
        }
    }

    public void startPlode() {

        setAnimationDrawableCallback(mPlode);

        if (mMeasuredSizes.size()>0) {

            Rect r = mMeasuredSizes.get(mMeasuredSizes.size() - 1);
            Point p = mLocations.get(mMeasuredSizes.size() - 1);
            mPlode.setBounds(r.left+p.x, r.top+p.y, r.right+p.x, r.bottom+p.y);
            mPlode.setVisible(true,true);
            mPlode.start();

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlode.stop();
                    invalidate();
                }
            }, mPlodeTime+20);

        }

    }

    private void setAnimationDrawableCallback(AnimationDrawable draw) {
        draw.setCallback(new Drawable.Callback() {
            @Override
            public void invalidateDrawable(Drawable drawable) {
                DodaView.this.postInvalidate();
            }

            @Override
            public void scheduleDrawable(Drawable drawable, Runnable runnable, long when) {
                DodaView.this.postDelayed(runnable, when - SystemClock.uptimeMillis());
            }

            @Override
            public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
                DodaView.this.removeCallbacks(runnable);
            }
        });
    }

//    private AnimationDrawable makeAni(int i) {
//
//        AnimationDrawable a = new AnimationDrawable();
//
//        Float size = mSizes.get(i);
//        String text = mItems.get(i);
//        mTextPaint.setTextSize(size);
//
//        Rect r = mMeasuredSizes.get(i);
//
//        Canvas c = new Canvas();
//        Bitmap b = Bitmap.createBitmap(r.right-r.left, r.bottom-r.top, Bitmap.Config.ARGB_8888);
//        int step =  360/20;
//        for (int ang=step; ang<360; ang+=step) {
//            c.setBitmap(b);
//            //c.drawColor(Color.TRANSPARENT);
//            c.drawText(text, 0, c.getHeight(), mTextPaint);
//            c.rotate(ang, (r.right - r.left) / 2, (r.bottom - r.top) / 2);
//            BitmapDrawable bm = new BitmapDrawable(getResources(), b);
//            a.addFrame(bm, 50);
//        }
//        Point p = mLocations.get(mMeasuredSizes.size() - 1);
//        a.setBounds(r.left+p.x, r.top+p.y, r.right+p.x, r.bottom+p.y);
//        setAnimationDrawableCallback(a);
//        return a;
//    }

//    AnimationDrawable mHighlightedAni;



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        synchronized (mItems) {
            for (int i = 0; i < mItems.size(); i++) {
                String text = mItems.get(i);
                Point p = mLocations.get(i);
//                Float size = mSizes.get(i);

//                int adj = 0;
//
//                if (mHighlight==i) {
//                    size *= 1.5f;
//                    adj = (int)(size/5);
//
//                }
//                mTextPaint.setTextSize(size);
//                Log.d("DodaView", i + " " + text);
                //canvas.drawText(text, p.x-adj, p.y+adj, mTextPaint);
                Bitmap b = mBitmaps.get(i);
                canvas.drawBitmap(b, p.x, p.y, mTextPaint);

            }
        }

        if (mPlode.isRunning()) {
            mPlode.draw(canvas);
        }

        //super.onDraw(canvas);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        //Log.d("DodaView", "xy= " + x + "," + y);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                synchronized (mItems) {
                    for (int i = mLocations.size() - 1; i >= 0; i--) {
                        Point p = mLocations.get(i);
                        Rect bounds = mMeasuredSizes.get(i);
                        //if (x>p.x && x<p.x+size && y<p.y && y>p.y-size) {
                        if (x > p.x + bounds.left && x < p.x + bounds.right && y < p.y + bounds.bottom && y > p.y + bounds.top) {
                            if (onItemTouchListener != null) {
                                onItemTouchListener.onItemClick(mItems.get(i));
                                return true;
                            }
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
