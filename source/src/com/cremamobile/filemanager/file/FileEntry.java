package com.cremamobile.filemanager.file;

import java.io.File;
import java.util.Date;
import java.util.List;

public class FileEntry extends FileListEntry{
	private String ext;
	private long size = 0;
	private Date lastModified;
	
	public FileEntry(File file) {
		super(file);
		
		int pos = name.lastIndexOf( "." );
		if (pos > 0) {
			this.ext = name.substring( pos + 1 );
		}
		lastModified = new Date(this.path.lastModified());
	}
	
	public FileEntry(String name) {
		this(new File(name));
	}
	
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}

	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@Override
	public String toString() {
		return "FileListEntry[file:"+this.path
				+", name:"+this.name
				+", ext:"+this.ext
				+", size:"+this.size
				+", lastModified:"+this.lastModified;
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
	
}
