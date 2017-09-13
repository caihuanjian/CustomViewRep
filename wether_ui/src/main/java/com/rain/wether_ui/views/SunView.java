package com.rain.wether_ui.views;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.rain.wether_ui.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by HwanJ.Choi on 2017-7-26.
 */

public class SunView extends View {

    private static final int DEFULT_SIZE = 400;
    private static final int ONE_DAY_MINITE = 24 * 60;

    private static final int SUN_DOT_RADIUS = 5;
    private static final int LIGHT_SPACE = 5;
    private static final int MIN_LIGHT_LENGTH = 5;
    private static final int DEFULT_LIGHT_NUM = 10;
    private static final int DEFULT_TEXT_SIZE = 20;
    private static final int DEFULT_RISE_TIME = 360;//06:00
    private static final int DEFULT_DOWN_TIME = 1110;//18:30

    private Paint mSunPaint;
    private Paint mLinePaint;
    private Paint mArcPaint;
    private Paint mTextPaint;

    private int mWidth;
    private int mHeight;

    private int mCurTimeMinite = 900;

    private int mSunRiseTime = DEFULT_RISE_TIME;
    private int mSunDownTime = DEFULT_DOWN_TIME;

    private float mSunDotRadius;
    private float mSunRadius;
    private float mSunLightLength;

    private float mRisePos;
    private float mDownPos;

    private float mCenterX;
    private float mCenterY;

    private float mCurSunPosX;
    private float mCurSunPosY;

    private int mTextSize;
    private int mTextOffsetTop;
    private float mTextHeight;

    private int mLightNum = DEFULT_LIGHT_NUM;

    private int mColorFill;
    private int mLineColor;
    private int mSunColor;

    private SunViewAnimaTask mAnimaTask;

    public SunView(Context context) {
        this(context, null);
    }

