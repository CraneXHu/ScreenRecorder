package com.pkhope.screenrecorder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.avos.avoscloud.AVAnalytics;
import com.pkhope.screenrecorder.R;
import com.pkhope.screenrecorder.service.RecordService;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // 测试 SDK 是否正常工作的代码
//        AVObject testObject = new AVObject("TestObject");
//        testObject.put("words", "Hello World!");
//        testObject.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(AVException e) {
//                if (e == null) {
//                    Log.d("saved", "success!");
//                }
//            }
//        });

        Intent intent = new Intent(MainActivity.this,RecordService.class);
        intent.putExtra("command","start service");
        startService(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AVAnalytics.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        AVAnalytics.onPause(this);
    }
}
