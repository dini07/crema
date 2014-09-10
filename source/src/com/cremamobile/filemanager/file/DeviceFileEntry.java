package com.cremamobile.filemanager.file;

import java.io.File;
import java.util.Date;

public class DeviceFileEntry extends FileListEntry {
	public static final int STORAGE_TYPE_SD_EXTERNAL = 1;
	public static final int STORAGE_TYPE_SD_PRIMARY = 2;
	public static final int STORAGE_TYPE_SYSTEM = 3;
	public static final int STORAGE_TYPE_EXTERNAL_ETC = 4;
	public static final int STORAGE_TYPE_EXTERNAL_OTG = 5;

	private final int device_type;
	private final boolean removable;
	public DeviceFileEntry(File file, int device_type, boolean removable) {
		super(file);
		// TODO Auto-generated constructor stub
		this.device_type = device_type;
		this.removable = removable;
	}

	@Override
	public boolean isDir() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDevice() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean isExternalDevice() {
		if (device_type == STORAGE_TYPE_SD_EXTERNAL || device_type == STORAGE_TYPE_EXTERNAL_ETC
				|| device_type == STORAGE_TYPE_EXTERNAL_OTG)
			return true;
		return false;
	}
	
	public boolean isRemovable() {
		return removable;
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
