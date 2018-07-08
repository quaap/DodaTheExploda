package com.quaap.dodatheexploda;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class DodaView extends View {

    private Paint mTextPaint;

    private final List<String> mItems = new ArrayList<>();
    private final List<Point> mLocations = new ArrayList<>();
    private final List<Rect> mMeasuredSizes = new ArrayList<>();
    private final List<Bitmap> mBitmaps = new ArrayList<>();

    private final Map<Integer,AnimationDrawable> aniCache = new WeakHashMap<>();

    private OnItemTouchListener onItemTouchListener;

    private int mHighlight = -1;

    private AnimationDrawable mPlode;

    private Mode mMode = Mode.Child;

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
        setAnimationDrawableCallback(mPlode);
        mPlode.setAlpha(180);


    }

    public void setMode(Mode mode) {
        mMode = mode;
    }

    public void addText(float size, String text) {
        synchronized (mItems) {
            mItems.add(text);

            mTextPaint.setTextSize(size);

            Rect r = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), r);

            r.set(0,0, r.right-r.left+6, (int)(r.bottom-r.top + Math.abs(mTextPaint.descent())));

            mMeasuredSizes.add(r);

            Point location;

            int tries=0;
            boolean done = true;
            do {
                done = true;
                location = new Point(getRandomInt(getWidth() - r.right*1.5) + r.right/10, getRandomInt(getHeight() - r.bottom*1.5) + r.bottom/10);
                for (int i=0; i<mLocations.size(); i++) {
                    Point p2 = mLocations.get(i);
                    Rect r2 = mMeasuredSizes.get(i);
                    if (Math.abs(p2.x - location.x)/mMode.getOverLap() < r2.right && Math.abs(p2.y - location.y)/mMode.getOverLap() < r2.bottom) {
                        done = false;
                        break;
                    }
                }
            } while (!done && tries++<mMode.getNumIcons()*3);

            mLocations.add(location);


            Canvas c = new Canvas();
            Bitmap b = Bitmap.createBitmap(r.right, r.bottom, Bitmap.Config.ARGB_8888);

            c.setBitmap(b);
            c.drawColor(Color.TRANSPARENT);
            c.drawText(text, 0, c.getHeight()-mTextPaint.descent(), mTextPaint);

            mBitmaps.add(b);

        }
        postInvalidate();

    }
    private static int getRandomInt(double max) {
        return (int)(Math.random()*max);

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
            mBitmaps.clear();
            mMeasuredSizes.clear();
            aniCache.clear();
        }
        invalidate();
    }

    public void pop() {
        synchronized (mItems) {
            if (mItems.size()>0) {
                int i=mItems.size()-1;
                mItems.remove(i);
                mBitmaps.remove(mBitmaps.size()-1);
                mLocations.remove(mLocations.size()-1);
                mMeasuredSizes.remove(mMeasuredSizes.size()-1);
                synchronized (aniCache) {
                    aniCache.remove(i);
                }
            }
        }
        invalidate();
    }

    public String peek(boolean cacheani) {
        synchronized (mItems) {
            if (mItems.size()>0) {
                if (cacheani) {
                    getAni(mItems.size() - 1, false);
                }
                return mItems.get(mItems.size() - 1);
            }
            return null;
        }
    }

    public void highlightTop() {
        synchronized (mItems) {
            if (mItems.size()>0) {
                highlight(mItems.size() - 1);
            }
        }
    }

    public void highlight(String text) {
        synchronized (mItems) {
            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).equals(text)) {
                    highlight(i);
                    break;
                }
            }
        }
    }

    public void highlightOff() {
        mHighlight = -1;
        invalidate();
    }


    private void highlight(final int h) {
        mHighlight = h;
        mHighlightedAni = getAni(h, true);
        postInvalidate();
    }

    public int count() {
        synchronized (mItems) {
            return mItems.size();
        }
    }

    public void startPlode() {

        if (mMeasuredSizes.size()>0) {

            Rect r = mMeasuredSizes.get(mMeasuredSizes.size() - 1);
            Point p = mLocations.get(mMeasuredSizes.size() - 1);
            mPlode.setBounds(p.x, p.y, r.right+p.x, r.bottom+p.y);
            setAnimationDrawableCallback(mPlode);
            startAni(mPlode);

        }

    }

    private void setAnimationDrawableCallback(final AnimationDrawable draw) {
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


    private void startAni(final AnimationDrawable draw) {

        int aniTime = 0;

        for (int i = 0; i < draw.getNumberOfFrames(); i++) {
            aniTime += draw.getDuration(i);
        }

        draw.start();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                draw.stop();
                if (draw == mHighlightedAni) {
                    mHighlightedAni = null;
                    mHighlight=-1;
                }
                postInvalidate();
            }
        }, aniTime+100);

    }



    private  AnimationDrawable getAni(int i, boolean start) {

        synchronized (mItems) {
            if (i>=mItems.size()) return null;

            AnimationDrawable a = aniCache.get(i);
            Bitmap orig = mBitmaps.get(i);
            if (a == null) {
                a = new AnimationDrawable();
                aniCache.put(i, a);

                int step = 360 / 20;
                for (int ang = step; ang < 361; ang += step) {
                    Matrix matrix = new Matrix();

                    int size = Math.max(orig.getWidth(), orig.getHeight());
                    matrix.postRotate(ang,size/2, size/2);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(orig, size, size, true);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                    BitmapDrawable bm = new BitmapDrawable(getResources(), rotatedBitmap);
                    a.addFrame(bm, 50);
                }

            }
            setAnimationDrawableCallback(a);
            a.setOneShot(true);

            Point p = mLocations.get(i);
            a.setBounds(p.x, p.y, p.x + orig.getWidth(), p.y + orig.getHeight());
            if (start) {
                startAni(a);
            }

            return a;
        }
    }

    private AnimationDrawable mHighlightedAni;


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        synchronized (mItems) {
            for (int i = 0; i < mItems.size(); i++) {
                String text = mItems.get(i);
                Point p = mLocations.get(i);

                if (mHighlight!=i) {
                    Bitmap b = mBitmaps.get(i);
                    canvas.drawBitmap(b, p.x, p.y, mTextPaint);
                } else {
                    if (mHighlightedAni!=null && mHighlightedAni.isRunning()) {
                        mHighlightedAni.draw(canvas);
                    }

                }


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
