package com.quaap.dodatheexploda;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
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

        mPlodeTime = 0;
        for (int i = 0; i < mPlode.getNumberOfFrames(); i++) {
            mPlodeTime += mPlode.getDuration(i);
        }

    }


    public int addText(Point location, float size, String text) {
        try {
            synchronized (mItems) {
                mItems.add(text);
                mLocations.add(location);
                mSizes.add(size);
                mTextPaint.setTextSize(size);

                Rect a = new Rect();

                mTextPaint.getTextBounds(text, 0, text.length(), a);
                mMeasuredSizes.add(a);
                return mItems.size() - 1;
            }
        } finally {
            //Log.d("DodaView", a.top+ " " + a.left + " " + a.bottom + " " + a.right);
            invalidate();
        }

    }


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
            mSizes.clear();
            mMeasuredSizes.clear();
        }
        invalidate();
    }

    public void pop() {
        synchronized (mItems) {
            if (mItems.size()>0) {
                mItems.remove(mItems.size()-1);
                mSizes.remove(mSizes.size()-1);
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
    public int count() {
        synchronized (mItems) {
            return mItems.size();
        }
    }

    public void startPlode() {

        //    int time = mPlode.getNumberOfFrames() * mPlode.getDuration(0);
        mPlode.setCallback(new Drawable.Callback() {
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

        if (mPlode.isRunning()) {
            mPlode.draw(canvas);
        }

        super.onDraw(canvas);
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
