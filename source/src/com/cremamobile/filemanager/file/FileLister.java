package com.cremamobile.filemanager.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.cremamobile.filemanager.device.Device;
import com.cremamobile.filemanager.utils.CLog;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileLister {
	private static final long ONE_GIGABYTE = 1024*1024*1024;
	private static Context context;
	
	private static String primary_sd_path;
	
//	public static List<FileListEntry> getRootExternalStorage(Context context) {
//		ArrayList<String> sVold = readVoldFile(context);
//		
//		return setPropertiesStorage(sVold);
//	}

	
//	public static List<FileListEntry> getSDCardDirectoryLists(int childDepth) {
//		File sd = Environment.getExternalStorageDirectory();
//		String state = Environment.getExternalStorageState();
//		List<FileListEntry> list = new ArrayList<FileListEntry>();
//		
//		if (Environment.MEDIA_MOUNTED.equals(state) && sd.isDirectory()) {
//            File[] fileArr = sd.listFiles();
//            for (File f : fileArr) {
//            	if (f.isDirectory()) {
//            		FileListEntry entry = new FileListEntry(f);
//            		if (childDepth > 0) {
//            			entry.setChildLists(getDirectoryLists(f, childDepth-1));
//            		}
//            		list.add(entry);
//            	}
//            }
//		}
//		return list;
//	}
	
	public static List<FileListEntry> getDirectoryLists(File parent, boolean withHidden) {
		if (parent == null)
			return null;
		
		List<FileListEntry> list = new ArrayList<FileListEntry>();
		File[] files = parent.listFiles();
		if (files == null || files.length <= 0)
			return null;
		
		for(File f : files) {
			if (f.isDirectory()) {
				DirFileEntry entry = new DirFileEntry(f);
				
				if (entry.getName().startsWith(".")) {
					if (withHidden) {
						entry.setHidden(true);
					} else {
						continue;
					}
				}
				
				File[] childs = f.listFiles();
				if (childs != null) {
//					entry.setChildNumber(childs.length);
					for (File child : childs){
						if (child.isDirectory() ) {
							if (withHidden || !entry.getName().startsWith(".")) {
								entry.setChildIsExistButNotInsertChild(true);
								break;
							}
						}
					}
				}
        		list.add(entry);
			}
		}
		return list;
	}

	public static List<FileListEntry> getFileLists(String path, boolean withHidden) {
		File parent;
		if (path == null || (parent = new File(path)) == null)
			return null;
		
		List<FileListEntry> list = new ArrayList<FileListEntry>();
		File[] files = parent.listFiles();
		if (files == null || files.length <= 0)
			return null;
		
		for(File f : files) {
			if (f.isDirectory()) {
				DirFileEntry entry = new DirFileEntry(f);
				
				if (entry.getName().startsWith(".")) {
					if (withHidden) {
						entry.setHidden(true);
					} else {
						continue;
					}
				}
				
				File[] childs = f.listFiles();
				if (childs != null) {
//					entry.setChildNumber(childs.length);
					for (File child : childs){
						if (child.isDirectory() ) {
							if (withHidden || !entry.getName().startsWith(".")) {
								entry.setChildIsExistButNotInsertChild(true);
								break;
							}
						}
					}
				}
        		list.add(entry);
			} else {
				FileEntry entry = new FileEntry(f);
				if (entry.getName().startsWith(".")) {
					if (withHidden) {
						entry.setHidden(true);
					} else {
						continue;
					}
				}
				
				list.add(entry);
			}
		}
		return list;
	}

//	public static List<FileListEntry> getDirectoryLists(File parent, int childDepth) {
//		if (parent == null)
//			return null;
//		
//		List<FileListEntry> list = new ArrayList<FileListEntry>();
//		File[] files = parent.listFiles();
//		if (files == null || files.length <= 0)
//			return null;
//		
//		for(File f : files) {
//			if (f.isDirectory()) {
//				FileListEntry entry = new FileListEntry(f);
//				if (childDepth > 0) {
//					entry.setChildLists(getDirectoryLists(f, childDepth-1));
//				}
//        		list.add(entry);
//			}
//		}
//		return list;
//	}
	
//	public static List<FileListEntry> getSDCardAllDirectoryLists() {
//		File sd = Environment.getExternalStorageDirectory();
//		String state = Environment.getExternalStorageState();
//		List<FileListEntry> list = new ArrayList<FileListEntry>();
//		
//		if (Environment.MEDIA_MOUNTED.equals(state) && sd.isDirectory()) {
//            File[] fileArr = sd.listFiles();
//            FileListEntry entry;
//            for (File f : fileArr) {
//            	if (f.isDirectory()) {
//            		entry = new FileListEntry(f);
//            		entry.setChildLists(getDirectoryAllLists(f));
//            		list.add(entry);
//            		
//            	}
//            }
//		}
//		return list;
//	}
	
//	public static List<FileListEntry> getDirectoryAllLists(File parent) {
//		if (parent == null)
//			return null;
//		
//		List<FileListEntry> list = new ArrayList<FileListEntry>();
//		File[] files = parent.listFiles();
//		if (files == null || files.length <= 0)
//			return null;
//		
//		FileListEntry entry;
//		for(File f : files) {
//			if (f.isDirectory()) {
//				entry = new FileListEntry(f);
//        		entry.setChildLists(getDirectoryAllLists(f));
//        		list.add(entry);
//			}
//		}
//		return list;
//	}
	
	public static void getAllLists() {
		/*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1
		 * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 * 
		 * some devices are missing the vold file entirely so we add a path here
		 * to make sure the list always includes the path to the first sdcard,
		 * whether real or emulated.
		 */

		//sVold.add("/mnt/sdcard");
		

		try {
			Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[2];

					if (element.contains(":"))
						element = element.substring(0, element.indexOf(":"));

					if (element.contains("usb"))
						continue;

					// don't add the default vold path
					// it's already in the list.
//					if (!sVold.contains(element))
//						sVold.add(element);
				}
			}
			scanner.close();
		} catch (Exception e) {
			// swallow - don't care
			e.printStackTrace();
		} finally {
			
		}
	}
	
	public static String[] labels;
	public static String[] paths;
	public static int count = 0;

	public static ArrayList<String> readVoldFile(Context context) {
		/*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1
		 * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 * 
		 * some devices are missing the vold file entirely so we add a path here
		 * to make sure the list always includes the path to the first sdcard,
		 * whether real or emulated.
		 */
		ArrayList<String> sVold = new ArrayList<String>();
		sVold.add("/mnt/sdcard");
		sVold.add("/storage/emulated/0");
		sVold.add("/storage/external_SD");
		
		try {
			Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				CLog.d("FileLister", "scanner [" + line + "]");
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[2];

					if (element.contains(":"))
						element = element.substring(0, element.indexOf(":"));

					if (element.contains("usb"))
						continue;

					File path = new File(element);
					CLog.d("FileLister", "file:"+path.getName() + ", isDirectory:"+path.isDirectory() + ", canWrite:"+path.canWrite());
					if (path.exists() && path.isDirectory() /*&& path.canWrite()*/) {
						// don't add the default vold path
						// it's already in the list.
						if (!sVold.contains(element))
							sVold.add(element);
					}
				}
			}
		} catch (Exception e) {
			// swallow - don't care
			e.printStackTrace();
		}

		return sVold;
	}

