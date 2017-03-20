package io.dlminer.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import io.dlminer.ont.LengthMetric;

public class ALCNode extends CNode {
	
	public Set<OWLClassExpression> clabels;
	public Set<OWLClassExpression> dlabels;
	
	
	public ALCNode(Set<OWLClassExpression> clabels,
			Set<OWLClassExpression> dlabels) {
		if (clabels == null || dlabels == null) {
			throw new IllegalArgumentException(Graph.NULL_LABEL_ERROR);
		}
		this.clabels = clabels;
		this.dlabels = dlabels;
	}


    public ALCNode(Set<OWLClassExpression> clabels) {
        if (clabels == null) {
            throw new IllegalArgumentException(Graph.NULL_LABEL_ERROR);
        }
        this.clabels = clabels;
        this.dlabels = new HashSet<>(1);
    }


    @Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ALCNode)) {
			return false;
		}
		ALCNode node = (ALCNode) obj;		
		return isEqualTo(node);
	}
	
	
	
	@Override
	public boolean isMoreSpecificThan(CNode node) {		
		if (node instanceof ALCNode) {
			return isMoreSpecificThanALCNode((ALCNode) node);
		}
		return false;
	}
	
	
	private boolean isMoreSpecificThanALCNode(ALCNode node) {
		// check concepts
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		// check labels
		// (A1 and A2) < A1; A1 < (A1 or A2)
		if (!clabels.containsAll(node.clabels)
				|| !node.dlabels.containsAll(dlabels)) {
			return false;
		}
		// check edges
		LinkedList<CEdge> edges = node.outEdges;
		if (edges == null) {
			return true;
		} 
		if (outEdges == null) {
			return false;
		}
		// check edge labels
		for (CEdge e2 : edges) {
			boolean found = false;
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
						&& e2 instanceof SomeEdge == e1 instanceof SomeEdge
                        && e2 instanceof OnlyEdge == e1 instanceof OnlyEdge) {
					found = true;
					break;					
				}				
			}
			if (!found) {
				return false;
			}
		}
		// check edge successors
		for (CEdge e2 : edges) {
			boolean found = false;
			for (CEdge e1 : outEdges) {
                if (isMoreSpecificThan(e1, e2)) {
                    found = true;
                    break;
                }
			}
			if (!found) {
				return false;
			}
		}					
		return true;
	}




    @Override
	protected int hash(int hash) {
		hash += clabels.hashCode() + dlabels.hashCode();		
		if (outEdges != null) {			
			for (CEdge e : outEdges) {
				hash = e.label.hashCode() + e.object.hash(hash);
				if (e instanceof OnlyEdge) {
					hash += 1;
				}
                if (e instanceof EDataEdge) {
                    hash += 2;
                }
                if (e instanceof GDataEdge) {
                    hash += 3;
                }
                if (e instanceof LDataEdge) {
                    hash += 4;
                }
			}		
		}
		return hash;
	}
	

	

	@Override
	public ALCNode clone() {
		ALCNode node = new ALCNode(new HashSet<>(clabels), new HashSet<>(dlabels));
		if (outEdges != null) {
			for (CEdge e : outEdges) {
                CEdge edge = null;
			    if (e instanceof DataEdge) {
                    DataEdge de = (DataEdge) e;
                    OWLDataPropertyExpression dp = (OWLDataPropertyExpression) de.label;
                    LiteralNode obj = (LiteralNode) de.object;
			        if (de instanceof EDataEdge) {
			            edge = new EDataEdge(node, dp, obj);
                    }
                    if (de instanceof GDataEdge) {
                        edge = new GDataEdge(node, dp, obj);
                    }
                    if (de instanceof LDataEdge) {
                        edge = new LDataEdge(node, dp, obj);
                    }
                } else {
                    ALCNode child = ((ALCNode) e.object).clone();
                    OWLObjectPropertyExpression op = (OWLObjectPropertyExpression) e.label;
                    if (e instanceof SomeEdge) {
                        edge = new SomeEdge(node, op, child);
                    }
                    if (e instanceof OnlyEdge) {
                        edge = new OnlyEdge(node, op, child);
                    }

                }
                if (edge != null) {
                    node.addOutEdge(edge);
                }
			}
		}		
		return node;
	}




    @Override
    protected int countDepth() {
        if (outEdges == null) {
            return 0;
        } else {
            int max = -1;
            for (CEdge e : outEdges) {
                int curr;
                if (e instanceof DataEdge) {
                    curr = 0;
                } else {
                    curr = e.object.countDepth();
                }
                if (max < curr) {
                    max = curr;
                }
            }
            return max+1;
        }
    }


	
	
	@Override
	protected int countLength(int count) {		
		if (clabels.isEmpty() && dlabels.isEmpty()) {
			// count owl:Thing
			count++;
		} else {
			// count conjunctions
			if (clabels.size() > 1) {
				count += clabels.size() - 1;
			}
			// count disjunctions
			if (dlabels.size() > 1) {
				count += dlabels.size() - 1;
			}
			// count the conjunction between them
			if (!clabels.isEmpty() && !dlabels.isEmpty()) {
				count++;
			}
			// count operands
			for (OWLClassExpression expr : clabels) {
				count += LengthMetric.length(expr);
			}
			for (OWLClassExpression expr : dlabels) {
				count += LengthMetric.length(expr);
			}			
		}				
		// count edges
		if (outEdges != null) {
			// count each edge and its conjunction
			count += 2*outEdges.size();
			// owl:Thing's conjunction is not counted
			if (clabels.isEmpty() && dlabels.isEmpty()) {
				count -= 2;
			}
			// count objects
            for (CEdge e : outEdges) {
                // call recursion on each edge
                count = e.object.countLength(count);
            }
        }
		return count;
	}

	
	
	@Override
	protected void buildTopLevelConcept(OWLDataFactory factory) {
		// gather expressions
		Set<OWLClassExpression> exprs = null;
		// process labels
		if (!clabels.isEmpty()) {
			exprs = new HashSet<>();
			exprs.addAll(clabels);
		}
		if (!dlabels.isEmpty()) {
			if (exprs == null) {
				exprs = new HashSet<>();
			}
			if (dlabels.size() == 1) {
				exprs.addAll(dlabels);
			} else {
				exprs.add(factory.getOWLObjectUnionOf(dlabels));
			}		
		}
		// process edges
		if (outEdges != null) {
			if (exprs == null) {
				exprs = new HashSet<>();
			}
			for (CEdge e : outEdges) {
			    // process data properties
                if (e instanceof DataEdge) {
                    DataEdge de = (DataEdge) e;
                    OWLDataPropertyExpression prop = (OWLDataPropertyExpression) de.label;
                    LiteralNode n = (LiteralNode) de.object;
                    OWLLiteral lit = n.literal;
                    Double val = DataEdge.parseNumber(lit);
                    if (val == null) {
                        throw new IllegalArgumentException(WRONG_LITERAL_TYPE_ERROR);
                    }
                    OWLDataRange range;
                    if (e instanceof EDataEdge) {
                        range = factory.getOWLDataOneOf(lit);
                    } else if (e instanceof GDataEdge) {
                        range = factory.getOWLDatatypeMinInclusiveRestriction(val);
                    } else if (e instanceof LDataEdge) {
                        range = factory.getOWLDatatypeMaxInclusiveRestriction(val);
                    } else {
                        throw new IllegalArgumentException(WRONG_EDGE_TYPE_ERROR);
                    }
                    OWLClassExpression expr = factory.getOWLDataSomeValuesFrom(prop, range);
                    exprs.add(expr);
                }
                // process object properties
                else {
                    // must go bottom-up
                    if (e.object.concept == null) {
                        throw new IllegalArgumentException(NULL_CONCEPT_ERROR);
                    } else {
                        OWLObjectPropertyExpression prop = (OWLObjectPropertyExpression) e.label;
                        OWLClassExpression expr;
                        if (e instanceof SomeEdge) {
                            expr = factory.getOWLObjectSomeValuesFrom(prop, e.object.concept);
                        } else if (e instanceof OnlyEdge) {
                            expr = factory.getOWLObjectAllValuesFrom(prop, e.object.concept);
                        } else {
                            throw new IllegalArgumentException(WRONG_EDGE_TYPE_ERROR);
                        }
                        exprs.add(expr);
                    }
                }
			}
		}				
		// process expressions				
		if (exprs == null || exprs.isEmpty()) {
			concept = factory.getOWLThing();
		} else if (exprs.size() == 1) {
			for (OWLClassExpression c : exprs) {
				concept = c;
				break;
			}
		} else {
			concept = factory.getOWLObjectIntersectionOf(exprs);
		}
	}



	@Override
	public boolean isOWLThing() {
		return clabels.isEmpty() && dlabels.isEmpty() && isLeaf();
	}



	public boolean containsUniversal() {
		if (outEdges == null) {
			return false;
		}
		for (CEdge e : outEdges) {
			if (e instanceof OnlyEdge) {
				return true;
			}
		}		
		return false;
	}



	public boolean containsExistential() {
		if (outEdges == null) {
			return false;
		}
		for (CEdge e : outEdges) {
			if (e instanceof SomeEdge) {
				return true;
			}
		}		
		return false;
	}




    // similarity and normalisation methods

    public double similarity(ALCNode node) {
        return (double)(getSimilarityWith(node, 0) + node.getSimilarityWith(this, 0))/(length() + node.length());
    }


    // recursion!
    private int getSimilarityWith(ALCNode node, int similarity) {
        for (OWLClassExpression expr1 : node.clabels) {
            for (OWLClassExpression expr2 : clabels) {
                if (expr1.equals(expr2)) {
                    similarity++;
                }
            }
        }
        // find the most similar node
        if (outEdges != null && node.outEdges != null) {
            for (CEdge e1 : outEdges) {
                int maxSim = -1;
                for (CEdge e2 : node.outEdges) {
                    if (e1.label.equals(e2.label)) {
                        int sim = ((ALCNode)e1.object).getSimilarityWith((ALCNode)e2.object, similarity);
                        if (sim > maxSim) {
                            maxSim = sim;
                        }
                    }
                }
                if (maxSim > -1) {
                    similarity = maxSim + 1;
                }
            }
        }
        return similarity;
    }




    public ALCNode normalise() {
        removeRedundantSuccessors();
        return this;
    }


    // recursion!
    private void removeRedundantSuccessors() {
        if (outEdges != null) {
            Set<CEdge> duplicates = new HashSet<>();
            for (CEdge e1 : outEdges) {
                for (CEdge e2 : outEdges) {
                    if (!e1.equals(e2)
                            && e1 instanceof SomeEdge == e2 instanceof SomeEdge
                            && e1 instanceof OnlyEdge == e2 instanceof OnlyEdge
                            && e1.label.equals(e2.label)
                            && !duplicates.contains(e1)
                            && isMoreSpecificThan(e1, e2)) {
                        duplicates.add(e2);
                    }
                }
            }
            removeOutEdges(duplicates);
            for (CEdge e : outEdges) {
                if (!(e instanceof DataEdge)) {
                    ((ALCNode) e.object).removeRedundantSuccessors();
                }
            }
        }
    }



}
