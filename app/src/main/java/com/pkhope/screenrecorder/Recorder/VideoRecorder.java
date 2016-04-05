package com.pkhope.screenrecorder.Recorder;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by thinkpad on 2016/1/27.
 */
public class VideoRecorder extends Thread {

    private static final String TAG = "VideoRecorder";
    private static final String MIME_TYPE = "video/avc";

    private String mFilePath;

    private int mWidth = 700;
    private int mHeight = 1200;
    private int mDpi;

    private int mBitRate = 2000000;
    private int mFrameRate = 30;
    private int mFrameInterval = 10;
    private int mTimeoutUs = 10000;

    private MediaCodec mMediaCodec;
    private MediaMuxer mMediaMuxer;
    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private int mVideoTrackIndex = -1;

    private MediaCodec.BufferInfo mBufferInfo;

    private AtomicBoolean mQuit;

    public VideoRecorder(MediaProjection mp, int dpi){

        mDpi = dpi;

        mMediaProjection = mp;

        mBufferInfo = new MediaCodec.BufferInfo();

        mQuit = new AtomicBoolean(false);
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

    public void setBitRate(int bitRate){
        mBitRate = bitRate;
    }

    public int getBitRate(){
        return mBitRate;
    }

    public void setFrameRate(int frameRate){
        mFrameRate = frameRate;
    }

    public int getFrameRate(){
        return mFrameRate;
    }

    public void setFilePath(String path){

        mFilePath = path;

    }

    private void createVirtualDisplay(){

        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG,
                mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mSurface, null, null);
    }

    private void prepareEncorder(){

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyMMdd-HHmmss");
            Date date = new Date();
            String fileName = df.format(date);
            String path = Environment.getExternalStorageDirectory() + "/ScreenRecorder/" + fileName + ".mp4";

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE,mBitRate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE,mFrameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,mFrameInterval);

            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mMediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mMediaCodec.createInputSurface();
            mMediaCodec.start();

            mMediaMuxer = new MediaMuxer(path,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            createVirtualDisplay();

        }catch (Exception e){

            e.printStackTrace();

        }
    }

    public void run(){

        prepareEncorder();

        while(!mQuit.get()){

//            int videoTrackIndex = -1;
            int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo,mTimeoutUs);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){

                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                mVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                mMediaMuxer.start();

            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER){

                try {
                    Thread.sleep(10);
                }catch (Exception e){
                    e.printStackTrace();
                }

            } else if (index >= 0){

                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(index);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size == 0) {
                    encodedData = null;
                } else {
                    Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size
                            + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                            + ", offset=" + mBufferInfo.offset);
                }
                if (encodedData != null) {
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMediaMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
                    Log.i(TAG, "sent " + mBufferInfo.size + " bytes to muxer...");
                }

                mMediaCodec.releaseOutputBuffer(index,false);
            }
        }

        release();
    }

    public void release(){

        if (mMediaCodec != null){
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (mVirtualDisplay != null){
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mMediaMuxer != null){

            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }

    }

    public void finish(){

        mQuit.set(true);

    }
}
