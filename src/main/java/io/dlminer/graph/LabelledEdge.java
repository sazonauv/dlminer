package io.dlminer.graph;

public class LabelledEdge<C, R> {
	
	public R label;
	
	public LabelledNode<C, R> subject;
	public LabelledNode<C, R> object;
	
	
	public LabelledEdge(LabelledNode<C, R> subject, R label,
			LabelledNode<C, R> object) {
		this.label = label;
		this.subject = subject;
		this.object = object;
	}
	
			
}
