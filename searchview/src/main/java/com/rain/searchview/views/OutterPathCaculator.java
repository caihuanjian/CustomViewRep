package com.rain.searchview.views;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Created by HwanJ.Choi on 2017-9-18.
 */

public class OutterPathCaculator implements PathCaculator {

    private PathMeasure pathMeasure;
    private float mFraction = 0.6f;

    public OutterPathCaculator(PathMeasure pathMeasure) {
        this.pathMeasure = pathMeasure;
    }

    @Override
    public Path caculatPath(float t) {
        Path dst = new Path();
        final float length = pathMeasure.getLength();
        final float stop = length * t;
        final float start = stop - (0.5f - Math.abs(t - 0.5f)) * length * mFraction;
        pathMeasure.getSegment(start, stop, dst, true);
        return dst;
    }

    public void setFraction(float fraction) {
        mFraction = fraction;
    }
}