//	public static List<FileListEntry> setPropertiesStorage(ArrayList<String> sVold) {
//		/*
//		 * At this point all the paths in the list should be valid. Build the
//		 * public properties.
//		 */
//		int i = 0;
//		if (sVold != null && sVold.size() > 0) {
//			List<FileListEntry> lists = new ArrayList<FileListEntry>();
//			
//			FileListEntry sdRoot = new FileListEntry();
//			sdRoot.setPath(new File(sVold.get(0)));
//			
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//				sdRoot.setName("Auto");
//				sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//			} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//				if (Environment.isExternalStorageRemovable()) {
//					sdRoot.setName("SDCARD 1");
//					sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_EXTERNAL);
//					i = 1;
//				} else
//					sdRoot.setName("SDCARD 0");
//					sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//			} else {
//				if (!Environment.isExternalStorageRemovable()
//						|| Environment.isExternalStorageEmulated()) {
//					sdRoot.setName("SDCARD 0");
//					sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//				} else {
//					sdRoot.setName("SDCARD 1");
//					sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_EXTERNAL);
//					i = 1;
//				}
//			}
//
//			lists.add(sdRoot);
//			
//			if (sVold.size() > 1) {
//				for (int j = 1; j < sVold.size(); j++) {
//					FileListEntry entry = new FileListEntry(new File(sVold.get(j)));
//					entry.setName("SDCARD " + (i + j));
//					entry.setStorageType(FileListEntry.STORAGE_TYPE_SD_EXTERNAL);
//				}
//			}
//			return lists;
//		}
//		return null;
//	}
	
	private static ArrayList<String> readMountsFile() {
		ArrayList<String> mMounts = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner(new File("/proc/mounts"));
			CLog.d("FileLister", "/proc/mounts");
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				CLog.d("FileLister", "scanner[" + line + "]");
				if (line.startsWith("/dev/fuse")) {
//				if (line.startsWith("/dev/block/vold/")) {
					String[] lineElements = line.split("[ \t]+");
					String element = lineElements[1];
					mMounts.add(element);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mMounts;
	}
	
//	private static List<String> readVoldFile() {
//		List<String> mVold = new ArrayList<String>();
//		try {
//			Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
//			
//			CLog.d("FileLister", "/system/etc/vold.fstab");
//			while (scanner.hasNext()) {
//				String line = scanner.nextLine();
//				CLog.d("FileLister", "scanner[" + line + "]");
//				if (line.startsWith("dev_mount")) {
//					String[] lineElements = line.split("[ \t]+");
//					String element = lineElements[2];
//					if (element.contains(":")) {
//						element = element.substring(0, element.indexOf(":"));
//					}
//					mVold.add(element);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return mVold;
//	}
	
	private static boolean checkMicroSDCard(String fileSystemName) {
		StatFs statFs = new StatFs(fileSystemName);
	    long totalSize = (long)statFs.getBlockSize() * statFs.getBlockCount();
		CLog.d("FileLister", "fileSystem[" + fileSystemName + "] size=" + totalSize);
	    if (totalSize < ONE_GIGABYTE) {
	        return false;
	    }
	    return true;
	}
	
	private static boolean isAvailableFileSystem(String fileSystemName) {
	    final String[]  unAvailableFileSystemList = {"/dev", "/mnt/asec", "/mnt/obb", "/system", "/data", "/cache", "/efs", "/firmware"};   // 알려진 File System List입니다.
	    
	    for (String name : unAvailableFileSystemList) {
	        if (fileSystemName.contains(name) == true) {
	            return false;
	        }
	    }
	     
	    if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(fileSystemName) == true) {
	        /** 안드로이드에서 제공되는 getExternalStorageDirectory() 경로와 같은 경로일 경우에는 추가로 삽입된 SDCard가 아니라고 판단하였습니다. **/
	        return false;
	    }
	     
	    return true;
	}
	
	public static List<FileListEntry> getDevices(Context context) {
		List<FileListEntry> deviceList = new ArrayList<FileListEntry>();
		
//		File rootDir = Environment.getRootDirectory();
//		if (rootDir != null) {
//			DeviceFileEntry root = new DeviceFileEntry(rootDir, 
//					DeviceFileEntry.STORAGE_TYPE_SYSTEM, false);
//			root.setName("Root");
//			deviceList.add(root);
//		}
		
		File sd0 = Environment.getExternalStorageDirectory();
		primary_sd_path = sd0.getAbsolutePath();	// 기본 SD 경로를 기억한다.  External에서 중복하지 않도록
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && sd0.isDirectory()) {
			DeviceFileEntry primarySD = new DeviceFileEntry(sd0, 
							DeviceFileEntry.STORAGE_TYPE_SD_PRIMARY,
							(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) ? Environment.isExternalStorageRemovable() : true);
			primarySD.setName("SDCARD 0");
			deviceList.add(primarySD);
		}
		
	    ArrayList<String> mMounts = readMountsFile();
	    //ArrayList<String> mVold = readVoldFile(context);
	    
	    for (int i=0; i < mMounts.size(); i++) {
	        String mount = mMounts.get(i);
	         
			CLog.d("FileLister", "getMicroSDCardDirectory[" + i + "] mount=" + mount);

//	        if (!mVold.contains(mount)) {
//	            mMounts.remove(i--);
//	            continue;
//	        }
	        
			if (mount.contains("emulated")) {
				mMounts.remove(i--);
				continue;
			}
			
			if (primary_sd_path.equals(mount)) {
	            mMounts.remove(i--);
				continue;
			}
			
	        File root = new File(mount);
			CLog.d("FileLister", "getMicroSDCardDirectory[" + i + "] exist=" + root.exists() + ", isDir=" + root.isDirectory());
	        if (!root.exists() || !root.isDirectory()) {
	            mMounts.remove(i--);
	            continue;
	        }
	         
	        if (!isAvailableFileSystem(mount)) {
	            mMounts.remove(i--);
	            continue;
	        }
	         
	        if (!checkMicroSDCard(mount)) {
	            mMounts.remove(i--);
	        	continue;
	        }
	        
	        DeviceFileEntry extStorage = new DeviceFileEntry(root, 
	        	DeviceFileEntry.STORAGE_TYPE_SD_EXTERNAL,
	        	true);
	        deviceList.add(extStorage);
	    }

	    if (deviceList.size() > 0)
	    	return deviceList;
	    return null;
	}
	
