package com.cremamobile.filemanager;

import java.io.File;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.cremamobile.filemanager.treeview.InMemoryTreeStateManager;
import com.cremamobile.filemanager.treeview.TreeBuilder;
import com.cremamobile.filemanager.treeview.TreeStateManager;
import com.cremamobile.filemanager.treeview.TreeViewAdapter;
import com.cremamobile.filemanager.treeview.TreeViewAdapterParent;
import com.cremamobile.filemanager.treeview.TreeViewList;

class NavigationDrawerDirectoryTree extends NavigationDrawerTreeHelper{
    public TreeViewList listView;
    public TreeStateManager<Long> manager = null;
    public TreeViewAdapter listAdapter;
    public boolean collapsible;
    public int	treeLevel = 1;
    
    public int deviceCount = 0;

    public File root;

    public void createTreeManager(Activity parent, TreeViewAdapterParent<Long> treeParent, int type) {
		this.manager = new InMemoryTreeStateManager();
        this.manager.setTreeBuilder(new TreeBuilder<Long>(manager));

        this.listAdapter = new TreeViewAdapter(parent, treeParent, manager, treeLevel);
        this.listView.setAdapter(listAdapter);
        this.listView.setCollapsible(true);
        this.collapsible = true;
        this.tree_type = type;
    }

}

class NavigationDrawerFavoriteTree extends NavigationDrawerTreeHelper {
	public ListView	listView;
	public ListAdapter listAdapter;
	
	@Override
	void createTreeManager(Activity parent,
			TreeViewAdapterParent<Long> treeParent, int type) {
		// TODO Auto-generated method stub
		tree_type = type;
		
		//TODO...
	}
	
}

public abstract class NavigationDrawerTreeHelper {
	public static final int TREE_TYPE_INTERNAL = 0;
	public static final int TREE_TYPE_EXTERNAL = 1;
	public static final int TREE_TYPE_FAVORITE = 2;
	
    public int tree_type;
    public long currentSelectedID;
    
    abstract void createTreeManager(Activity parent, TreeViewAdapterParent<Long> treeParent, int type);
    
}
