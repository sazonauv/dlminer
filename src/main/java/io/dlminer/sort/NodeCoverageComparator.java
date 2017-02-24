package io.dlminer.sort;

import io.dlminer.graph.CNode;
import java.util.Comparator;

/**
 * @author Slava Sazonau
 * compares concepts by the number of instances
 */
public class NodeCoverageComparator 
extends AbstractComparator
implements Comparator<CNode> {
	
	
	public NodeCoverageComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(CNode node1, CNode node2) {		
		if (order.equals(SortingOrder.ASC)) {
			return node1.coverage.compareTo(node2.coverage);
		}
		return - node1.coverage.compareTo(node2.coverage);		
	}

}
