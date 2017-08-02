package com.konka.radarview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by HwanJ.Choi on 2017-7-5.
 */

public class RadarView extends View {
    //每个圆圈所占的比例
    private static float[] circleProportion = {1 / 13f, 2 / 13f, 3 / 13f, 4 / 13f, 5 / 13f, 6 / 13f};
    private Paint mPaintCircle;//画圆需要用到的paint

    private static final int DEFAULT_SIZE = 300;

    private int mWidth;
    private int mHeight;

    private Bitmap mCenterBitmap;

    private Shader mScanShader;
    private Matrix mMatrix;

    private IScanningListener mScanListener;

    private int mSpeed = 3;
    private int mCurScanAngle;
    private boolean mStartFlag;
    private int mTimes;

    private int mMaxScanCount = 15;
    private int mCurScanCount;

    private Paint centerIconPaint;

    private Bitmap mask;

    public void setScanListener(IScanningListener listener) {
        mScanListener = listener;
    }

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Log.e("chj", "radarView init");
        mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCircle.setColor(Color.WHITE);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        centerIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerIconPaint.setColor(Color.BLACK);
        mCenterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        mMatrix = new Matrix();

        post(drawScanTask);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("chj", "radarView onMeasure");
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e("chj", "radarView onSizeChanged");
        mWidth = mHeight = Math.min(w, h);
        mScanShader = new SweepGradient(mWidth / 2, mHeight / 2, Color.TRANSPARENT, Color.parseColor("#84B5CA"));
        int size = (int) (mWidth * circleProportion[0]) * 2;
        mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    }

    private int measureSize(int measureSpec) {
        int result = DEFAULT_SIZE;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, result);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawCenterIcon(canvas);
        drawScan(canvas);

        float iconWidth = mWidth * circleProportion[0] * 2; //绘制区域的大小
        canvas.drawBitmap(mCenterBitmap, null,
                new RectF(0, 0, iconWidth, iconWidth), centerIconPaint);
    }


    private void drawCircle(Canvas canvas) {
        for (Float f : circleProportion) {
            canvas.drawCircle(mWidth / 2, mWidth / 2, mWidth * f, mPaintCircle);
        }
    }

    private void drawCenterIcon(Canvas canvas) {
        canvas.save();
        PorterDuffXfermode fermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        float iconWidth = mWidth * circleProportion[0] * 2; //绘制区域的大小
        Canvas c = new Canvas(mask);//后续绘制将保存在mask里
        c.drawCircle(iconWidth / 2, iconWidth / 2, iconWidth / 2, centerIconPaint);//先画一个圆作为背景图
        centerIconPaint.setXfermode(fermode);//再设置mode
        c.drawBitmap(mCenterBitmap, null, new RectF(0, 0, iconWidth, iconWidth), centerIconPaint);//在圆上边再画前景图
        centerIconPaint.setXfermode(null);//取消
        canvas.drawBitmap(mask, (mWidth - iconWidth) / 2, (mWidth - iconWidth) / 2, null);//把绘制在mask上的效果绘制在canvas上

        /* 为何有问题！！*/
//        if (mCenterBitmap != null) {
//            mCenterBitmap = Bitmap.createScaledBitmap(mCenterBitmap,  iconWidth,  iconWidth, false);
//            BitmapShader bitmapShader = new BitmapShader(mCenterBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            centerIconPaint.setShader(bitmapShader);
//            canvas.drawCircle(mWidth / 2, mWidth / 2, iconWidth / 2, centerIconPaint);
//        }
        canvas.restore();
    }

    private void drawScan(Canvas canvas) {
        canvas.save();
        canvas.concat(mMatrix);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(mScanShader);
        float radius = mWidth * circleProportion[circleProportion.length - 2];
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, paint);
        canvas.restore();
    }

    Runnable drawScanTask = new Runnable() {

        @Override
        public void run() {
            mCurScanAngle = (mCurScanAngle + mSpeed) % 360;
            mMatrix.postRotate(mSpeed, mWidth / 2, mHeight / 2);
            invalidate();
            int maxTimes = 360 / mSpeed;

            if (mStartFlag && mTimes < maxTimes && mScanListener != null) {
                int temp = (int) Math.ceil(maxTimes * 1.0 / mMaxScanCount);//除数
                if (mCurScanCount < mMaxScanCount && mTimes % temp == 0) {
                    mScanListener.onScaning(mCurScanCount++, mCurScanAngle);
                } else if (mCurScanCount >= mMaxScanCount) {
                    mScanListener.onScanSuccess();
                    mStartFlag = false;
                }
                mTimes++;
            }
            postDelayed(this, 20);
        }
    };

    public void startScan() {
        mStartFlag = true;
    }

    public void setmMaxScanCount(int count) {
        mMaxScanCount = count;
    }

    interface IScanningListener {
        void onScanSuccess();

        void onScaning(int position, int angle);
    }
}
