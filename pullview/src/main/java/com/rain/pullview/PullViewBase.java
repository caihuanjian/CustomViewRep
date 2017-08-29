package com.rain.pullview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.rain.pullview.PullViewBase.PullState.STATUS_DRAG;
import static com.rain.pullview.PullViewBase.PullState.STATUS_REFRESHING;

/**
 * Created by HwanJ.Choi on 2017-8-22.
 */

public abstract class PullViewBase<T extends View> extends ViewGroup {

    enum PullState {
        STATUS_IDLE, STATUS_REFRESHING, STATUS_DRAG
    }

    private static final int MSG_REFRESH_CANCEL = 0;
    private static final int MSG_PULL_REFRESH = 1;
    private static final int MSG_RELEASE_REFRESH = 2;
    private static final int MSG_REFRESH_COMPLETE = 3;
    private static final int MSG_REFRESHING = 4;

    private View mHeaderView;

    protected T mContentView;

    private Scroller mScroller;

    private float mLastY;

    private int mHeaderViewHeight;

    private Handler mHandler;

    private HeaderViewHandler mHeaderViewHandler;

    public PullViewBase(Context context) {
        this(context, null);
    }

    public PullViewBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mStateManger = new StateManger();
        mHandler = new RefreshHandler();
        init(context);
    }

    private void init(Context context) {
        initHeader(context);
        setUpContentView(context);
    }

    private void initHeader(Context context) {
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_header, this, false);
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, screenHeight / 4));
        mHeaderView.setPadding(0, screenHeight / 4 - 100, 0, 0);
        mHeaderView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        addView(mHeaderView);
        mHeaderViewHandler = new HeaderViewHandler(mHeaderView);
    }

    private void setUpContentView(Context context) {
        setContentView(context);
        addView(mContentView);
    }

    protected abstract void setContentView(Context context);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("chj", "onMeasure");
        View childView;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
