package com.cremamobile.filemanager.file;

import java.io.File;
import java.util.Date;
import java.util.List;

public class DirFileEntry extends FileListEntry {
	private int childFileNumber;
	private List<FileListEntry> child;
	private boolean childIsExistButNotInsertChild;
	private Date lastModified;
	private long size;
	
	public DirFileEntry(File file) {
		super(file);
	}

	@Override
	public boolean isDir() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDevice() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getChildFileNumber() {
		return childFileNumber;
	}
	public List<FileListEntry> getChildLists() {
		return this.child;
	}
	public void setChildLists(List<FileListEntry> list) {
		this.childIsExistButNotInsertChild = false;
		this.child = list;
		this.childFileNumber = this.child != null ? this.child.size() : 0;
	}

	public void setChildNumber(int number) {
		this.childFileNumber = number;
		this.childIsExistButNotInsertChild = false;
	}
	
	public void setChildIsExistButNotInsertChild(boolean search) {
		this.childIsExistButNotInsertChild	 = search;
	}
	
	public boolean isNeedSearchChild() {
		return this.childIsExistButNotInsertChild;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

}
