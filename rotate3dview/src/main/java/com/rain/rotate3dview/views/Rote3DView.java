package com.rain.rotate3dview.views;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import com.rain.rotate3dview.R;

/**
 * Created by HwanJ.Choi on 2017-9-12.
 */

public class Rote3DView extends ViewGroup {
    private int mCurScreen = 2;
    // 滑动的速度
    private static final int SNAP_VELOCITY = 500;
    private VelocityTracker mVelocityTracker;
    private int mWidth;
    private Scroller mScroller;
    private Camera mCamera;
    private Matrix mMatrix;
    // 旋转的角度，可以进行修改来观察效果
    private float angle = 90;

    public Rote3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mCamera = new Camera();
        mMatrix = new Matrix();
        initScreens();
    }

    public void initScreens() {
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        for (int i = 0; i < 4; i++) {
            this.addView(new ImageView(this.getContext()), i, p);
        }
        ((ImageView) this.getChildAt(0)).setImageResource(R.drawable.phone1);
        ((ImageView) this.getChildAt(1)).setImageResource(R.drawable.phone2);
        ((ImageView) this.getChildAt(2)).setImageResource(R.drawable.phone3);
        ((ImageView) this.getChildAt(3)).setImageResource(R.drawable.phone4);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("仅支持精确尺寸");
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException("仅支持精确尺寸");
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(mCurScreen * width, 0);
    }

    private float mDownX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        //将当前的触摸事件传递给VelocityTracker对象
        mVelocityTracker.addMovement(event);
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mDownX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                int disX = (int) (mDownX - x);
                mDownX = x;
                scrollBy(disX, 0);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
                    snapToScreen(mCurScreen + 1);
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void snapToDestination() {
        final int destScreen = (getScrollX() + mWidth / 2) / mWidth;
        snapToScreen(destScreen);
    }

    public void snapToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        int scrollX = getScrollX();
        int startWidth = whichScreen * mWidth;
        if (scrollX != startWidth) {
            int delta = 0;
            int startX = 0;
            if (whichScreen > mCurScreen) {
                Log.d("chj sepre", "");
                setPre();
                delta = startWidth - scrollX;
                startX = mWidth * mCurScreen - startWidth + scrollX;
            } else if (whichScreen < mCurScreen) {
                setNext();
                startX = scrollX + mWidth;
                delta = -startX + mCurScreen * mWidth;
            } else {
                startX = scrollX;
                delta = startWidth - scrollX;
            }
            mScroller.startScroll(startX, 0, delta, 0, Math.abs(delta) * 2);
            invalidate();
        }
    }

    private void setNext() {
        int count = this.getChildCount();
        View view = getChildAt(count - 1);
        removeViewAt(count - 1);
        addView(view, 0);
    }

    private void setPre() {
        int count = this.getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, count - 1);
    }


    /*
     * 当进行View滑动时，会导致当前的View无效，该函数的作用是对View进行重新绘制 调用drawScreen函数
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawScreen(canvas, i, drawingTime);
        }
    }

    public void drawScreen(Canvas canvas, int screen, long drawingTime) {
        // 得到当前子View的宽度
        final int width = getWidth();
        final int scrollWidth = screen * width;
        final int scrollX = this.getScrollX();
        // 偏移量不足的时
        if (scrollWidth > scrollX + width || scrollWidth + width < scrollX) {
            return;
        }
        final View child = getChildAt(screen);
//        final int faceIndex = screen;
//        final float currentDegree = getScrollX() * (angle / getMeasuredWidth());
//        final float faceDegree = currentDegree - faceIndex * angle;
//        if (faceDegree > 90 || faceDegree < -90) {
//            return;
//        }
        float degree = 90 * (scrollWidth - scrollX) / width;
        if (degree > 90 || degree < -90)
            return;
        Log.d("chj degree", degree + "");
        final float centerX = (scrollWidth < scrollX) ? scrollWidth + width
                : scrollWidth;
        final float centerY = getHeight() / 2;
        final Camera camera = mCamera;
        final Matrix matrix = mMatrix;
        canvas.save();
        camera.save();
        camera.rotateY(degree);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        canvas.concat(matrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }
}
