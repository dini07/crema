package com.cremamobile.filemanager.file;

import java.io.File;
import java.util.Date;
import java.util.List;

public abstract class FileListEntry {
	protected File path;
	protected String absolutePath;
	protected String name;
	protected boolean hidden;
	protected boolean selected;
	
	public FileListEntry(File file) {
		this.path = file;
		this.absolutePath = path.getAbsolutePath();
		this.name = path.getName();
	}

	public File getPath() {
		return path;
	}
	public void setPath(File path) {
		this.path = path;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}
	
	public void setAbsolutePath(String name) {
		absolutePath = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setHidden(boolean b) {
		this.hidden = b;
	}
	
	public boolean getHidden() {
		return hidden;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean b) {
		selected = b;
	}
//	public int getStorageType() {
//		return storage_type;
//	}
//	
//	public void setStorageType(int type) {
//		this.storage_type = type;
//	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileListEntry other = (FileListEntry) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	
	abstract public boolean isDir();

	abstract public boolean isDevice();
	
	abstract public Date getLastModified();
	
	abstract public long getSize();
}