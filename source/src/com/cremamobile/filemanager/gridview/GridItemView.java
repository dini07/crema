package com.cremamobile.filemanager.gridview;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GridItemView extends View {
	ImageView	fileImage;
	ImageView	selectedImage;
	TextView	fileName;
	
	boolean selectMode;
	
	public GridItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void updateView(boolean mode) {
		selectMode = mode;
		if (selectMode) {
			setSelected(false);
			selectedImage.setVisibility(View.VISIBLE);
		} else {
			selectedImage.setVisibility(View.GONE);
		}
	}
	
	public void setSelected(boolean selected) {
		if (!selectMode)
			return;

		//TODO.
		if (selected) {
		} else {
		}
	}
	
}
