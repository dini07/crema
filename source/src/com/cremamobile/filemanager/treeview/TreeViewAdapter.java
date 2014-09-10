package com.cremamobile.filemanager.treeview;

import java.util.Arrays;

import java.util.List;
import java.util.Set;

import com.cremamobile.filemanager.utils.CLog;
import com.cremamobile.filemanager.R;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

/**
 * Adapter used to feed the table view.
 * 
 * @param <T>
 *            class for ID of the tree
 */
public class TreeViewAdapter extends BaseAdapter implements ListAdapter {    
    private final TreeStateManager<Long> treeStateManager;
    private int numberOfRoot;
    private int numberOfLevels;
    private final LayoutInflater layoutInflater;

    private int indentWidth = 0;
    private int indicatorGravity = 0;
    private Drawable rootCollapsedDrawable;
    private Drawable rootExpandedDrawable;
    private Drawable directoryCollapsedDrawable;
    private Drawable directoryExpandedDrawable;
    private Drawable indicatorCollapsedDrawable;
    private Drawable indicatorExpandedDrawable;
    private Drawable indicatorBackgroundDrawable;
    private Drawable rowBackgroundDrawable;

    private boolean collapsible;
    private final Activity activity;
    private final TreeViewAdapterParent<Long> adapterParent;

    public Activity getActivity() {
        return activity;
    }

    protected TreeStateManager<Long> getManager() {
        return treeStateManager;
    }

    public void expandCollapse(final Long id) {
        final TreeNodeInfo<Long> info = treeStateManager.getNodeInfo(id);
        if (!info.isWithChildren()) {
            // ignore - no default action
            return;
        }
        if (info.isExpanded()) {
            treeStateManager.collapseChildren(id);
        } else {
            treeStateManager.expandDirectChildren(id);
        }
    }
    
    public void collapse(final Long id) {
        final TreeNodeInfo<Long> info = treeStateManager.getNodeInfo(id);
    	if (info.isWithChildren() && info.isExpanded()) {
            treeStateManager.collapseChildren(id);
        }
    }
    

    public boolean isNeedSearchChild(final Long id) {
        final TreeNodeInfo<Long> info = treeStateManager.getNodeInfo(id);
    	return info.isNeedSearchChild();
    }
    
//    private void calculateIndentWidth() {
//        if (expandedDrawable != null) {
//            indentWidth = Math.max(getIndentWidth(),
//                    expandedDrawable.getIntrinsicWidth());
//        }
//        if (collapsedDrawable != null) {
//            indentWidth = Math.max(getIndentWidth(),
//                    collapsedDrawable.getIntrinsicWidth());
//        }
//    }
    
    public TreeViewAdapter(final Activity activity, final TreeViewAdapterParent<Long> parent, 
    		final TreeStateManager<Long> treeStateManager, 
    		int numberOfLevels, int numberOfRoot) {
        this.activity = activity;
        this.adapterParent = parent;
        this.treeStateManager = treeStateManager;
        this.layoutInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.numberOfRoot = numberOfRoot;
        this.numberOfLevels = numberOfLevels;
        this.rootCollapsedDrawable = null;
        this.rootExpandedDrawable = null;
        this.directoryCollapsedDrawable = null;
        this.directoryExpandedDrawable = null;
        this.indicatorCollapsedDrawable = null;
        this.indicatorExpandedDrawable = null;
        this.indicatorBackgroundDrawable = null;
        this.rowBackgroundDrawable = null;
    }
    
    public TreeViewAdapter(final Activity activity,
    		final TreeViewAdapterParent<Long> parent, 
            final TreeStateManager<Long> treeStateManager, final int numberOfLevels) {
        this.activity = activity;
        this.adapterParent = parent;
        this.treeStateManager = treeStateManager;
        this.layoutInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.numberOfRoot = 1;	//default
        this.numberOfLevels = numberOfLevels;
        this.rootCollapsedDrawable = null;
        this.rootExpandedDrawable = null;
        this.directoryCollapsedDrawable = null;
        this.directoryExpandedDrawable = null;
        this.indicatorCollapsedDrawable = null;
        this.indicatorExpandedDrawable = null;
        this.indicatorBackgroundDrawable = null;
        this.rowBackgroundDrawable = null;
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        treeStateManager.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        treeStateManager.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return treeStateManager.getVisibleCount();
    }

