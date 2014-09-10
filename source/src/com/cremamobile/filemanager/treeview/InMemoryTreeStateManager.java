package com.cremamobile.filemanager.treeview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cremamobile.filemanager.file.FileListEntry;
import com.cremamobile.filemanager.utils.CLog;



import android.database.DataSetObserver;

/**
 * In-memory manager of tree state.
 * 
 * @param <T>
 *            type of identifier
 */
public class InMemoryTreeStateManager implements TreeStateManager<Long> {
    private static final long serialVersionUID = 1L;
    private final Map<Long, InMemoryTreeNode<Long>> allNodes = new HashMap<Long, InMemoryTreeNode<Long>>();
    private final InMemoryTreeNode<Long> topSentinel = new InMemoryTreeNode<Long>(
            null, null, null, false, null, -1, true, true);
    private transient List<Long> visibleListCache = null; // lasy initialised
    private transient List<Long> unmodifiableVisibleList = null;
    private boolean visibleByDefault = true;
    private final transient Set<DataSetObserver> observers = new HashSet<DataSetObserver>();
    private TreeBuilder<Long> builder;
    
    private synchronized void internalDataSetChanged() {
        visibleListCache = null;
        unmodifiableVisibleList = null;
        for (final DataSetObserver observer : observers) {
            observer.onChanged();
        }
    }
    
    /**
     * If true new nodes are visible by default.
     * 
     * @param visibleByDefault
     *            if true, then newly added nodes are expanded by default
     */
    public void setVisibleByDefault(final boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }

    private InMemoryTreeNode<Long> getNodeFromTreeOrThrow(final Long id) {
        if (id == null) {
            throw new NodeNotInTreeException("(null)");
        }
        final InMemoryTreeNode<Long> node = allNodes.get(id);
        if (node == null) {
            throw new NodeNotInTreeException(id.toString());
        }
        return node;
    }

    private InMemoryTreeNode<Long> getNodeFromTreeOrThrowAllowRoot(final Long id) {
        if (id == null) {
            return topSentinel;
        }
        return getNodeFromTreeOrThrow(id);
    }

    private void expectNodeNotInTreeYet(final Long id) {
        final InMemoryTreeNode<Long> node = allNodes.get(id);
        if (node != null) {
            throw new NodeAlreadyInTreeException(id.toString(), node.toString());
        }
    }

    @Override
    public synchronized TreeNodeInfo<Long> getNodeInfo(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrow(id);
        final List<InMemoryTreeNode<Long>> children = node.getChildren();
        boolean expanded = false;
        if (!children.isEmpty() && children.get(0).isVisible()) {
            expanded = true;
        }
        return new TreeNodeInfo<Long>(id, node.getPath(), node.getName(), node.isRoot(), node.getLevel(), !children.isEmpty(),
                node.isVisible(), expanded, node.getNeedSearchChild());
    }

    @Override
    public synchronized List<Long> getChildren(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        return node.getChildIdList();
    }

    @Override
    public synchronized Long getParent(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        return node.getParent();
    }

    private boolean getChildrenVisibility(final InMemoryTreeNode<Long> node) {
        boolean visibility;
        final List<InMemoryTreeNode<Long>> children = node.getChildren();
        if (children.isEmpty()) {
            visibility = visibleByDefault;
        } else {
            visibility = children.get(0).isVisible();
        }
        return visibility;
    }

