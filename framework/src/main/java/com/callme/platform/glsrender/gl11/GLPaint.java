package com.callme.platform.glsrender.gl11;

import com.callme.platform.glsrender.core.Utils;

public class GLPaint {
    private float mLineWidth = 1f;
    private int mColor = 0;

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public void setLineWidth(float width) {
        Utils.assertTrue(width >= 0);
        mLineWidth = width;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

}