    @Override
    public Object getItem(final int position) {
        return getTreeId(position);
    }

    public Long getTreeId(final int position) {
        return treeStateManager.getVisibleList().get(position);
    }

    public TreeNodeInfo<Long> getTreeNodeInfo(final int position) {
        return treeStateManager.getNodeInfo(getTreeId(position));
    }

    @Override
    public boolean hasStableIds() { // NOPMD
        return true;
    }

    @Override
    public int getItemViewType(final int position) {
        return getTreeNodeInfo(position).getLevel();
    }

    @Override
    public int getViewTypeCount() {
        return numberOfLevels + (numberOfRoot == 1 ? 0 : 1);
    }
    
    public int getRootCount() {
    	return numberOfRoot;
    }
    
    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() { // NOPMD
        return true;
    }

    @Override
    public boolean isEnabled(final int position) { // NOPMD
        return true;
    }

    @Override
    public final View getView(final int position, final View convertView,
            final ViewGroup parent) {
    	CLog.d(this, "Creating a view based on " + convertView
                + " with position " + position);
    	
        final TreeNodeInfo<Long> nodeInfo = getTreeNodeInfo(position);

        if (convertView == null) {
        	CLog.d(this, "Creating the view a new");
            final LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.tree_list_item_wrapper, null);
            return populateTreeItem(layout, getNewChildView(nodeInfo),
                    nodeInfo, true);
        } else {
        	CLog.d(this, "Reusing the view");
            final LinearLayout linear = (LinearLayout) convertView;
            final FrameLayout frameLayout = (FrameLayout) linear
                    .findViewById(R.id.treeview_list_item_frame);
            final View childView = frameLayout.getChildAt(0);
            updateView(childView, nodeInfo);
            return populateTreeItem(linear, childView, nodeInfo, false);
        }
    }

    public View getNewChildView(TreeNodeInfo<Long> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) getActivity()
                .getLayoutInflater().inflate(R.layout.tree_list_item, null);
        return updateView(viewLayout, treeNodeInfo);
    }

    public View updateView(View view, TreeNodeInfo<Long> treeNodeInfo) {
        final LinearLayout viewLayout = (LinearLayout) view;
        final TextView descriptionView = (TextView) viewLayout
                .findViewById(R.id.treeview_list_item_description);
        descriptionView.setText(treeNodeInfo.getName());
        return viewLayout;
    }

    /**
     * Retrieves background drawable for the node.
     * 
     * @param treeNodeInfo
     *            node info
     * @return drawable returned as background for the whole row. Might be null,
     *         then default background is used
     */
    public Drawable getBackgroundDrawable(final TreeNodeInfo<Long> treeNodeInfo) { // NOPMD
        return null;
    }

    private Drawable getDrawableOrDefaultBackground(final Drawable r) {
        if (r == null) {
            return activity.getResources()
                    .getDrawable(R.drawable.list_selector_background).mutate();
        } else {
            return r;
        }
    }

    private Drawable getIndicateDrawable(final TreeNodeInfo<Long> treeNode) {
    	if (treeNode.isRoot() || treeNode.isNeedSearchChild() || treeNode.isWithChildren()) {
    		if (treeNode.isExpanded()) {
    			return directoryExpandedDrawable;
    		} else {
    			return directoryExpandedDrawable;
    		}
    	} else {
    		return getDrawableOrDefaultBackground(null);
    	}
    }

    private Drawable getFolderDrawable(final TreeNodeInfo<Long> treeNode) {
    	if (treeNode.isRoot()) {
    		if (treeNode.isExpanded()) {
    			return rootExpandedDrawable;
    		} else {
    			return rootCollapsedDrawable;
    		}
    	} else if (treeNode.isNeedSearchChild() || treeNode.isWithChildren()) {
    		if (treeNode.isExpanded()) {
    			return directoryExpandedDrawable;
    		} else {
    			return directoryExpandedDrawable;
    		}
    	} else {
    		return getDrawableOrDefaultBackground(null);
    	}
    }

    public final LinearLayout populateTreeItem(final LinearLayout layout,
            final View childView, final TreeNodeInfo<Long> nodeInfo,
            final boolean newChildView) {
    	// 전체 영역 클릭 시 동작시킨다.
    	//final View itemLayout = layout.findViewById(R.id.treeview_list_item_frame);
    	
    	layout.setTag(nodeInfo.getId());
    	
    	// background 
        final Drawable individualRowDrawable = getBackgroundDrawable(nodeInfo);
        layout.setBackgroundDrawable(individualRowDrawable == null ? getDrawableOrDefaultBackground(rowBackgroundDrawable)
                : individualRowDrawable);

    	final Drawable indicatDrawable = getIndicateDrawable(nodeInfo);
    	final Drawable folderDrawable = getFolderDrawable(nodeInfo);

        final LinearLayout.LayoutParams indentLayoutParam = new LinearLayout.LayoutParams(
                calculateIndentation(nodeInfo), LayoutParams.FILL_PARENT);
        
        final LinearLayout indentLayout = (LinearLayout) layout
                .findViewById(R.id.treeview_list_indent);
//        indicatorLayout.setOnClickListener(itemClickListener);
        indentLayout.setGravity(indicatorGravity);
        indentLayout.setLayoutParams(indentLayoutParam);
        indentLayout.setTag(nodeInfo.getId());
        
        // ChildView에서 indicator icon, folder icon, text를 설정한다. 
        
        final ImageView indicateImageView = (ImageView) childView.findViewById(R.id.treeview_list_item_indicator_image);
        indicateImageView.setImageDrawable(getIndicateDrawable(nodeInfo));
        indicateImageView.setClickable(false);
        indicateImageView.setBackgroundDrawable(getDrawableOrDefaultBackground(indicatorBackgroundDrawable));
        indicateImageView.setScaleType(ScaleType.CENTER);
        
        final ImageView folderImageView = (ImageView) childView.findViewById(R.id.treeview_list_item_folder_image);
        folderImageView.setImageDrawable(getFolderDrawable(nodeInfo));
        folderImageView.setClickable(false);
        folderImageView.setBackgroundDrawable(getDrawableOrDefaultBackground(indicatorBackgroundDrawable));
        folderImageView.setScaleType(ScaleType.CENTER);
//        image.setTag(nodeInfo.getId());
//        if (nodeInfo.isWithChildren() && collapsible) {
//            image.setOnClickListener(indicatorClickListener);
//        } else {
//            image.setOnClickListener(null);
//        }
        
        final FrameLayout frameLayout = (FrameLayout) layout
                .findViewById(R.id.treeview_list_item_frame);
        final FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        if (newChildView) {
            frameLayout.addView(childView, childParams);
        }
        //frameLayout.setTag(nodeInfo.getId());
        return layout;
    }

    protected int calculateIndentation(final TreeNodeInfo<Long> nodeInfo) {
    	return getIndentWidth() * (nodeInfo.getLevel()+1);
    }

