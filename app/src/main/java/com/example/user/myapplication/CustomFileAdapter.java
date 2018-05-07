package com.example.user.myapplication;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by user on 2018/3/4.
 */

public class CustomFileAdapter extends RecyclerView.Adapter<CustomFileAdapter.FileInfoViewHolder> {
    private final OnRecycleViewItemListener mListener;
    List<FileInfo> mFileInfoList;

    static class FileInfoViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView_;
        TextView mTextView_file_name;
        TextView mTextView_file_size;
        TextView mTextView_modified_time;

        public FileInfoViewHolder(View itemView) {
            super(itemView);
            mTextView_file_name = itemView.findViewById(R.id.file_name);
            mTextView_file_name.setTextColor(Color.BLACK);
            mTextView_file_size = itemView.findViewById(R.id.file_size);
            mTextView_file_size.setTextColor(Color.BLACK);
            mTextView_modified_time = itemView.findViewById(R.id.modified_time);
            mTextView_modified_time.setTextColor(Color.BLACK);
        }

        public void bind(final FileInfo fileInfo, final OnRecycleViewItemListener listener) {
            long time=fileInfo.ModifiedDate;
            SimpleDateFormat formatter = new
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String result=formatter.format(time);
            mTextView_modified_time.setText(result);
            mTextView_file_size.setText(Long.toString(fileInfo.fileSize));
            mTextView_file_name.setText(fileInfo.fileName);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.OnRecycleViewItemLongClick(itemView,fileInfo);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnRecycleViewItemClick(fileInfo);
                }
            });
        }
    }

    public CustomFileAdapter(List<FileInfo> infos, OnRecycleViewItemListener listener) {
        mFileInfoList = infos;
        mListener = listener;
    }

    @Override
    public FileInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View container = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_file_browser_item, parent, false);
        return new FileInfoViewHolder(container);

    }

    @Override
    public void onBindViewHolder(FileInfoViewHolder holder, int position) {
        FileInfo fileInfo = mFileInfoList.get(position);
        holder.bind(fileInfo, mListener);

    }

    @Override
    public int getItemCount() {
        return mFileInfoList.size();
    }
}