//	private static int getStorageType(File root) {
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//			return DeviceFileEntry.STORAGE_TYPE_SD_INTERNAL;
//			sdRoot.setName("Auto");
//			sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//			if (Environment.isExternalStorageRemovable()) {
//				sdRoot.setName("SDCARD 1");
//				sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_EXTERNAL);
//				i = 1;
//			} else
//				sdRoot.setName("SDCARD 0");
//				sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//		} else {
//			if (!Environment.isExternalStorageRemovable()
//					|| Environment.isExternalStorageEmulated()) {
//				sdRoot.setName("SDCARD 0");
//				sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_INTERNAL);
//			} else {
//				sdRoot.setName("SDCARD 1");
//				sdRoot.setStorageType(FileListEntry.STORAGE_TYPE_SD_EXTERNAL);
//				i = 1;
//			}
//		}
//	}
	
	public static String getSDCARDiD() {
		String memBlk, sd_cid="";
        try {
            File file = new File("/sys/block/mmcblk1");
            if (file.exists() && file.isDirectory()) {
                memBlk = "mmcblk1";
            } else {
                //System.out.println("not a directory");
                memBlk = "mmcblk0";
            }

            Process cmd = Runtime.getRuntime().exec("cat /sys/block/"+memBlk+"/device/cid");
            BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            sd_cid = br.readLine();
        } catch (IOException e) {
        	return null;
        }
        return sd_cid;
    }
}