//    protected Drawable getDrawable(final TreeNodeInfo<Long> nodeInfo) {
//    	// Device 정보  Node 일 경우는 device관련 image표시 
//    	if (nodeInfo.isRoot()) {
//    		if (nodeInfo.isExpanded())
//    			return rootExpendedDrawable;
//    		else
//    			return rootCollapsedDrawable;
//    	}
//    	// Child가 이미 존재하거나, 곧 존재할 Node 일 경우에는 Directory 관련 이미지 표시
//    	if (nodeInfo.isWithChildren() || nodeInfo.isNeedSearchChild()) {
//    		if (nodeInfo.isExpanded()) {
//                return expandedDrawable;
//    		} else {
//                return collapsedDrawable;
//    		}
//    	}
//    	
//    	return getDrawableOrDefaultBackground(indicatorBackgroundDrawable);
//    }

    public void setIndicatorGravity(final int indicatorGravity) {
        this.indicatorGravity = indicatorGravity;
    }

    public void setRootCollapsedDrawable(final Drawable rootDrawable) {
    	this.rootCollapsedDrawable = rootDrawable;
//    	calculateIndentWidth();
    }
    
    public void setRootExpandedDrawable(final Drawable rootExpendedDrawable) {
    	this.rootExpandedDrawable = rootExpendedDrawable;
//    	calculateIndentWidth();
    }
    public void setDirectoryCollapsedDrawable(final Drawable collapsedDrawable) {
        this.directoryCollapsedDrawable = collapsedDrawable;
//        calculateIndentWidth();
    }

    public void setDirectoryExpandedDrawable(final Drawable expandedDrawable) {
        this.directoryExpandedDrawable = expandedDrawable;
//        calculateIndentWidth();
    }

    public void setIndicatorCollapsedDrawable(final Drawable collapsedDrawable) {
        this.indicatorCollapsedDrawable = collapsedDrawable;
//        calculateIndentWidth();
    }

    public void setIndicatorExpandedDrawable(final Drawable expandedDrawable) {
        this.indicatorExpandedDrawable = expandedDrawable;
//        calculateIndentWidth();
    }

    public void setIndicatorBackgroundDrawable(
            final Drawable indicatorBackgroundDrawable) {
        this.indicatorBackgroundDrawable = indicatorBackgroundDrawable;
    }

    public void setIndentWidth(final int indentWidth) {
        this.indentWidth = indentWidth;
//        calculateIndentWidth();
    }

    public void setRowBackgroundDrawable(final Drawable rowBackgroundDrawable) {
        this.rowBackgroundDrawable = rowBackgroundDrawable;
    }

    public void setCollapsible(final boolean collapsible) {
        this.collapsible = collapsible;
    }

    public void refresh() {
        treeStateManager.refresh();
    }

    private int getIndentWidth() {
        return indentWidth;
    }

    public void handleItemClick(AdapterView<?> parent, View view, int position, long id) {
//        expandCollapse((Long) id);
        
        Long longId = (Long) id;
        TreeNodeInfo<Long> info = treeStateManager.getNodeInfo(longId);
        
//		int childCount = parent.getChildCount();
//		for(int i=0; i<childCount; i++) {
//			if(i == position) {
//				parent.getChildAt(i).setBackgroundColor(Color.BLUE);
//			}
//			else {
//				parent.getChildAt(i).setBackgroundColor(Color.BLACK);
//			}
//		}

    	expandCollapse((Long) id);

//        List<Long> children = treeStateManager.getChildren(longId);
//        if (children != null || children.size() > 0) {
//        	for (Long childId : children) {
//                TreeNodeInfo<Long> childInfo = treeStateManager.getNodeInfo(childId);
//                if (childInfo.isNeedSearchChild())
//                	adapterParent.updateChildList(childId, childInfo.getPaht());
//        	}
//        }
        
        TreeNodeInfo<Long> nodeInfo = treeStateManager.getNodeInfo(longId);
        adapterParent.updateChildList(longId, nodeInfo.getPaht(), nodeInfo.isNeedSearchChild());


//        if (info.isWithChildren()) {
//    		if (info.isNeedSearchChild() && parent != null) {
//    			adapterParent.updateChildList(longId, info.getPaht());
//    		}
//        }
    }

    @Override
    public long getItemId(final int position) {
        return getTreeId(position);
    }
    
}
