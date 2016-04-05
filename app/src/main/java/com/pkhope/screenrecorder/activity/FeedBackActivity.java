package com.pkhope.screenrecorder.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.pkhope.screenrecorder.R;

/**
 * Created by thinkpad on 2016/3/9.
 */
public class FeedBackActivity extends AppCompatActivity {

    private EditText mFeedbackEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFeedbackEt = (EditText)findViewById(R.id.et_feedback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_feedback: {
                AVObject testObject = new AVObject("Feedback");
                testObject.put("words", mFeedbackEt.getText());
                testObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            mFeedbackEt.setText(null);
                            Log.d("saved", "success!");
                        }
                    }
                });
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