//            childView.measure(widthMeasureSpec,heightMeasureSpec);
        }
        final int width = measureSize(widthMeasureSpec, true);
        final int height = measureSize(heightMeasureSpec, false);
        Log.d("chj", "==height:" + height);
        setMeasuredDimension(width, height);
    }

    private int measureSize(int measureSpec, boolean measureWidth) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                View childView;
                for (int i = 0; i < getChildCount(); i++) {
                    childView = getChildAt(i);
                    int childWidth = childView.getMeasuredWidth();
                    int childHeight = childView.getMeasuredHeight();
                    if (measureWidth) {
                        result = result < childWidth ? childWidth : result;
                    } else {
                        result += childHeight;
                    }
                }
                break;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("chj", "onLayout");
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        int count = getChildCount();
        int top = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(0, top, child.getMeasuredWidth(), top + child.getMeasuredHeight());
            top += child.getMeasuredHeight();
        }
        if (mStateManger.getCurState() == PullState.STATUS_IDLE)
            scrollTo(getScrollX(), mHeaderViewHeight);
    }

    protected abstract boolean isTop();

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Do not intercept touch event, let the child handle it
            return false;
        }

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // int yDistance = (int) ev.getRawY() - mYDown;
                int yDistance = (int) (ev.getRawY() - mLastY);
                // 如果拉到了顶部, 并且是下拉,并且不是正在刷新,则拦截触摸事件,从而转到onTouchEvent来处理下拉刷新事件
                if ((isTop() && yDistance > 0 && mStateManger.getCurState() != STATUS_REFRESHING)) {
                    return true;
                }
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mStateManger.getCurState() == STATUS_REFRESHING)
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float curY = event.getRawY();
                final float distance = curY - mLastY;
                changeScrollY(distance);
                mLastY = curY;
                break;
            case MotionEvent.ACTION_UP:
                doRefresh();
                if (mRefreshListener != null && mStateManger.getCurState() == STATUS_REFRESHING) {
                    mRefreshListener.onRefresh();
                    final Message message = mHandler.obtainMessage(MSG_REFRESHING);
                    message.obj = mHeaderViewHandler;
                    message.sendToTarget();
                }
                break;
        }
        return true;
    }

    private void doRefresh() {
        if (mStateManger.getCurState() != STATUS_DRAG) {
            return;
        }
        final int scrollY = getScrollY();
        if (mCanRefresh) {
            mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), mHeaderView.getPaddingTop() - scrollY);
            mStateManger.setCurState(STATUS_REFRESHING);
        } else {
            mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), mHeaderViewHeight - scrollY);
            mStateManger.setCurState(PullState.STATUS_IDLE);
            final Message message = mHandler.obtainMessage(MSG_REFRESH_CANCEL);
            message.obj = mHeaderViewHandler;
            message.sendToTarget();
        }
        invalidate();
    }

    private boolean mCanRefresh;

    private void changeScrollY(float distance) {
        final int scrollY = getScrollY();
        if (distance > 0 && scrollY - distance > getPaddingTop()) {
            scrollBy(0, (int) -distance);
            mStateManger.setCurState(PullState.STATUS_DRAG);
        } else if (distance < 0 && scrollY - distance <= mHeaderViewHeight) {
            scrollBy(0, (int) -distance);
            mStateManger.setCurState(PullState.STATUS_DRAG);
        }

        float slop = mHeaderViewHeight / 2;
        if (scrollY > slop) {//还在下拉状态
            if (mCanRefresh) {
                final Message message = mHandler.obtainMessage();
                message.obj = mHeaderViewHandler;
                message.what = MSG_PULL_REFRESH;
                message.sendToTarget();
            }
            mCanRefresh = false;
        } else if (scrollY <= slop) {//可以释放状态
            if (!mCanRefresh) {
                final Message message = mHandler.obtainMessage();
                message.obj = mHeaderViewHandler;
                message.what = MSG_RELEASE_REFRESH;
                message.sendToTarget();
            }
            mCanRefresh = true;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void refreshComplete() {
        mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), mHeaderViewHeight - getScrollY());
        mStateManger.setCurState(PullState.STATUS_IDLE);
        postComplete();
        invalidate();
    }

    private void postComplete() {
        mCanRefresh = false;
        final Message message = mHandler.obtainMessage(MSG_REFRESH_COMPLETE);
        message.obj = mHeaderViewHandler;
        mHandler.sendMessageDelayed(message, 200);
    }

    private StateManger mStateManger;

    private OnRefreshListener mRefreshListener;

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    public void addOnStateChageListener(OnStateChangeListener listener) {
        mStateManger.addOnStateChageListener(listener);
    }

    interface OnRefreshListener {
        void onRefresh();
    }

    interface OnStateChangeListener {
        void onStateChange(PullState oldState, PullState newState);
    }

    private static class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            HeaderViewHandler headerViewHandler;
            switch (msg.what) {
                case MSG_REFRESH_CANCEL:
                    headerViewHandler = (HeaderViewHandler) msg.obj;
                    headerViewHandler.updateViewStatus(HeaderViewHandler.CANCLE);
                    break;
                case MSG_PULL_REFRESH:
                    headerViewHandler = (HeaderViewHandler) msg.obj;
                    headerViewHandler.updateViewStatus(HeaderViewHandler.PULL_REFRESH);
                    break;
                case MSG_RELEASE_REFRESH:
                    headerViewHandler = (HeaderViewHandler) msg.obj;
                    headerViewHandler.updateViewStatus(HeaderViewHandler.RELEASE_REFRESH);
                    break;
                case MSG_REFRESH_COMPLETE:
                    headerViewHandler = (HeaderViewHandler) msg.obj;
                    headerViewHandler.updateViewStatus(HeaderViewHandler.COMPLETE);
                    break;
                case MSG_REFRESHING:
                    headerViewHandler = (HeaderViewHandler) msg.obj;
                    headerViewHandler.updateViewStatus(HeaderViewHandler.REFRESHING);
                    break;
                default:
                    break;
            }
        }
    }

    static class HeaderViewHandler {

        private static final int CANCLE = 0;

        private static final int RELEASE_REFRESH = 1;
        private static final int PULL_REFRESH = 2;
        private static final int COMPLETE = 3;
        private static final int REFRESHING = 4;

        private ValueAnimator mAnimator;

        private View arrow;

        private TextView tip;

        private ProgressBar progressBar;

        public HeaderViewHandler(View header) {
            arrow = header.findViewById(R.id.iv_arrow);
            tip = (TextView) header.findViewById(R.id.tv_tip);
            progressBar = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress);
            mAnimator = ObjectAnimator.ofFloat(arrow, "rotation", 0, -180);
            mAnimator.setDuration(150);
        }

        public void updateViewStatus(int what) {
            switch (what) {
                case CANCLE:
                    tip.setText(tip.getResources().getString(R.string.pull_to_refresh_pull_label));
                    break;
                case RELEASE_REFRESH:
                    mAnimator.start();
                    tip.setText(tip.getResources().getString(R.string.pull_to_refresh_release_label));
                    break;
                case PULL_REFRESH:
                    if (!(arrow.getRotation() == 0)) {
                        mAnimator.reverse();
                    }
                    tip.setText(tip.getResources().getString(R.string.pull_to_refresh_pull_label));
                    break;
                case COMPLETE:
                    refreshComplete();
                    break;
                case REFRESHING:
                    refreshing();
                    break;
            }
        }

        private void refreshing() {
            tip.setText(tip.getResources().getString(R.string.pull_to_refresh_refreshing_label));
            progressBar.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.INVISIBLE);
            mAnimator.reverse();
        }

        private void refreshComplete() {
            tip.setText(tip.getResources().getString(R.string.pull_to_refresh_pull_label));
            progressBar.setVisibility(View.GONE);
            arrow.setVisibility(View.VISIBLE);
        }
    }

    static class StateManger {

        private PullState mCurState = PullState.STATUS_IDLE;

        private PullState mOldState;

        private List<OnStateChangeListener> mStateListeners = new ArrayList<>();

        public void setCurState(PullState state) {
            mOldState = mCurState;
            mCurState = state;
            notifyStateChange();
        }

        public synchronized void addOnStateChageListener(OnStateChangeListener listener) {
            if (!mStateListeners.contains(listener)) {
                mStateListeners.add(mStateListeners.size(), listener);
            }
        }

        private void notifyStateChange() {
            for (OnStateChangeListener listener : mStateListeners) {
                listener.onStateChange(mOldState, mCurState);
            }
        }

        public PullState getCurState() {
            return mCurState;
        }
    }
}