    public SunView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SunView);
        mTextOffsetTop = typedArray.getDimensionPixelSize(R.styleable.SunView_textOffsetTop, 0);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.SunView_timeTextSize, DEFULT_TEXT_SIZE);
        mColorFill = typedArray.getColor(R.styleable.SunView_fillColor, Color.parseColor("#964d441c"));
        mLineColor = typedArray.getColor(R.styleable.SunView_lineColor, Color.WHITE);
        mSunColor = typedArray.getColor(R.styleable.SunView_sunColor, Color.YELLOW);
        mSunDotRadius = typedArray.getDimensionPixelSize(R.styleable.SunView_sunDotRadius, SUN_DOT_RADIUS);
        mSunRadius = mSunDotRadius + 2;
        mSunLightLength = Math.max(MIN_LIGHT_LENGTH, mSunRadius - 3);
        typedArray.recycle();
        init();
    }

    private void init() {
        mLinePaint = new Paint(ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2);

        mSunPaint = new Paint(ANTI_ALIAS_FLAG);
        mSunPaint.setColor(mSunColor);
        mSunPaint.setDither(true);

        mArcPaint = new Paint(ANTI_ALIAS_FLAG);
        mArcPaint.setColor(mLineColor);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(2);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextHeight = mTextPaint.descent() - mTextPaint.ascent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = measureSize(widthMeasureSpec);
        setMeasuredDimension(width, measureHeight(width));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;

        mRisePos = caculatePos(mSunRiseTime, mWidth);
        mDownPos = caculatePos(mSunDownTime, mWidth);

        mCenterX = (mRisePos + mDownPos) / 2;//圆弧的圆心X坐标
        mCenterY = mHeight - mSunDotRadius - mTextHeight - mTextOffsetTop;//圆弧的圆心Y坐标
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0, mCenterY, mWidth, mCenterY, mLinePaint);
        drawArc(canvas);
        drawSun(canvas);
        drawRiseDown(canvas);
        drawText(canvas);
    }

    private void drawArc(Canvas canvas) {
        canvas.save();
        final float arcRadius = getArcRadius(mWidth);
        float left = mRisePos;
        float top = mSunRadius + LIGHT_SPACE + mSunLightLength;
        float right = left + 2 * arcRadius;
        float bottom = top + 2 * arcRadius;

        PathEffect pathEffect = new DashPathEffect(new float[]{5, 3}, 0);
        mArcPaint.setPathEffect(pathEffect);
        canvas.drawArc(left, top, right, bottom, 180, 180, false, mArcPaint);

        canvas.restore();
    }

    private void drawRiseDown(Canvas canvas) {
        mSunPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(caculatePos(mSunRiseTime, mWidth), mCenterY, mSunDotRadius, mSunPaint);
        canvas.drawCircle(caculatePos(mSunDownTime, mWidth), mCenterY, mSunDotRadius, mSunPaint);
    }

    private void drawSun(Canvas canvas) {
        if (mCurTimeMinite < mSunRiseTime || mCurTimeMinite > mSunDownTime) {
            return;
        }
        mSunPaint.setStyle(Paint.Style.STROKE);
        mSunPaint.setStrokeWidth(3);

        final int arcRadius = getArcRadius(mWidth);//圆弧半径
        final int dayTime = mSunDownTime - mSunRiseTime;
        float proportion = (mCurTimeMinite - mSunRiseTime) * 1.0f / dayTime;//占比
        float proporAngle = proportion * 180;

        //这种先确定X坐标，再根据圆的计算公式确定Y坐标
//        final float curSunPosX = proportion * (mDownPos - mRisePos) + mRisePos;//当前太阳x坐标
//        final float curSunPosY = (float) (mCenterY - Math.sqrt(arcRadius * arcRadius - (curSunPosX - mCenterX) * (curSunPosX - mCenterX)));//圆的计算公式
        mCurSunPosX = (float) (mCenterX + Math.cos(Math.toRadians(proporAngle + 180)) * arcRadius);//当前太阳x坐标
        mCurSunPosY = (float) (mCenterY + Math.sin(Math.toRadians(proporAngle + 180)) * arcRadius);//当前太阳y坐标
        //画填充颜色
        drawFillColor(canvas, mCurSunPosX, mCurSunPosY, proportion * 180);
        canvas.save();
        //画太阳圆圈
        canvas.rotate(proporAngle, mCenterX, mCenterY);
        canvas.drawCircle(mRisePos, mCenterY, mSunRadius, mSunPaint);

        //画太阳光线
        float angleStep = 360 / mLightNum;
        float startY = mCenterY - mSunRadius - LIGHT_SPACE;
        for (int i = 0; i < mLightNum; i++) {
            canvas.drawLine(mRisePos, startY, mRisePos, startY - mSunLightLength, mSunPaint);
            canvas.rotate(angleStep, mRisePos, mCenterY);
        }
        canvas.restore();

    }

    private void drawText(Canvas canvas) {
        //x,y参数不是文本的左上角，在没有设置Align的情况下，x指文本的左边界在屏幕的位置，y指文本的baseline在屏幕的位置（比如文本：Ag（字符A左下角那个点在屏幕上的xy处开始绘制））
        canvas.drawText(String.valueOf(formatTime(mSunRiseTime)), mRisePos, mHeight - mTextPaint.descent(), mTextPaint);
        canvas.drawText(String.valueOf(formatTime(mSunDownTime)), mDownPos, mHeight - mTextPaint.descent(), mTextPaint);
    }

    private void drawFillColor(Canvas canvas, float sunPosX, float sunPosY, float sweepAngle) {
        Paint colorPaint = new Paint(ANTI_ALIAS_FLAG);
        colorPaint.setColor(mColorFill);
        colorPaint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        path.moveTo(sunPosX, sunPosY);
        path.lineTo(sunPosX, mCenterY);
        path.lineTo(mRisePos, mCenterY);
        float left = mRisePos;
        float top = mSunRadius + LIGHT_SPACE + mSunLightLength;
        float right = left + 2 * getArcRadius(mWidth);
        float bottom = top + 2 * getArcRadius(mWidth);
        path.addArc(left, top, right, bottom, 180, sweepAngle);
        canvas.drawPath(path, colorPaint);
    }

    private int caculatePos(int time, int maxWidth) {
        final int pos = (int) (time * 1.0f / ONE_DAY_MINITE * maxWidth);
        return pos;
    }

    private int measureSize(int measureSpec) {

        int result = DEFULT_SIZE;
        final int mode = MeasureSpec.getMode(measureSpec);
        final int specialSize = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = specialSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specialSize, result);
                break;
        }
        return result;
    }

    private int measureHeight(int width) {
        final int height = (int) (getArcRadius(width) + mSunDotRadius + mSunRadius + LIGHT_SPACE + mSunLightLength + mTextHeight + mTextOffsetTop);
        return height;
    }

    private int getArcRadius(int width) {
        return (caculatePos(mSunDownTime, width) - caculatePos(mSunRiseTime, width)) / 2;
    }

    private String formatTime(int timeMinites) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, timeMinites / 60);
        c.set(Calendar.MINUTE, timeMinites % 60);
        return format.format(c.getTime());
    }

    /**
     * @param riseTime 单位分钟
     */
    public void setSunRiseTime(int riseTime) {
        mSunRiseTime = riseTime;
        requestLayout();//时间改变了，让控件重新测量绘制
    }

    /**
     * @param downTime 单位分钟
     */
    public void setSunDownTime(int downTime) {
        mSunDownTime = downTime;
        requestLayout();
    }

    public void setCurTime(int curTime) {
        mCurTimeMinite = curTime;
        invalidate();
    }

    public int getRiseTime() {
        return mSunRiseTime;
    }

    public void setTimeTextSize(int size) {
        mTextSize = size;
        requestLayout();
    }

    public void setCurTimeWithAnim(int targetTime) {
        if (mAnimaTask == null) {
            mAnimaTask = new SunViewAnimaTask(this, targetTime);
        }
        post(mAnimaTask);
    }

    private static class SunViewAnimaTask implements Runnable {

        private SunView mSunView;
        private ValueAnimator animator;

        SunViewAnimaTask(SunView sunView, final int targetTime) {
            mSunView = sunView;
            animator = ValueAnimator.ofInt(1, 100);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private IntEvaluator evaluator = new IntEvaluator();

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    final int evaluate = evaluator.evaluate(fraction, mSunView.getRiseTime(), targetTime);
                    mSunView.setCurTime(evaluate);
                }
            });
        }

        @Override
        public void run() {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(3000);
            animator.start();
        }
    }
}
