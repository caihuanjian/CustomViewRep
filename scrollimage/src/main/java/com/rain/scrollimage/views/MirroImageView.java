package com.rain.scrollimage.views;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by HwanJ.Choi on 2017-9-13.
 */

public class MirroImageView extends ImageView {

    private Camera mCamera;

    public MirroImageView(Context context) {
        this(context, null);
    }

    public MirroImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MirroImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCamera = new Camera();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Matrix imageMatrix = getImageMatrix();
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (w == 0 || h == 0) {
            return;
        }
        mCamera.save();
        mCamera.rotateY(180);
        mCamera.getMatrix(imageMatrix);
        mCamera.restore();
        imageMatrix.preTranslate(-w / 2, -h / 2);
        imageMatrix.postTranslate(w / 2, h / 2);
        canvas.concat(imageMatrix);
        super.onDraw(canvas);
    }
}
