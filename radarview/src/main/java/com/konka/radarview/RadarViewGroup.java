package com.konka.radarview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by HwanJ.Choi on 2017-7-6.
 */

public class RadarViewGroup extends ViewGroup implements RadarView.IScanningListener {

    private SparseArray<Info> mDatas = new SparseArray<>();//数据源

    private SparseIntArray mAngleList = new SparseIntArray();
    private float mMaxDistance;

    private int mWidth;
    private int mHeight;

    private CircleView mCurShowView;

    public RadarViewGroup(Context context) {
        this(context, null);
    }

    public RadarViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("chj", "viewgroup onMeasure");
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        final RadarView radarView = (RadarView) findViewById(R.id.radar_view);
        radarView.setScanListener(this);
//        radarView.setmMaxScanCount(mDatas.size());
        radarView.startScan();
        Log.e("chj", "viewgroup startScan");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = mHeight = Math.min(w, h);
    }

    private int measureSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 300;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("chj", "viewgroup onLayout");
        final View radarView = findViewById(R.id.radar_view);
        radarView.layout(0, 0, radarView.getMeasuredWidth(), radarView.getMeasuredHeight());
        for (int i = 1; i < getChildCount(); i++) {
            CircleView circleView = (CircleView) getChildAt(i);
            final int angle = mAngleList.get(i - 1);
            if (angle == 0) {
                continue;
            }
            float distance = circleView.getInfo().getDistance();
            float fraction = distance / mMaxDistance;
            float x = (float) Math.cos(Math.toRadians(angle)) * (fraction + 0.6f) * 0.52f * mWidth / 2;
            float y = (float) Math.sin(Math.toRadians(angle)) * (fraction + 0.6f) * 0.52f * mHeight / 2;
            circleView.layout((int) x + mWidth / 2, (int) y + mWidth / 2, (int) x + mWidth / 2 + circleView.getMeasuredWidth(), (int) y + mWidth / 2 + circleView.getMeasuredHeight());
        }
    }

    public void setUpData(SparseArray<Info> datas) {
        CircleView circleView;
        for (int i = 0; i < datas.size(); i++) {
            Info data = datas.get(i);
            if (data.getDistance() > mMaxDistance) {
                mMaxDistance = data.getDistance();
            }
            circleView = new CircleView(getContext());
            circleView.setInfo(data);
            if (data.getSex()) {
                circleView.setColor(getResources().getColor(R.color.bg_color_pink));
            } else {
                circleView.setColor(getResources().getColor(R.color.bg_color_blue));
            }
            circleView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == mCurShowView) {
                        return;
                    }
                    if (mCurShowView == null) {
                        mCurShowView = (CircleView) v;
                    }
                    mCurShowView.clearBitmap();
                    Utils.scaleView(mCurShowView, 1.0f, 1.8f, true);
                    mCurShowView = (CircleView) v;
                    mCurShowView.setBitmap(mCurShowView.getInfo().getPortraitId());
                    Utils.scaleView(v, 1.0f, 1.8f, false);
                }
            });
            addView(circleView);
        }


    }

    @Override
    public void onScanSuccess() {
        Log.e("onScanSuccess", "onsuccess");
    }

    @Override
    public void onScaning(int position, int angle) {
        Log.e("onScaning", "position:" + position + ",angle:" + angle);
        mAngleList.put(position, angle);
        requestLayout();
    }
}
