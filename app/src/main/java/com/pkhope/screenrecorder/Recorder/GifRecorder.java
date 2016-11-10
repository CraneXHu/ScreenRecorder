package com.pkhope.screenrecorder.Recorder;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.pkhope.screenrecorder.gifencoder.AnimatedGifEncoder;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thinkpad on 2016/1/27.
 */
public class GifRecorder {

    private static final String TAG = "GifRecorder";

    private int mWidth = 100;
    private int mHeight = 100;
    private int mDpi;

    private String mFilePath;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private AnimatedGifEncoder mGifEncoder;
    private NativeGifEncorder mNativeGifEncorder;
    private ImageReader mImageReader = null;

    private Handler backgroundHandler;
    private HandlerThread thread;

    private boolean canFinished;


    public GifRecorder(MediaProjection mp, int dpi){

        mMediaProjection = mp;
        mDpi = dpi;
        mGifEncoder = new AnimatedGifEncoder();
        mNativeGifEncorder = new NativeGifEncorder();
        mGifEncoder.setDelay(300);

    }

    public void setWidth(int width){

        mWidth = width;

    }

    public int getWidth(){

        return mWidth;

    }

    public void setHeight(int height){

        mHeight = height;

    }

    public int getHeight(){

        return mHeight;

    }

    public void setFilePath(String path){

        mFilePath = path;

    }

    private void createVirtualDisplay(){

        if (mImageReader == null){

            mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        }

        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG,
                mWidth, mHeight, mDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

    }

    public void start(){

        createVirtualDisplay();

        SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
        Date date = new Date();
        String fileName = format.format(date);
        String path = Environment.getExternalStorageDirectory() + "/ScreenRecorder/" + fileName + ".gif";

        try {

//            OutputStream out = new FileOutputStream(path);
//            mGifEncoder.start(out);

            boolean res = mNativeGifEncorder.open(path,mWidth,mHeight,30);
            if (res == false){
                Log.i(TAG, "Open file failed");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        thread = new HandlerThread("GIFRecorderHandler");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                canFinished = false;

                Bitmap bitmap = getScreenBitmap(reader);

                if(bitmap == null){
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:SS");
                Date date = new Date();
                String time = format.format(date);
                Log.i(TAG, "Start : " + time);

//                mGifEncoder.addFrame(bitmap);
                mNativeGifEncorder.addFrame(bitmap);

                date = new Date();
                time = format.format(date);
                Log.i(TAG, "End : " + time);

                bitmap.recycle();

                canFinished = true;

            }
        }, backgroundHandler);

    }

    private Bitmap getScreenBitmap(ImageReader reader){

        Image image = reader.acquireLatestImage();
        if(image == null){
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
//
//        Log.i(TAG,"Width = " + bitmap.getWidth() + "  Height = " + bitmap.getHeight());
//        Matrix matrix = new Matrix();
//        matrix.postScale(0.5f,0.5f);
//        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//        bitmap.recycle();
//
//        Log.i(TAG, "Resize Width = " + resizeBitmap.getWidth() + "  Resize Height = " + resizeBitmap.getHeight());
//        return resizeBitmap;
        return bitmap;
    }

    public void finish(){

        if (mVirtualDisplay != null){

            mVirtualDisplay.release();
            mVirtualDisplay = null;

        }

        mImageReader.close();
        mImageReader = null;

        while (true){

            if (canFinished){

//                mGifEncoder.finish();

                mNativeGifEncorder.save();
                return;
            }

        }

    }
}