    @Override
    public synchronized void addBeforeChild(final Long parent, final Long newChild, final String childPath, final String childName,
    		final boolean isRoot, final boolean needSearchChild, final Long beforeChild) {
        expectNodeNotInTreeYet(newChild);
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(parent);
        final boolean visibility = getChildrenVisibility(node);
        // top nodes are always expanded.
        if (beforeChild == null) {
            final InMemoryTreeNode<Long> added = node.add(0, newChild, childPath, childName, isRoot, needSearchChild, visibility);
            allNodes.put(newChild, added);
        } else {
            final int index = node.indexOf(beforeChild);
            final InMemoryTreeNode<Long> added = node.add(index == -1 ? 0 : index, newChild, childPath, childName, isRoot, needSearchChild, visibility);
            allNodes.put(newChild, added);
        }
        if (visibility) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized void addAfterChild(final Long parent, final Long newChild, final String childPath, final String childName, 
            final boolean isRoot, final boolean needSearchChild, final Long afterChild) {
        expectNodeNotInTreeYet(newChild);
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(parent);
        final boolean visibility = getChildrenVisibility(node);
        if (afterChild == null) {
            final InMemoryTreeNode<Long> added = node.add(node.getChildrenListSize(), newChild, childPath, childName, isRoot, needSearchChild, visibility);
            allNodes.put(newChild, added);
        } else {
            final int index = node.indexOf(afterChild);
            final InMemoryTreeNode<Long> added = node.add(
                    index == -1 ? node.getChildrenListSize() : index + 1, newChild, childPath, childName, isRoot, needSearchChild, visibility);
            allNodes.put(newChild, added);
        }
        node.setNeedSearchChild(false);
        if (visibility) {
            internalDataSetChanged();
        }
    }

    @Override
    public synchronized void removeNodeRecursively(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        final boolean visibleNodeChanged = removeNodeRecursively(node);
        final Long parent = node.getParent();
        final InMemoryTreeNode<Long> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        parentNode.removeChild(id);
        if (visibleNodeChanged) {
            internalDataSetChanged();
        }
    }

    private boolean removeNodeRecursively(final InMemoryTreeNode<Long> node) {
        boolean visibleNodeChanged = false;
        for (final InMemoryTreeNode<Long> child : node.getChildren()) {
            if (removeNodeRecursively(child)) {
                visibleNodeChanged = true;
            }
        }
        node.clearChildren();
        if (node.getId() != null) {
            allNodes.remove(node.getId());
            if (node.isVisible()) {
                visibleNodeChanged = true;
            }
        }
        return visibleNodeChanged;
    }

    private void setChildrenVisibility(final InMemoryTreeNode<Long> node,
            final boolean visible, final boolean recursive) {
        for (final InMemoryTreeNode<Long> child : node.getChildren()) {
            child.setVisible(visible);
            if (recursive) {
                setChildrenVisibility(child, visible, true);
            }
        }
    }

    @Override
    public synchronized void expandDirectChildren(final Long id) {
        CLog.d(this, "Expanding direct children of " + id);
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        setChildrenVisibility(node, true, false);
        internalDataSetChanged();
    }

    @Override
    public synchronized void expandEverythingBelow(final Long id) {
        CLog.d(this, "Expanding all children below " + id);
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        setChildrenVisibility(node, true, true);
        internalDataSetChanged();
    }

    @Override
    public synchronized void collapseChildren(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        if (node == topSentinel) {
            for (final InMemoryTreeNode<Long> n : topSentinel.getChildren()) {
                setChildrenVisibility(n, false, true);
            }
        } else {
            setChildrenVisibility(node, false, true);
        }
        internalDataSetChanged();
    }

    @Override
    public synchronized Long getNextSibling(final Long id) {
        final Long parent = getParent(id);
        final InMemoryTreeNode<Long> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        boolean returnNext = false;
        for (final InMemoryTreeNode<Long> child : parentNode.getChildren()) {
            if (returnNext) {
                return child.getId();
            }
            if (child.getId().equals(id)) {
                returnNext = true;
            }
        }
        return null;
    }

    @Override
    public synchronized Long getPreviousSibling(final Long id) {
        final Long parent = getParent(id);
        final InMemoryTreeNode<Long> parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
        Long previousSibling = null;
        for (final InMemoryTreeNode<Long> child : parentNode.getChildren()) {
            if (child.getId().equals(id)) {
                return previousSibling;
            }
            previousSibling = child.getId();
        }
        return null;
    }

    @Override
    public synchronized boolean isInTree(final Long id) {
        return allNodes.containsKey(id);
    }

    @Override
    public synchronized int getVisibleCount() {
        return getVisibleList().size();
    }

    @Override
    public synchronized List<Long> getVisibleList() {
    	Long currentId = null;
        if (visibleListCache == null) {
            visibleListCache = new ArrayList<Long>(allNodes.size());
            do {
                currentId = getNextVisible(currentId);
                if (currentId == null) {
                    break;
                } else {
                    visibleListCache.add(currentId);
                }
            } while (true);
        }
        if (unmodifiableVisibleList == null) {
            unmodifiableVisibleList = Collections
                    .unmodifiableList(visibleListCache);
        }
        return unmodifiableVisibleList;
    }

    public synchronized Long getNextVisible(final Long id) {
        final InMemoryTreeNode<Long> node = getNodeFromTreeOrThrowAllowRoot(id);
        if (!node.isVisible()) {
            return null;
        }
        final List<InMemoryTreeNode<Long>> children = node.getChildren();
        if (!children.isEmpty()) {
            final InMemoryTreeNode<Long> firstChild = children.get(0);
            if (firstChild.isVisible()) {
                return firstChild.getId();
            }
        }
        final Long sibl = getNextSibling(id);
        if (sibl != null) {
            return sibl;
        }
        Long parent = node.getParent();
        do {
            if (parent == null) {
                return null;
            }
            final Long parentSibling = getNextSibling(parent);
            if (parentSibling != null) {
                return parentSibling;
            }
            parent = getNodeFromTreeOrThrow(parent).getParent();
        } while (true);
    }

    @Override
    public synchronized void registerDataSetObserver(
            final DataSetObserver observer) {
        observers.add(observer);
    }

    @Override
    public synchronized void unregisterDataSetObserver(
            final DataSetObserver observer) {
        observers.remove(observer);
    }

    @Override
    public int getLevel(final Long id) {
        return getNodeFromTreeOrThrow(id).getLevel();
    }

    @Override
    public Integer[] getHierarchyDescription(final Long id) {
        final int level = getLevel(id);
        final Integer[] hierarchy = new Integer[level + 1];
        int currentLevel = level;
        Long currentId = id;
        Long parent = getParent(currentId);
        while (currentLevel >= 0) {
            hierarchy[currentLevel--] = getChildren(parent).indexOf(currentId);
            currentId = parent;
            parent = getParent(parent);
        }
        return hierarchy;
    }

    private void appendToSb(final StringBuilder sb, final Long id) {
        if (id != null) {
            final TreeNodeInfo<Long> node = getNodeInfo(id);
            final int indent = node.getLevel() * 4;
            final char[] indentString = new char[indent];
            Arrays.fill(indentString, ' ');
            sb.append(indentString);
            sb.append(node.toString());
            sb.append(Arrays.asList(getHierarchyDescription(id)).toString());
            sb.append("\n");
        }
        final List<Long> children = getChildren(id);
        for (final Long child : children) {
            appendToSb(sb, child);
        }
    }

    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();
        appendToSb(sb, null);
        return sb.toString();
    }

    @Override
    public synchronized void clear() {
        allNodes.clear();
        topSentinel.clearChildren();
        internalDataSetChanged();
    }

    @Override
    public void refresh() {
        internalDataSetChanged();
    }

	@Override
	public void addRoot(Long parent, Long root, String rootMessage) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean getNeedUpdateChildrenNode(Long id) {
		final InMemoryTreeNode<Long> node = allNodes.get(id);
		if (node != null)
			return node.getNeedSearchChild();
		
		return false;
	}

	@Override
	public void setNeedUpdateChildrenNode(Long id, boolean need) {
		final InMemoryTreeNode<Long> node = allNodes.get(id);
		if (node != null)
			node.setNeedSearchChild(need);
	}
	
	@Override
    public TreeBuilder<Long> getTreeBuilder() {
    	return builder;
    }

	@Override
	public void setTreeBuilder(TreeBuilder<Long> builder) {
		// TODO Auto-generated method stub
		this.builder = builder;
	}
}
