package com.pkhope.screenrecorder.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import com.pkhope.screenrecorder.service.RecordService;

/**
 * Created by thinkpad on 2016/1/19.
 */
public class ProjectionActivity extends Activity {

    private static final String TAG = "ProjectionActivity";
    private final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            Intent intent = new Intent(ProjectionActivity.this,RecordService.class);
            if (resultCode != Activity.RESULT_OK) {
                intent.putExtra("command","cancel recording");
            }else {
                intent.putExtra("command","start recording");
                RecordService.setUpMediaProjection(mMediaProjectionManager.getMediaProjection(resultCode,data));
            }

            startService(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
