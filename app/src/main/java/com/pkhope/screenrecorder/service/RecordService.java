package com.pkhope.screenrecorder.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pkhope.screenrecorder.R;
import com.pkhope.screenrecorder.Recorder.GifRecorder;
import com.pkhope.screenrecorder.Recorder.VideoRecorder;
import com.pkhope.screenrecorder.activity.MainActivity;

/**
 * Created by thinkpad on 2016/1/19.
 */
public class RecordService extends Service {

    private static final String TAG = "RecordService";
    private static final int STATE_START = 0;
    private static final int STATE_STOP = 1;
    private int state = STATE_STOP;

    private static final int MENU_BROWSER = 0;
    private static final int MENU_SETTING = 1;
    private static final int MENU_EXIT = 2;

    private FrameLayout mFloatLayout;
    private TextView mStartBtn;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private DisplayMetrics mMetrics;

    private static MediaProjection mMediaProjection = null;

    private VideoRecorder mVideoRecorder;
    private GifRecorder mGifRecorder;

    private SharedPreferences mPreference;

    private String mVideoFormat;

    private BroadcastReceiver mScreenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        createFloatView();

        getWindowsParams();

        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null){
            String data = intent.getStringExtra("cmd");
            if (data != null){
                if (data.equals("start recording")){
                    startRecording();
                }
                else if (data.equals("stop recording")){
                    stopRecording();
                }
                else if (data.equals("show controlbar")){
                    mFloatLayout.setVisibility(View.VISIBLE);
                }
//                else if (data.equals("hide controlbar")){
//                    mFloatLayout.setVisibility(View.INVISIBLE);
//                }
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setUpMediaProjection(MediaProjection mp) {

        mMediaProjection = mp;

    }

//    private static void tearDownMediaProjection() {
//
//        if (mMediaProjection != null) {
//            mMediaProjection.stop();
//            mMediaProjection = null;
//        }
//
//    }

    private void createFloatView(){

        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
        mLayoutParams.width = LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = LayoutParams.WRAP_CONTENT;

        final LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (FrameLayout)inflater.inflate(R.layout.layout_record,null);
        mStartBtn = (TextView)mFloatLayout.findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordService.this, RecordService.class);
                intent.putExtra("cmd","start recording");
                startService(intent);
            }
        });

        mFloatLayout.setVisibility(View.INVISIBLE);

        mWindowManager.addView(mFloatLayout, mLayoutParams);

    }

    private void getWindowsParams(){

        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);

    }

    private void registerReceiver(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (state == STATE_START){

                    stopRecording();

                    Intent mainIntent = new Intent(RecordService.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }

            }
        };
        registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterReceiver(){
        unregisterReceiver(mScreenReceiver);
    }

    private void startRecording() {

        state = STATE_START;

        mFloatLayout.setVisibility(View.INVISIBLE);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.recording)).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setPriority(Notification.PRIORITY_MIN);
        Notification notification = builder.build();
        startForeground(1, notification);

        mVideoFormat = mPreference.getString("video_format", "MP4");

        Log.i(TAG, "Format :" + mVideoFormat);
        if (mVideoFormat.equals("MP4")) {

            Log.i(TAG, "MP4 Encorder Started");

            mVideoRecorder = new VideoRecorder(mMediaProjection,mMetrics.densityDpi);
            String resolution = mPreference.getString("resolution", "1200×720");
            String[] split = resolution.split("×");
            int h = Integer.parseInt(split[0]);
            int w = Integer.parseInt(split[1]);
            mVideoRecorder.setWidth(w);
            mVideoRecorder.setHeight(h);

            int frameRate = Integer.parseInt(mPreference.getString("frame_rate", "30"));
            mVideoRecorder.setFrameRate(frameRate);

            int bitRate = Integer.parseInt(mPreference.getString("bit_rate", "1"));
            mVideoRecorder.setBitRate(bitRate*100000);

            new Handler().postDelayed(new Runnable() {

                public void run() {

                    mVideoRecorder.start();

                }

            }, 1000);


        } else if(mVideoFormat.equals("GIF")){

            Log.i(TAG, "GIF Encorder Started");
            mGifRecorder = new GifRecorder(mMediaProjection, mMetrics.densityDpi);
            mGifRecorder.setWidth(mMetrics.widthPixels);
            mGifRecorder.setHeight(mMetrics.heightPixels);

            new Handler().postDelayed(new Runnable() {

                public void run() {

                    mGifRecorder.start();

                }

            }, 1000);

        }

    }

    private void stopRecording() {

        stopForeground(true);

        state = STATE_STOP;

        if (mVideoFormat.equals("MP4")){

            mVideoRecorder.finish();
            mVideoRecorder = null;

            Log.i(TAG, "MP4 Encorder Stoped");

        } else if(mVideoFormat.equals("GIF")){

            mGifRecorder.finish();
            mGifRecorder = null;

            Log.i(TAG, "GIF Encorder Stoped");
        }

//        tearDownMediaProjection();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFloatLayout != null){
            mWindowManager.removeView(mFloatLayout);
        }

//        if (state == STATE_START){
//            stopRecording();
//        }

        unregisterReceiver();

    }

}
