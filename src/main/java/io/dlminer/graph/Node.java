package io.dlminer.graph;

import java.util.HashSet;
import java.util.Set;

public class Node<V> {
	
	public V value;
	protected Set<Node<V>> childs;
	protected Set<Node<V>> parents;	
	
	
	public Node(V value) {
		if (value == null) {
			throw new IllegalArgumentException(Graph.NULL_LABEL_ERROR);
		}
		this.value = value;
		init();
	}
	
	private void init() {
		childs = null;
		parents = null;
	}
	
	public boolean addChilds(Set<Node<V>> nodes) {
		if (nodes != null) {
			boolean allAdded = true;
			for (Node<V> node : nodes) {
				if (!addChild(node)) {
					allAdded = false;
				}
			}
			return allAdded;
		}
		return false;		
	}
	
	public boolean addParents(Set<Node<V>> nodes) {
		if (nodes != null) {
			boolean allAdded = true;
			for (Node<V> node : nodes) {
				if (!node.addChild(this)) {
					allAdded = false;
				}
			}
			return allAdded;
		} 
		return false;		
	}
	
	public boolean addChild(Node<V> node) {
		if (node != null) {
			if (childs == null) {
				childs = new HashSet<>();
			}
			return childs.add(node) && node.addParent(this);
		}
		return false;		
	}
	
	private boolean addParent(Node<V> node) {
		if (parents == null) {
			parents = new HashSet<>();
		}
		return parents.add(node);
	}
	
	public boolean removeChilds() {
		if (childs != null) {
			boolean allRemoved = true;
			// to avoid the concurrent modification
			Set<Node<V>> backup = new HashSet<>(childs);
			for (Node<V> node : backup) {
				if (!removeChild(node)) {
					allRemoved = false;
				}
			}
			return allRemoved;
		}
		return false;		
	}
	
	public boolean removeChild(Node<V> node) {
		if (node != null && childs != null) {
			return childs.remove(node) && node.removeParent(this);
		}
		return false;		
	}

	private boolean removeParent(Node<V> node) {
		if (parents != null) {
			return parents.remove(node);
		} 
		return false;		
	}

	public Set<Node<V>> getChilds() {
		return childs;
	}

	public Set<Node<V>> getParents() {
		return parents;
	}
	
	public boolean hasChild(V child) {
		if (child == null || childs == null) {
			return false;
		}		
		for (Node<V> cnode : childs) {
			if (cnode.value.equals(child)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean hasParent(V parent) {
		if (parent == null || parents == null) {
			return false;
		}		
		for (Node<V> pnode : parents) {
			if (pnode.value.equals(parent)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean hasDescendant(Node<V> descNode) {
		if (childs == null) {
			return false;
		}
		if (childs.contains(descNode)) {
			return true;
		}
		for (Node<V> childNode : childs) {
			if (childNode.hasDescendant(descNode)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean removeChildFromAncestors(Node<V> child) {
		if (parents == null) {
			return false;
		}		
		for (Node<V> parent : parents) {
			if (parent.removeChild(child)) {
				return true;
			}
		}		
		for (Node<V> parent : parents) {
			if (parent.removeChildFromAncestors(child)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public int countAncestors(int count) {
		if (parents == null) {
			return count;
		}
		count += parents.size();
		for (Node<V> parent : parents) {
			count = parent.countAncestors(count);
		}		
		return count;
	}	
	
	
	
	public int countDescendants(int count) {
		if (childs == null) {
			return count;
		}
		count += childs.size();
		for (Node<V> child : childs) {
			count = child.countAncestors(count);
		}		
		return count;
	}
	
	
	

	@Override
	public String toString() {
		return value.toString();
	}

	

	
	
}
