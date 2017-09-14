package com.rain.scrollimage.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.rain.scrollimage.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HwanJ.Choi on 2017-9-13.
 */

public class ScrollImageView extends View {

    private List<Bitmap> mBitmaps = new ArrayList<>();
    private int maxHeight;

    private int mDrawOffset;

    private int speed = 10;

    private Rect clipBounds = new Rect();

    private int mStartIndex = 0;

    public ScrollImageView(Context context) {
        this(context, null);
    }

    public ScrollImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    private void init() {
        for (Bitmap bitmap : mBitmaps) {
            maxHeight = Math.max(maxHeight, bitmap.getHeight());
        }
    }

    private void initAttr(AttributeSet attrs) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScrollImageView, 0, 0);
        speed = (int) typedArray.getDimension(R.styleable.ScrollImageView_speed, 5);
//        mBitmaps.add(typedArray.getResourceId(R.styleable.))
        final int srcId = typedArray.getResourceId(R.styleable.ScrollImageView_src, 0);
        if (srcId != 0) {
            int type = isInEditMode() ? TypedValue.TYPE_STRING : typedArray.peekValue(R.styleable.ScrollImageView_src).type;
            if (type == TypedValue.TYPE_REFERENCE) {
                final TypedArray srcArray = getResources().obtainTypedArray(srcId);
                for (int i = 0; i < srcArray.length(); i++) {
                    final int id = srcArray.getResourceId(i, 0);
                    mBitmaps.add(BitmapFactory.decodeResource(getResources(), id));
                }
                srcArray.recycle();
            } else if (type == TypedValue.TYPE_STRING) {
                mBitmaps.add(BitmapFactory.decodeResource(getResources(), srcId));
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), maxHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmaps.size() == 0) {
            return;
        }
        canvas.getClipBounds(clipBounds);
        //如果offset移到屏幕之外，offset--》|bitmap屏幕外|        屏幕         |
        //则把offset加上第一个bitmap的宽度
        if (mDrawOffset <= -getNextDrawBitmap(mStartIndex).getWidth()) {
            mDrawOffset += getNextDrawBitmap(mStartIndex).getWidth();
            mStartIndex = ++mStartIndex % mBitmaps.size();
        }

        int left = mDrawOffset;
        for (int i = 0; left < clipBounds.width(); i++) {
            final Bitmap drawBitmap = getNextDrawBitmap((i + mStartIndex) % mBitmaps.size());
            canvas.drawBitmap(drawBitmap, getDrawingLeft(left, drawBitmap.getWidth()), 0, null);
            left += drawBitmap.getWidth();
        }

        if (speed != 0) {
            mDrawOffset -= Math.abs(speed);
            postInvalidateOnAnimation();
        }
    }

    private Bitmap getNextDrawBitmap(int index) {
        return mBitmaps.get(index);
    }

    private int getDrawingLeft(int offset, int curBitmapWidth) {
        if (speed > 0) {
            return offset;
        } else {
            return clipBounds.width() - curBitmapWidth - offset;
        }
    }
}
