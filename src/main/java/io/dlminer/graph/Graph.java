package io.dlminer.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class Graph<V> {
	
	// exceptions
	public static final String NULL_LABEL_ERROR = "Cannot create a node with NULL value";
	
	private Map<V, Node<V>> nodeMap;	
	
	public Graph() {
		nodeMap = new HashMap<>();
	}
	
	public Graph(Collection<V> values) {
		nodeMap = new HashMap<>();
		createNodes(values);
	}
	
	
	public Node<V> getNode(V value) {
		return nodeMap.get(value);
	}
	
		
	public Set<V> getLabels() {
		return nodeMap.keySet();
	}
	
	private void addNode(Node<V> node) {		
		nodeMap.put(node.value, node);		
	}
	
	public void createNodes(Collection<V> values) {
		for (V value : values) {			
			addNode(new Node<>(value));
		}
	}
	
	public boolean addChild(V parent, V child) {
		Node<V> parNode = nodeMap.get(parent);
		Node<V> childNode = nodeMap.get(child);
		if (parNode == null || childNode == null) {
			return false;
		}		
		return parNode.addChild(childNode);			
	}
	
	public boolean addChilds(V parent, Set<V> childs) {
		boolean allAdded = true;
		for (V child : childs) {
			if (!addChild(parent, child)) {
				allAdded = false;
			}
		}
		return allAdded;
	}
	
	
	public boolean removeChild(V parent, V child) {
		Node<V> parNode = nodeMap.get(parent);
		Node<V> childNode = nodeMap.get(child);
		if (parNode == null || childNode == null) {
			return false;
		}		
		return parNode.removeChild(childNode);			
	}
	
	
	public boolean removeChildFromAncestors(V parent, V child) {		
		return removeChildFromAncestorsNoRecursion(parent, child);
	}
	
	
		
	private boolean removeChildFromAncestorsNoRecursion(V parent, V child) {
		Node<V> parNode = nodeMap.get(parent);
		Node<V> childNode = nodeMap.get(child);
		if (parNode == null || childNode == null) {
			return false;
		}		
		Set<Node<V>> ancNodes = traverseAncestors(parNode, false);		
		if (ancNodes == null) {
			return false;
		}
		boolean isRemoved = false;
		for (Node<V> ancNode : ancNodes) {
			if (ancNode.removeChild(childNode)) {
				isRemoved = true;
			}
		}
		return isRemoved;
	}
	
	
	
	public Set<Node<V>> getParents(V child) {
		Node<V> childNode = nodeMap.get(child);
		if (childNode == null) {
			return null;
		}
		return childNode.getParents();
	}
	
	
	public Set<Node<V>> getChilds(V parent) {
		Node<V> parNode = nodeMap.get(parent);
		if (parNode == null) {
			return null;
		}
		return parNode.getChilds();
	}
	
	
	public boolean hasChild(V parent, V child) {
		Node<V> parNode = nodeMap.get(parent);
		if (parNode == null) {
			return false;
		}
		return parNode.hasChild(child);
	}
	
	
	public boolean hasDescendant(V ancestor, V descendant) {		
		return hasDescendantNoRecursion(ancestor, descendant);		
	}
	
	
		
	private boolean hasDescendantNoRecursion(V ancestor, V descendant) {
		Node<V> ancNode = nodeMap.get(ancestor);
		if (ancNode == null) {
			return false;
		}
		Set<Node<V>> descNodes = traverseDescendants(ancNode, false);
		if (descNodes == null) {
			return false;
		}
		for (Node<V> descNode : descNodes) {
			if (descNode.value.equals(descendant)) {
				return true;
			}
		}
		return false;
	}
	
	
	public int countAncestors(V value) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return -1;
		}
		return countAncestors(node);
	}
	
	
	
	private int countAncestors(Node<V> node) {
		return countAncestorsNoRecursion(node);
	}
	
			
	private int countAncestorsNoRecursion(Node<V> node) {		
		Set<Node<V>> ancNodes = traverseAncestors(node, false);
		if (ancNodes == null) {
			return 0;
		}
		return ancNodes.size();
	}
	
	
	public int countDescendants(V value) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return -1;
		}
		return countDescendants(node);
	}
		
	
	private int countDescendants(Node<V> node) {			
		return countDescendantsNoRecursion(node);
	}
	

	
	private int countDescendantsNoRecursion(Node<V> node) {		
		Set<Node<V>> descNodes = traverseDescendants(node, false);
		if (descNodes == null) {
			return 0;
		}
		return descNodes.size();
	}
		
	
	
	public void closeCycles() {			
		Set<Node<V>> delSet = new HashSet<>();
		for (Node<V> node : nodeMap.values()) {
			if (!delSet.contains(node)) {
				Set<Node<V>> childNodes = node.getChilds();
				if (childNodes != null) {					
					for (Node<V> childNode : childNodes) {
						Set<Node<V>> grandChilds = childNode.getChilds();
						if (grandChilds != null && grandChilds.contains(node)) {
							// mark for deletion
							delSet.add(childNode);							
						}
					}					
				}
			}
		}
		// process marked nodes
		for (Node<V> node : delSet) {
			// remove childs (incl. node)
			node.removeChilds();						
		}		
	}
	
	
	public Set<Set<V>> getConnectedSets() {
		Set<Set<V>> conSets = new HashSet<>();
		LinkedList<Node<V>> freshNodes = new LinkedList<>(nodeMap.values());
		while (!freshNodes.isEmpty()) {
			Node<V> next = freshNodes.pollFirst();
			Set<V> conSet = new HashSet<V>();
			Set<Node<V>> visitedNodes = traverse(next, true);
			if (visitedNodes == null) {
				conSet.add(next.value);				
			} else {
				for (Node<V> node : visitedNodes) {
					conSet.add(node.value);
				}
				freshNodes.removeAll(visitedNodes);
			}			
			conSets.add(conSet);
		}		
		return conSets;
	}
	
	
	/**
	 * @param node - a starting node
	 * @param dfs - depth-first search
	 * @return visited nodes
	 */
	public Set<Node<V>> traverse(Node<V> node, boolean dfs) {
		if (node.getParents() == null && node.getChilds() == null) {
			return null;
		}
		LinkedList<Node<V>> que = new LinkedList<>();
		que.add(node);
		Set<Node<V>> visited = new HashSet<>();
		while (!que.isEmpty()) {
			Node<V> next = null;
			if (dfs) {
				next = que.pollFirst();
			} else {
				next = que.pollLast();
			}
			visited.add(next);			
			// do not store visited nodes
			Set<Node<V>> childs = next.getChilds();
			if (childs != null) {
				for (Node<V> child : childs) {
					if (!visited.contains(child)) {
						que.add(child);
					}
				}
			}
			Set<Node<V>> parents = next.getParents();
			if (parents != null) {
				for (Node<V> parent : parents) {
					if (!visited.contains(parent)) {
						que.add(parent);
					}
				}
			}
		}
		return visited;
	}
	
	
	public Set<Node<V>> traverseAncestors(Node<V> node, boolean dfs) {
		if (node.parents == null) {
			return null;
		}
		LinkedList<Node<V>> que = new LinkedList<>();
		que.add(node);
		Set<Node<V>> visited = new HashSet<>();
		while (!que.isEmpty()) {
			Node<V> next = null;
			if (dfs) {
				next = que.pollFirst();
			} else {
				next = que.pollLast();
			}
			visited.add(next);			
			// do not store visited nodes			
			Set<Node<V>> parents = next.getParents();
			if (parents != null) {
				for (Node<V> parent : parents) {
					if (!visited.contains(parent)) {
						que.add(parent);
					}
				}
			}
		}
		return visited;
	}
	
	
	public Set<Node<V>> traverseDescendants(Node<V> node, boolean dfs) {
		if (node.childs == null) {
			return null;
		}
		LinkedList<Node<V>> que = new LinkedList<>();
		que.add(node);
		Set<Node<V>> visited = new HashSet<>();
		while (!que.isEmpty()) {
			Node<V> next = null;
			if (dfs) {
				next = que.pollFirst();
			} else {
				next = que.pollLast();
			}
			visited.add(next);			
			// do not store visited nodes			
			Set<Node<V>> childs = next.getChilds();
			if (childs != null) {
				for (Node<V> child : childs) {
					if (!visited.contains(child)) {
						que.add(child);
					}
				}
			}
		}
		return visited;
	}
	
	
	
		
	public V getHighestAncestor(V value, 			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return null;
		}
		return getHighestAncestor(node, entitiesMap, entities).value;
	}
	
	
	
	private Node<V> getHighestAncestor(Node<V> node, 			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {		
		Set<Node<V>> ancestors = traverseAncestors(node, true);
		if (ancestors == null) {
			return node;
		}
		boolean doEntities = (entities != null && entities.length > 0 && entitiesMap != null);
		int maxDescendants = -1;
		Node<V> maxAncestor = null;
		for (Node<V> ancestor : ancestors) {
			boolean hasEntity = false;
			if (doEntities) {
				Set<String> keywords = entitiesMap.get(ancestor.value);			
				for (String entity : entities) {
					if (keywords.contains(entity)) {
						hasEntity = true;
						break;
					}
				}
			}
			if (!doEntities || hasEntity) {
				int nDescendants = countDescendants(ancestor);
				if (maxDescendants < nDescendants) {
					maxDescendants = nDescendants;
					maxAncestor = ancestor;
				}
			}
		}
		if (maxAncestor == null) {
			return node;
		}
		return maxAncestor;
	}
	
	
	
	
	public V getHigherAncestor(V value, 			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return null;
		}
		return getHigherAncestor(node, entitiesMap, entities).value;
	}
	
	
	
	private Node<V> getHigherAncestor(Node<V> node, 			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {		
		if (node.parents == null) {
			return node;
		}
		boolean doEntities = (entities != null && entities.length > 0 && entitiesMap != null);
		int maxDescendants = -1;
		Node<V> maxAncestor = null;
		for (Node<V> ancestor : node.parents) {
			boolean hasEntity = false;
			if (doEntities) {
				Set<String> keywords = entitiesMap.get(ancestor.value);			
				for (String entity : entities) {
					if (keywords.contains(entity)) {
						hasEntity = true;
						break;
					}
				}
			}
			if (!doEntities || hasEntity) {
				int nDescendants = countDescendants(ancestor);
				if (maxDescendants < nDescendants) {
					maxDescendants = nDescendants;
					maxAncestor = ancestor;
				}
			}
		}
		if (maxAncestor == null) {
			return node;
		}
		return maxAncestor;
	}
	
	
	
		
	public V getLowestDescendant(V value,			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return null;
		}
		return getLowestDescendant(node, entitiesMap, entities).value;
	}
	
	
	
	private Node<V> getLowestDescendant(Node<V> node, 			 
			Map<V, Set<String>> entitiesMap, 
			String... entities) {		
		Set<Node<V>> descendants = traverseDescendants(node, true);
		if (descendants == null) {
			return node;
		}
		boolean doEntities = (entities != null && entities.length > 0 && entitiesMap != null);
		int maxAncestors = -1;
		Node<V> maxDescendant = null;
		for (Node<V> descendant : descendants) {
			boolean hasEntity = false;
			if (doEntities) {
				Set<String> keywords = entitiesMap.get(descendant.value);
				for (String entity : entities) {
					if (keywords.contains(entity)) {
						hasEntity = true;
						break;
					}
				}
			}
			if (!doEntities || hasEntity) {
				int nAncestors = countAncestors(descendant);
				if (maxAncestors < nAncestors) {
					maxAncestors = nAncestors;
					maxDescendant = descendant;
				}
			}
		}
		if (maxDescendant == null) {
			return node;
		}
		return maxDescendant;
	}
	
	
	
		
	public V getLowerDescendant(V value,			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {
		Node<V> node = nodeMap.get(value);
		if (node == null) {
			return null;
		}
		return getLowerDescendant(node, entitiesMap, entities).value;
	}
	
	
	
	private Node<V> getLowerDescendant(Node<V> node, 			
			Map<V, Set<String>> entitiesMap, 
			String... entities) {		
		if (node.childs == null) {
			return node;
		}
		boolean doEntities = (entities != null && entities.length > 0 && entitiesMap != null);
		int maxAncestors = -1;
		Node<V> maxDescendant = null;
		for (Node<V> descendant : node.childs) {
			boolean hasEntity = false;
			if (doEntities) {
				Set<String> keywords = entitiesMap.get(descendant.value);			
				for (String entity : entities) {
					if (keywords.contains(entity)) {
						hasEntity = true;
						break;
					}
				}
			}
			if (!doEntities || hasEntity) {
				int nAncestors = countAncestors(descendant);
				if (maxAncestors < nAncestors) {
					maxAncestors = nAncestors;
					maxDescendant = descendant;
				}
			}
		}
		if (maxDescendant == null) {
			return node;
		}
		return maxDescendant;
	}
	
	
	
	public boolean isEmpty() {
		return nodeMap.isEmpty();
	}
	
	

	@Override
	public String toString() {
		String str = "\nGraph:";
		for (V value : nodeMap.keySet()) {
			str += ("\n" + value.toString() + " -> ");
			Set<Node<V>> childs = nodeMap.get(value).getChilds();
			if (childs != null) {
				for (Node<V> node : childs) {
					str += (node.toString() + ", ");
				}
			}
		}
		return str;		
	}

	
}
