/**
 * 
 */
package io.dlminer.sort;

import java.util.Comparator;

import io.dlminer.graph.CNode;

/**
 * @author slava
 *
 */
public class NodeLengthComparator 
extends AbstractComparator
implements Comparator<CNode> {
	
	public NodeLengthComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(CNode node1, CNode node2) {		
		if (order.equals(SortingOrder.ASC)) {
			return Integer.compare(node1.length(), node2.length());
		}
		return - Integer.compare(node1.length(), node2.length());		
	}

}
