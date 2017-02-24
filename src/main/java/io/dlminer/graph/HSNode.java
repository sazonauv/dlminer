package io.dlminer.graph;

import java.util.HashSet;
import java.util.Set;

public class HSNode<V> {
	
	public V value;
	public V path;
	private Set<HSNode<V>> childs;
	private HSNode<V> parent;	
	
	
	public HSNode(V value, V path) {		
		this.value = value;
		this.path = path;
		init();
	}
	
	private void init() {
		childs = null;
		parent = null;
	}
	
	public boolean addChilds(Set<HSNode<V>> nodes) {
		if (nodes != null) {
			boolean allAdded = true;
			for (HSNode<V> node : nodes) {
				if (!addChild(node)) {
					allAdded = false;
				}
			}
			return allAdded;
		} else {
			return false;
		}
	}
	
	public boolean addParents(Set<HSNode<V>> nodes) {
		if (nodes != null) {
			boolean allAdded = true;
			for (HSNode<V> node : nodes) {
				if (!node.addChild(this)) {
					allAdded = false;
				}
			}
			return allAdded;
		} else {
			return false;
		}
	}
	
	public boolean addChild(HSNode<V> node) {
		if (node != null) {
			if (childs == null) {
				childs = new HashSet<>();
			}
			node.addParent(this);
			return childs.add(node);
		} else {
			return false;
		}
	}
	
	private void addParent(HSNode<V> node) {		
		parent = node;
	}
	
	public boolean removeChilds() {
		if (childs != null) {
			boolean allRemoved = true;
			// to avoid the concurrent modification
			Set<HSNode<V>> backup = new HashSet<>(childs);
			for (HSNode<V> node : backup) {
				if (!removeChild(node)) {
					allRemoved = false;
				}
			}
			return allRemoved;
		} else {
			return false;
		}
	}
	
	public boolean removeChild(HSNode<V> node) {
		if (node != null && childs != null) {
			node.removeParent();
			return childs.remove(node);
		} else {
			return false;
		}
	}

	private void removeParent() {
		parent = null;
	}

	public Set<HSNode<V>> getChilds() {
		return childs;
	}

	public HSNode<V> getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return (value.toString() + " via " + path.toString());
	}	
	

}
