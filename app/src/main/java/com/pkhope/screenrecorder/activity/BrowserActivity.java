package com.pkhope.screenrecorder.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pkhope.screenrecorder.R;
import com.pkhope.screenrecorder.SRApplication;
import com.pkhope.screenrecorder.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by thinkpad on 2016/2/20.
 */
public class BrowserActivity extends BaseActivity {

    private static final String TAG = "BrowserActivity";
    private ListView mListView = null;
    private ArrayAdapter<String> mMyArrayAdapter = null;
    private ArrayList<String> mList = null;

    private boolean mIsStartActivity = false;
    private boolean mIsListViewIdle = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browser);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        mList = getFileList();
        mListView = (ListView)findViewById(R.id.file_list);
        mMyArrayAdapter = new MyListAdapter(this,R.layout.list_item);
        mListView.setAdapter(mMyArrayAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mIsStartActivity = true;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(mList.get(position)));
                if (mList.get(position).endsWith(".mp4")) {
                    intent.setDataAndType(uri, "video/*");
                } else {
                    intent.setDataAndType(uri, "image/*");
                }
                startActivity(intent);

            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    mIsListViewIdle = true;
                    mMyArrayAdapter.notifyDataSetChanged();
                } else {
                    mIsListViewIdle = false;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0,0,0,getString(R.string.menu_del));
            }

        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if (item.getItemId() == 0){
            File file = new File(mList.get(menuInfo.position));
            file.delete();
            mList.remove(menuInfo.position);
            mMyArrayAdapter.notifyDataSetChanged();
        }
        return super.onContextItemSelected(item);
    }

    ArrayList<String> getFileList(){

        ArrayList<String> list = new ArrayList<String>();
        File dir = new File(((SRApplication)getApplication()).getDirectory());
        if (dir.exists()){
            for(String file : dir.list()){
                file = ((SRApplication)getApplication()).getDirectory() + file;
                list.add(file);
            }
        }
        return list;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!mIsStartActivity){

            finish();
        }
        mIsStartActivity = false;
    }

    class MyListAdapter extends ArrayAdapter<String>{

        private int resId;
        private LruCache<String,Bitmap> mBitmapCache;

        MyListAdapter(Context context, int resId){
            super(context,resId);
            this.resId = resId;

            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            mBitmapCache = new LruCache<String, Bitmap>(cacheSize){
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            if(bitmap == null){
                return;
            }
            if (getBitmapFromMemCache(key) == null) {

                mBitmapCache.put(key, bitmap);
            }
        }

        public Bitmap getBitmapFromMemCache(String key) {
            return mBitmapCache.get(key);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String path = getItem(position);
            View view;
            ViewHolder viewHolder;
            if (convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resId,null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)view.findViewById(R.id.image);
                viewHolder.fileNameTv = (TextView)view.findViewById(R.id.file_name);
                viewHolder.fileSizeTv = (TextView)view.findViewById(R.id.file_size);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder)view.getTag();
            }

            Bitmap bitmap = null;

            File file = new File(path);
            float fileSize = file.length()/1024.0f/1024.0f;

            viewHolder.fileNameTv.setText(file.getName());
            viewHolder.fileSizeTv.setText( new java.text.DecimalFormat("#.000").format(fileSize) + "MB" );

            bitmap = getBitmapFromMemCache(path);
            if (bitmap == null && mIsListViewIdle){
                float scale = getResources().getDisplayMetrics().density;
                int size =  (int) (72 * scale + 0.5f);
                if (path.toLowerCase().endsWith(".mp4")){

                    bitmap = ImageUtils.getVideoThumbnail(path, size, size, MediaStore.Images.Thumbnails.MICRO_KIND);

                }else{

                    bitmap = ImageUtils.getImageThumbnail(path,size,size);

                }
                addBitmapToMemoryCache(path,bitmap);
                new ThumbnailTask().execute(path,viewHolder.imageView);
            }
            else{

                viewHolder.imageView.setImageBitmap(bitmap);
            }

//            viewHolder.imageView.setImageBitmap(bitmap);

            return view;
        }

        class ViewHolder {

            public ImageView imageView;
            public TextView fileNameTv;
            public TextView fileSizeTv;

        }

        class ThumbnailTask extends AsyncTask<Object,Integer,Bitmap>{

            private String mPath;
            private ImageView mImageView;

            @Override
            protected Bitmap doInBackground(Object... params) {
                mPath = (String)params[0];
                mImageView = (ImageView)params[1];

                Bitmap bitmap = null;
                float scale = getResources().getDisplayMetrics().density;
                int size =  (int) (72 * scale + 0.5f);
                if (mPath.toLowerCase().endsWith(".mp4")){

                    bitmap = ImageUtils.getVideoThumbnail(mPath,size,size, MediaStore.Images.Thumbnails.MICRO_KIND);

                }else{

                    bitmap = ImageUtils.getImageThumbnail(mPath,size,size);

                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                mImageView.setImageBitmap(bitmap);

                addBitmapToMemoryCache(mPath,bitmap);
            }
        }


    }


}
