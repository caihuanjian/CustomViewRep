package com.rain.searchview.views;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Created by HwanJ.Choi on 2017-9-18.
 */

public class EndingPathCaculator implements PathCaculator {

    private PathMeasure pathMeasure;

    EndingPathCaculator(PathMeasure pathMeasure) {
        this.pathMeasure = pathMeasure;
    }

    @Override
    public Path caculatPath(float t) {
        Path dst = new Path();
        float stop = pathMeasure.getLength();
        float start = pathMeasure.getLength() * (1 - t);
        pathMeasure.getSegment(start, stop, dst, true);
        return dst;
    }
}
