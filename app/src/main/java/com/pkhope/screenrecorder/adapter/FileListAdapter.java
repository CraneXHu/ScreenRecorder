package com.pkhope.screenrecorder.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pkhope.screenrecorder.R;

import java.io.File;
import java.util.List;


/**
 * Created by pkhope on 2016/10/10.
 */

public class FileListAdapter extends Adapter<FileListAdapter.FileListViewHolder> {

    private List<String> fileList;
    private Context context;

    public FileListAdapter(Context context, List<String> fileList){
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_item,parent,false);
        return new FileListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileListViewHolder holder, int position) {

        String filePath = fileList.get(position);
        holder.filePath = filePath;
        File file = new File(filePath);
        holder.fileName.setText(file.getName());
//        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//        holder.preview.setImageBitmap(bitmap);
        Glide.with(context)
                .load(filePath)
                .into(holder.preview);

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileListViewHolder extends RecyclerView.ViewHolder{

        ImageView preview;
        TextView fileName;
        ImageView more;

        String filePath;

        public FileListViewHolder(View itemView) {
            super(itemView);

            preview = (ImageView) itemView.findViewById(R.id.rv_preview);
            fileName = (TextView) itemView.findViewById(R.id.rv_file_name);
            more = (ImageView) itemView.findViewById(R.id.rv_more);

            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = Uri.fromFile(new File(filePath));
                    if (filePath.endsWith(".gif")){
                        intent.setDataAndType(uri, "image/*");
                    } else {
                        intent.setDataAndType(uri, "video/*");
                    }
                    context.startActivity(intent);
                }
            });

            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    Menu menu = popupMenu.getMenu();

                    MenuInflater menuInflater = ((Activity)context).getMenuInflater();
                    menuInflater.inflate(R.menu.menu_popup, menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.share:
                                    Uri imageUri = Uri.fromFile(new File(filePath));
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_STREAM,imageUri);
                                    intent.setType("video/*;image/*");
                                    context.startActivity(Intent.createChooser(intent,"Share"));
                                    break;
                                case R.id.delete:
                                    File file = new File(filePath);
                                    file.delete();
                                    fileList.remove(filePath);
                                    notifyDataSetChanged();
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }
}
