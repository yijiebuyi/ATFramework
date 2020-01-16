package com.callme.platform.glsrender.anim;


import com.callme.platform.glsrender.gl11.GLCanvas;

public abstract class CanvasAnimation extends Animation {

    public abstract int getCanvasSaveFlags();
    public abstract void apply(GLCanvas canvas);
}

