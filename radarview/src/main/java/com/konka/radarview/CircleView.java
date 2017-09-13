package com.konka.radarview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by HwanJ.Choi on 2017-7-6.
 */

public class CircleView extends View {

    private static final int DEFAULT_SIZE = 30;

    public void setColor(int color) {
        mColorPaint.setColor(color);
        invalidate();
    }

    public void setInfo(Info mInfo) {
        this.mInfo = mInfo;
    }

    public Info getInfo() {
        return mInfo;
    }

    private Info mInfo;

    private Paint mColorPaint;

    private int mSize;
    private int mRadius;

    private Paint mBitmapPaint;

    private Bitmap mBitmap;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setColor(getResources().getColor(R.color.bg_color_blue));
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mRadius, mRadius, mRadius, mColorPaint);
        if (mBitmap != null) {
            BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapPaint.setShader(bitmapShader);
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureSize(widthMeasureSpec);
        int height = measureSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mSize = Math.min(w, h);
        mRadius = mSize / 2;
    }

    private int measureSize(int measureSpec) {

        int result = DEFAULT_SIZE;
        final int mode = MeasureSpec.getMode(measureSpec);
        final int specialSize = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(specialSize, result);
                break;
            case MeasureSpec.EXACTLY:
                result = specialSize;
                break;
        }
        return result;
    }

    public void setBitmap(int resId) {
        mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mRadius * 2, mRadius * 2, false);
        invalidate();
    }

    public void clearBitmap() {
        mBitmap = null;
        invalidate();
    }
}
