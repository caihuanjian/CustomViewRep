package com.konka.radarview;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by HwanJ.Choi on 2017-7-7.
 */

public class Utils {

    public static void scaleView(View view, float from, float to, boolean reverse) {
        PropertyValuesHolder xHolder = PropertyValuesHolder.ofFloat("scaleX", from, to);
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("scaleY", from, to);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, xHolder, yHolder);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (reverse)
            objectAnimator.reverse();
        else
            objectAnimator.start();
    }
}
