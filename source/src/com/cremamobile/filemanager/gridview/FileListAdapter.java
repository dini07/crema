package com.cremamobile.filemanager.gridview;

import java.util.List;

import com.cremamobile.filemanager.R;
import com.cremamobile.filemanager.file.FileListEntry;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {
	List<FileListEntry>	mFiles;
	Context	mContext;
	boolean mSelectMode;
	boolean mViewMode;
	
	public FileListAdapter(Context context) {
		mContext = context;
	}
	
	public FileListAdapter(Context context, List<FileListEntry> files) {
		mContext = context;
		mFiles = files;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mFiles == null) return 0;
		return mFiles.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mFiles == null) return null;
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View grid;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
		if (convertView == null) {
			grid = new View(mContext);
			grid = inflater.inflate(R.layout.grid_item, null);
		} else {
			grid = convertView;
		}

//		if(position < chacheImgList.size())
//			imageView.setImageBitmap(chacheImgList.get(position));
		
		return updateView(grid, position);
	}

	private View updateView(View view, int position) {
		FileListEntry file = (FileListEntry)getItem(position);
		
		TextView fileNameView = (TextView) view.findViewById(R.id.file_name);
        ImageView fileImageView = (ImageView) view.findViewById(R.id.file_image);
        ImageView selectedImageView = (ImageView) view.findViewById(R.id.selected_image);
        
        fileNameView.setText(file.getName());
        fileImageView.setImageDrawable(getFileImageDrawable(file));
        if (mSelectMode) {
        	selectedImageView.setVisibility(View.VISIBLE);
	        if (file.isSelected()) {
	        	//TODO.
	        	selectedImageView.setImageDrawable(null);
	        } else {
	        	selectedImageView.setImageDrawable(null);
	        }
        } else {
        	selectedImageView.setVisibility(View.GONE);
        }
		return view;
	}
	
	private Drawable getFileImageDrawable(FileListEntry file) {
		return mContext.getResources().getDrawable(R.drawable.folder_01);
	}
	
	public void setList(List<FileListEntry> files) {
		mFiles = null;
		mFiles = files;
		
		notifyDataSetChanged();
	}
	
}
