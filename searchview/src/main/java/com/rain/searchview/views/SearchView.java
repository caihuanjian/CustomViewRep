package com.rain.searchview.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by HwanJ.Choi on 2017-9-18.
 */

public class SearchView extends View {

    enum State {
        None, Start, Serching, End
    }

    private int mBorderWidth = 13;

    private int mWidth;

    private int mHeight;

    private int mInnerCircleRadius = 40;

    private int mOutterCircleRadius = 100;

    private PathMeasure mPathMeasure;

    private Path mInerpath;
    private Path mOutterPath;
    private Path drawingPath;

    private Paint mPaint;
    private State mState = State.None;

    private boolean isOver;

    private int mStartDuration = 1700;

    private OutterPathCaculator mOutterCaculator;
    private InnerPathCaculator mInnerCaculator;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {

        this(context, attrs, 0);
    }


    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);
        mPathMeasure = new PathMeasure();
        mOutterCaculator = new OutterPathCaculator(mPathMeasure);
        mInnerCaculator = new InnerPathCaculator(mPathMeasure);

        RectF in = new RectF(-mInnerCircleRadius, -mInnerCircleRadius, mInnerCircleRadius, mInnerCircleRadius);
        mInerpath = new Path();
        mInerpath.addArc(in, 45, 359.9f);

        RectF out = new RectF(-mOutterCircleRadius, -mOutterCircleRadius, mOutterCircleRadius, mOutterCircleRadius);
        mOutterPath = new Path();
        mOutterPath.addArc(out, 45, 359.9f);
        PathMeasure pathMeasure = new PathMeasure(mOutterPath, false);
        float[] pos = new float[2];
        pathMeasure.getPosTan(0, pos, null);
        mInerpath.lineTo(pos[0], pos[1]);
        drawingPath = mInerpath;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    private int measureSize(int measureSpec) {
        int result = mOutterCircleRadius * 2 + mBorderWidth;
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(mWidth / 2, mWidth / 2);
        if (drawingPath != null) {
            canvas.drawPath(drawingPath, mPaint);
        }
        canvas.restoreToCount(saveCount);
    }

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private class AnimatedRunnable implements Runnable {

        private final long mStartTime;
        private PathCaculator caculator;

        public AnimatedRunnable(PathCaculator caculator) {
            this.caculator = caculator;
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {

            float t = interpolate();

            Path dst = caculator.caculatPath(t);
            onSearching(dst, t);

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                postOnAnimation(this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mStartDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private void onSearching(Path dst, float progress) {
        Log.d("chj", progress + " ");
        if (progress <= 1 && mState != State.None) {
            drawingPath = dst;
            if (progress >= 1) {
                updateState();
            }
        } else {
            updateState();
        }
        postInvalidate();
    }

    private void updateState() {
        if (mState == State.None) {
            mState = State.Start;
            mPathMeasure.setPath(mInerpath, false);
            postOnAnimation(new AnimatedRunnable(mInnerCaculator));
        } else if (mState == State.Start) {
            mState = State.Serching;
            mPathMeasure.setPath(mOutterPath, false);
            postOnAnimation(new AnimatedRunnable(mOutterCaculator));
        } else if (mState == State.Serching) {
            if (isOver) {
                mState = State.End;
                mPathMeasure.setPath(mInerpath, false);
                postOnAnimation(new AnimatedRunnable(new EndingPathCaculator(mPathMeasure)));
            } else {
                postOnAnimation(new AnimatedRunnable(mOutterCaculator));
            }
        } else if (mState == State.End) {
            mState = State.None;
        }
    }

    public void start() {
        if (mState != State.None) {
            return;
        }
        isOver = false;
        updateState();
    }

    public void stop() {
        isOver = true;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }
}
