package com.cremamobile.filemanager.treeview;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Node. It is package protected so that it cannot be used outside.
 * 
 * @param <T>
 *            type of the identifier used by the tree
 */
class InMemoryTreeNode<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final T id;
    private final boolean isRoot;
    private final T parent;
    private final int level;
    private final String path;
    private final String name;
    private boolean visible = true;
    private boolean needSearchChild = true;
    private final List<InMemoryTreeNode<T>> children = new LinkedList<InMemoryTreeNode<T>>();
    private List<T> childIdListCache = null;

    public InMemoryTreeNode(final T id, final String path, final String name, final boolean isRoot, final T parent, final int level,
    		final boolean needSearchChild, final boolean visible) {
        super();
        this.id = id;
        this.isRoot = isRoot;
        this.path = path;
        this.name = name;
        this.parent = parent;
        this.level = level;
        this.needSearchChild = needSearchChild;
        this.visible = visible;
    }

    public int indexOf(final T id) {
        return getChildIdList().indexOf(id);
    }

    /**
     * Cache is built lasily only if needed. The cache is cleaned on any
     * structure change for that node!).
     * 
     * @return list of ids of children
     */
    public synchronized List<T> getChildIdList() {
        if (childIdListCache == null) {
            childIdListCache = new LinkedList<T>();
            for (final InMemoryTreeNode<T> n : children) {
                childIdListCache.add(n.getId());
            }
        }
        return childIdListCache;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public int getChildrenListSize() {
        return children.size();
    }

    public synchronized InMemoryTreeNode<T> add(final int index, final T child, final String childPath, final String childName,
            final boolean isRoot, final boolean needSearchChild, final boolean visible) {
        childIdListCache = null;
        // Note! top levell children are always visible (!)
        final InMemoryTreeNode<T> newNode = new InMemoryTreeNode<T>(child, childPath, childName, isRoot,
                getId(), getLevel() + 1, needSearchChild, getId() == null ? true : visible);
        children.add(index, newNode);
        return newNode;
    }

    /**
     * Note. This method should technically return unmodifiable collection, but
     * for performance reason on small devices we do not do it.
     * 
     * @return children list
     */
    public List<InMemoryTreeNode<T>> getChildren() {
        return children;
    }

    public boolean isRoot() {
    	return isRoot;
    }
    
    public synchronized void clearChildren() {
        children.clear();
        childIdListCache = null;
        needSearchChild = true;
    }

    public synchronized void removeChild(final T child) {
        final int childIndex = indexOf(child);
        if (childIndex != -1) {
            children.remove(childIndex);
            childIdListCache = null;
        }
    }

    @Override
    public String toString() {
        return "InMemoryTreeNode [id=" + id
        		+ ", path=" + path
        		+ ", name=" + name
        		+ ", parent=" + parent
                + ", level=" + level + ", visible=" + visible
                + ", children=" + children + ", childIdListCache="
                + childIdListCache + "]";
    }

    T getId() {
        return id;
    }

    T getParent() {
        return parent;
    }

    int getLevel() {
        return level;
    }
    
    String getPath() {
    	return path;
    }
    
    String getName() {
    	return name;
    }
    
    boolean getNeedSearchChild() {
    	return needSearchChild;
    }
    
    void setNeedSearchChild(boolean need) {
    	needSearchChild = need;
    }

}