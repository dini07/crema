package com.cremamobile.filemanager.device;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils.SimpleStringSplitter;

/**
 * Ein {@link Device}, das ein speziell gemountetes Ger�t beschreibt, z.B.
 * die Secondary-SDs vieler moderner Ger�te und die USB-Ger�te bzw.
 * Kartenleser. Erkennt die Pfade aus vold.fstab (siehe {@link DeviceUtils}
 * und emuliert die getXXXDir-Methoden, die sonst {@link Context} hat.
 * 
 * @author Jockel
 *
 */
class DeviceDiv extends Device {
	private String mLabel, mName;
	private boolean mAvailable, mWriteable;
	

	/**
	 * Constructor, der eine Zeile aus vold.fstab bekommt (dev_mount schon weggelesen)
	 * @param sp der StringSplitter, aus dem die Zeile gelesen wird, wobei
	 * 		"dev_mount" schon weggelesen sein muss.
	 * @return this f�r Verkettungen wie {@code return new Device().initFrom...() } 
	 */
	DeviceDiv(SimpleStringSplitter sp) {
		mLabel = sp.next().trim();
		mMountPoint = sp.next().trim();
		updateState();
	}
	
	@Override
	public boolean isAvailable() { return mAvailable; }

	@Override
	public boolean isWriteable() { return mWriteable; }

	@Override
	protected void updateState() {
		File f = new File(mMountPoint);
		setName(f.getName()); // letzter Teil des Pfads
		if (mAvailable = f.isDirectory() && f.canRead()) { // ohne canRead() klappts z.B. beim Note2 nicht
			mSize = Size.getSpace(f); 
			mWriteable = f.canWrite();
			// Korrektur, falls in /mnt/sdcard gemountet (z.B. Samsung)
			if (mMountPoint.startsWith(DeviceUtils.mPrimary.mMountPoint) && mSize.equals(DeviceUtils.mPrimary.mSize)) 
				mAvailable = mWriteable = false;
		} else 
			mWriteable = false;
		
	}

	public final String getLabel() { return mLabel; }

	public String getName() { return mName; }
	protected final void setName(String name) { mName = name; }

	@Override
	public boolean isRemovable() { return true; }

	@Override
	public File getCacheDir(Context ctx) { return getFilesDirLow(ctx, "/cache"); }

	@Override
	public File getFilesDir(Context ctx) { return getFilesDirLow(ctx, "/files"); }
	
	@Override
	public File getFilesDir(Context ctx, String s) { return getFilesDirLow(ctx, s); }

	@Override
	public File getPublicDirectory(String s) { 
		if (s!=null && !s.startsWith("/")) s = "/" + s;
		return new File(getMountPoint() + s);
	}

	@Override
	public String getState() {
		if (mAvailable)
			return mWriteable ? Environment.MEDIA_MOUNTED : Environment.MEDIA_MOUNTED_READ_ONLY;
		else 
			return Environment.MEDIA_REMOVED;
	}

}
