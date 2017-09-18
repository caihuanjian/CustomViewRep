package com.rain.searchview.views;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Created by HwanJ.Choi on 2017-9-18.
 */

public class InnerPathCaculator implements PathCaculator {

    private PathMeasure pathMeasure;

    public InnerPathCaculator(PathMeasure pathMeasure) {
        this.pathMeasure = pathMeasure;
    }

    @Override
    public Path caculatPath(float t) {
        Path dst = new Path();
        pathMeasure.getSegment(pathMeasure.getLength() * t, pathMeasure.getLength(), dst, true);
        return dst;
    }
}
