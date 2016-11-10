package com.pkhope.screenrecorder.Recorder;

import android.graphics.Bitmap;

/**
 * Created by thinkpad on 2016/3/28.
 */
public class NativeGifEncorder {

    static {
        System.loadLibrary("GifEncorder");
    }

    public native void test();

    public native boolean open(String fileName,int width,int height,int delay);

    public native boolean addFrame(Bitmap bmp);

    public native boolean save();
}
