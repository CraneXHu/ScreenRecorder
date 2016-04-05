package com.pkhope.screenrecorder;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thinkpad on 2016/1/21.
 */
public class SRApplication extends Application {

    private String mDirectory;

    @Override
    public void onCreate() {
        super.onCreate();

        AVOSCloud.initialize(this, "73JTwkwUS8uLaNT3BVDEIvzo-gzGzoHsz", "mpKP52gojCfEf4TUj3j5JfiE");
        AVAnalytics.enableCrashReport(this, true);

        createFolder();
    }

    public String createFolder(){
        mDirectory = Environment.getExternalStorageDirectory() + "/ScreenRecorder/";
        File file = new File(mDirectory);
        if(null==file || !file.exists()){

            return null;

        }
        return mDirectory;
    }

    public String getDirectory(){
        return mDirectory;
    }

}
