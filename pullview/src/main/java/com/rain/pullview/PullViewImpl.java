package com.rain.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

/**
 * Created by HwanJ.Choi on 2017-8-22.
 */

public class PullViewImpl extends PullViewBase {

    public PullViewImpl(Context context) {
        super(context);
    }

    public PullViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setContentView(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.layout_content, this, false);
    }

    @Override
    protected boolean isTop() {
        return true;
    }
}
