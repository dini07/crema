package com.cremamobile.filemanager.history;

public class HistoryNode {
	String path;
	int currentPosition;
	
	public String getPath() {
		return path;
	}
	
	public int getCurrentPosition() {
		return currentPosition;
	}
	
	public HistoryNode(String path, int currentPosition) {
		this.path = path;
		this.currentPosition = currentPosition;
	}
}
