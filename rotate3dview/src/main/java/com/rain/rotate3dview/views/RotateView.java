package com.rain.rotate3dview.views;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import static com.rain.rotate3dview.views.RotateView.State.Nomal;
import static com.rain.rotate3dview.views.RotateView.State.ToNext;
import static com.rain.rotate3dview.views.RotateView.State.ToPre;

/**
 * Created by HwanJ.Choi on 2017-9-12.
 */

public class RotateView extends ViewGroup {

    private Scroller mScroller;
    private int mWidth;
    private int mHeight;

    private float mDownX;
    private float mDownY;

    private int mTouchSlop;

    private static final int SNAP_VELOCITY = 500;

    private VelocityTracker mVelocityTracker;

    private int mCurScreen = 1;
    private Camera mCamera;
    private Matrix mMatrix;


    public RotateView(Context context) {
        this(context, null);
    }

    public RotateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mScroller = new Scroller(getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mCamera = new Camera();
        mMatrix = new Matrix();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        scrollTo(mCurScreen * mWidth, 0);
    }

    private int measureWidth(int measureSpec) {
        int speciaSize = MeasureSpec.getSize(measureSpec);
        int speciaMode = MeasureSpec.getMode(measureSpec);
        int result = 0;
        int childCount = getChildCount();
        switch (speciaMode) {
            case MeasureSpec.EXACTLY:
                result = speciaSize;
                break;
            case MeasureSpec.AT_MOST:
                int width = 0;
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    final int childWidth = child.getMeasuredWidth();
                    if (width < childWidth) {
                        width = childWidth;
                    }
                }
                result = Math.min(speciaSize, width);
                break;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int speciaSize = MeasureSpec.getSize(measureSpec);
        int speciaMode = MeasureSpec.getMode(measureSpec);
        int result = 0;
        int childCount = getChildCount();
        switch (speciaMode) {
            case MeasureSpec.EXACTLY:
                result = speciaSize;
                break;
            case MeasureSpec.AT_MOST:
                int height = 0;
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    final int childHeight = child.getMeasuredHeight();
                    if (height < childHeight) {
                        height = childHeight;
                    }
                }
                result = Math.min(speciaSize, height);
                break;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int top = 0;
        int left = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            final int measuredWidth = child.getMeasuredWidth();
            final int measuredHeight = child.getMeasuredHeight();
            child.layout(left, top, left + measuredWidth, top + measuredHeight);
            left += measuredWidth;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mDownX = event.getX();
                mDownY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mDownX - event.getX();
                scroll(deltaX);
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY) {//右滑
                    snapToScreen(ToPre);
                } else if (velocityX < -SNAP_VELOCITY) {//左滑
                    snapToScreen(ToNext);
                } else {
                    snapToScreen(Nomal);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawScreen(canvas, i, drawingTime);
        }
    }

    private void drawScreen(Canvas canvas, int i, long drawingTime) {
        final View child = getChildAt(i);
        final int childWidth = child.getMeasuredWidth();
        int startLeft = i * mWidth;
        int scrollX = getScrollX();
        if (startLeft + childWidth < scrollX || scrollX + childWidth < startLeft) {
            return;
        }
        int centerX = scrollX > startLeft ? startLeft + childWidth : startLeft;
        int centerY = getHeight() / 2;

        float degree = 90 * (startLeft - scrollX) / getWidth();
        if (degree > 90 || degree < -90)
            return;
        Camera camera = mCamera;
        Matrix matrix = mMatrix;
        camera.save();
        camera.rotateY(degree);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        drawChildInner(child, canvas, matrix, drawingTime);
    }

    private void drawChildInner(View child, Canvas canvas, Matrix matrix, long drawingTime) {
        canvas.save();
        canvas.concat(matrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }

    enum State {
        ToPre, ToNext, Nomal
    }

    private void snapToScreen(State state) {
        switch (state) {
            case ToPre:
//                snapToScreen(mCurScreen - 1);

                if (mCurScreen > 0) {
                    final View view = getChildAt(mCurScreen - 1);
                    int preLeft = view.getLeft();
                    final int distance = getScrollX() - preLeft;
                    if (distance < mWidth / 2) {
                        mCurScreen--;
                        mScroller.startScroll(getScrollX(), getScrollY(), -distance, getScrollY());
                    } else {
                        mScroller.startScroll(getScrollX(), getScrollY(), mWidth - distance, getScrollY());
                    }
                }
                break;
            case ToNext:
//                snapToScreen(mCurScreen + 1);
                if (mCurScreen < getChildCount() - 1) {
                    View view = getChildAt(mCurScreen);
                    int width = view.getWidth();
                    final int right = view.getRight();
                    int distance = right - getScrollX();
                    if (distance < mWidth / 2) {
                        mCurScreen++;
                        mScroller.startScroll(getScrollX(), getScrollY(), distance, getScrollY());
                    } else {
                        mScroller.startScroll(getScrollX(), getScrollY(), -(width - distance), getScrollY());
                    }
                }
                break;
            case Nomal:
                snapToDestination();
                break;
        }
        invalidate();
    }

    private void snapToDestination() {
        final View curView = getChildAt(mCurScreen);
        final int i = (int) (getScrollX() * 1.0 / mWidth + 0.5);
        final View nextView = getChildAt(i);
        final int endX = nextView.getLeft();
        if (curView.getLeft() != endX) {
            if (curView.getLeft() > endX && mCurScreen > 0)
                mCurScreen--;
            else if (curView.getLeft() < endX && mCurScreen < getChildCount() - 1) mCurScreen++;
        }
        mScroller.startScroll(getScrollX(), getScrollY(), endX - getScrollX(), getScrollY());
    }

    private void scroll(float distance) {
        scrollBy((int) distance, 0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

//    public void snapToDestination() {
//        final int destScreen = (getScrollX() + mWidth / 2) / mWidth;
//        snapToScreen(destScreen);
//    }

    public void snapToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        int scrollX = getScrollX();
        int left = whichScreen * mWidth;//左边坐标
        if (scrollX != left) {
            int delta;
            int startX;
            if (whichScreen > mCurScreen) {//下一屏幕
                addNext();//把第一个子view添加到最后
                startX = scrollX - mWidth;//相应减掉一个子view的宽度
                delta = mCurScreen * mWidth - startX;//末位置减起始位置
            } else if (whichScreen < mCurScreen) {
                addPre();
                startX = scrollX + mWidth;//前面添加了一个子view，相应加上一个宽度
                delta = mWidth * mCurScreen - startX;
            } else {
                startX = scrollX;
                delta = left - startX;
            }
            mScroller.startScroll(startX, 0, delta, 0, Math.abs(delta) * 2);
            invalidate();
        }
    }

    private void addPre() {
        int count = this.getChildCount();
        View view = getChildAt(count - 1);
        removeViewAt(count - 1);
        addView(view, 0);
    }

    private void addNext() {
        int count = this.getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, count - 1);
    }
}
