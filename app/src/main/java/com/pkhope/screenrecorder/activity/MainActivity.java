package com.pkhope.screenrecorder.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.pkhope.screenrecorder.R;
import com.pkhope.screenrecorder.SRApplication;
import com.pkhope.screenrecorder.adapter.FileListAdapter;
import com.pkhope.screenrecorder.service.RecordService;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mediaProjectionManager;
    private static MediaProjection mediaProjection = null;

    private RecyclerView recyclerView;
    private FileListAdapter fileListAdapter;

    private List<String> fileList;

    private long lastBackTime = 0;

    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileList = new ArrayList<>();

        setUpRecyclerView();

        floatingActionButton = (FloatingActionButton)findViewById(R.id.activity_main_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaProjection == null){
                    Toast.makeText(MainActivity.this,"No permissions ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Build.VERSION.SDK_INT >= 23){
                    askForPermission();
                } else {
                    startService();
                }

            }
        });

        mediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        fileList.clear();
        getFileList();
        fileListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            if (resultCode == Activity.RESULT_OK){

                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
                RecordService.setUpMediaProjection(mediaProjection);

                Intent intent = new Intent(MainActivity.this,RecordService.class);
                intent.putExtra("cmd","start service");
                startService(intent);

            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(MainActivity.this,RecordService.class);
        stopService(intent);

        if (mediaProjection != null){
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_setting: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis() - lastBackTime) > 2000){
                Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show();
                lastBackTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(23)
    private void askForPermission(){
        if (Settings.canDrawOverlays(this)){
            startService();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }
    }

    private void startService(){

        Intent intent = new Intent(MainActivity.this, RecordService.class);
        intent.putExtra("cmd","show controlbar");
        startService(intent);

        MainActivity.this.moveTaskToBack(true);
    }

    private void setUpRecyclerView(){

        getFileList();
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_rv);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        fileListAdapter = new FileListAdapter(this, fileList);
        recyclerView.setAdapter(fileListAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (floatingActionButton.getVisibility() == View.VISIBLE && dy > 0) {
                    floatingActionButton.hide();
                } else if (floatingActionButton.getVisibility() != View.VISIBLE && dy < 0){
                    floatingActionButton.show();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void getFileList(){

        File dir = new File(((SRApplication)getApplication()).getDirectory());
        if (dir.exists()){

            for(String file : dir.list()){
                file = ((SRApplication)getApplication()).getDirectory() + file;
                fileList.add(file);
            }

            int datePos = ((SRApplication)getApplication()).getDirectory().length();
            final int start = datePos;
            final int end = datePos + 13;
            final SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");

            Collections.sort(fileList, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {

                    String fileName1 = lhs.substring(start, end);
                    String fileName2 = rhs.substring(start,end);
                    try {

                        Date date1 = format.parse(fileName1);
                        Date date2 = format.parse(fileName2);

                        if (date1.getTime() > date2.getTime()){
                            return -1;
                        } else {
                            return 1;
                        }

                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
        }
    }

}
