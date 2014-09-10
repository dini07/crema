package com.cremamobile.filemanager.file;

import java.io.File;
import java.util.Date;

public class ImageFileEntry extends FileListEntry {

	public ImageFileEntry(File file) {
		super(file);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isDir() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDevice() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getLastModified() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
