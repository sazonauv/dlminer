package io.dlminer.ont;

import io.dlminer.graph.*;
import io.dlminer.print.Out;

import java.util.*;

import io.dlminer.refine.ALCOperator;
import org.semanticweb.owlapi.model.*;

public class InstanceChecker {

	// internal maps for instance checking
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> classInstanceMap;
    private Map<OWLClassExpression, List<Expansion>> classExpansionMap;
    private Map<OWLObjectPropertyExpression, Map<OWLNamedIndividual, Set<OWLNamedIndividual>>> propInstanceMap;

    // data properties
    private Map<OWLDataProperty, List<Double>> dataPropertyThresholdsMap;
    private Map<OWLDataProperty, Map<Double, Set<OWLNamedIndividual>>> dataPropertyInstancesMap;
    private Map<OWLDataProperty, Integer> dataPropertyStepMap;

    // clusters of identical trees
	private Map<Expansion, List<Expansion>> expansionClusterMap;
    private Map<OWLNamedIndividual, Expansion> individualClusterMap;

	private OWLDataFactory factory;

		
	public InstanceChecker(ALCOperator operator, OntologyHandler handler) {
		classInstanceMap = operator.getClassInstanceMap();
        classExpansionMap = new HashMap<>();
        dataPropertyThresholdsMap = operator.getDataPropertyThresholdsMap();
        dataPropertyInstancesMap = operator.getDataPropertyInstancesMap();
        dataPropertyStepMap = operator.getDataPropertyStepMap();
        factory = handler.getDataFactory();
        initPropMaps(handler);
	}



    private void initPropMaps(OntologyHandler handler) {
        Set<OWLObjectPropertyAssertionAxiom> propAxioms = handler.getObjectPropertyAssertions();
        propInstanceMap = new HashMap<>();
        // fill individuals maps
        for (OWLObjectPropertyAssertionAxiom ax : propAxioms) {
            OWLObjectPropertyExpression prop = ax.getProperty();
            Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap =
                    propInstanceMap.get(prop);
            if (indToIndMap == null) {
                indToIndMap = new HashMap<>();
                propInstanceMap.put(prop, indToIndMap);
            }
            OWLIndividual anonymSubj = ax.getSubject();
            OWLIndividual anonymObj = ax.getObject();
            if (anonymSubj.isNamed() && anonymObj.isNamed()) {
                OWLNamedIndividual subj = anonymSubj.asOWLNamedIndividual();
                OWLNamedIndividual obj = anonymObj.asOWLNamedIndividual();
                Set<OWLNamedIndividual> objs = indToIndMap.get(subj);
                if (objs == null) {
                    objs = new HashSet<>(2);
                    indToIndMap.put(subj, objs);
                }
                objs.add(obj);
            }
        }
    }



	public Set<OWLNamedIndividual> getPropertyChainValues(
			OWLNamedIndividual subj, OWLObjectPropertyChain chain) {
		Set<OWLNamedIndividual> values = null;
		List<OWLObjectPropertyExpression> roles = chain.getPropertyExpressions();
		// two-step role chains
		if (subj != null) {
			Set<OWLNamedIndividual> middles =
					getObjectPropertyValues(subj, roles.get(0));
			if (middles != null) {
				for (OWLNamedIndividual middle : middles) {
					if (middle != null) {
						Set<OWLNamedIndividual> objs =
								getObjectPropertyValues(middle, roles.get(1));
						if (objs != null && !objs.isEmpty()) {
							if (values == null) {
								values = new HashSet<>();
							}
							values.addAll(objs);
						}
					}
				}
			}
		}
		return values;
	}


	public Set<OWLNamedIndividual> getObjectPropertyValues(
			OWLNamedIndividual subj, OWLObjectPropertyExpression expr) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> instMap = propInstanceMap.get(expr);
		if (instMap == null) {
			return null;
		}
		return instMap.get(subj);
	}



	public Set<OWLNamedIndividual> getObjectPropertySubjects(OWLObjectPropertyExpression expr) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> instMap = propInstanceMap.get(expr);
		if (instMap == null) {
			return null;
		}
		return instMap.keySet();
	}


    public Set<OWLNamedIndividual> getInstances(OWLClassExpression concept) {
	    return classInstanceMap.get(concept);
    }



    public List<Expansion> getInstances(ALCNode node) {
        return getInstances(node, expansionClusterMap.keySet());
    }


	public List<Expansion> getInstances(ALCNode node,
			Collection<Expansion> suspects) {
	    if (suspects == null || suspects.isEmpty()) {
	        return null;
        }
	    if (node.isOWLThing()) {
	        return getInstancesOfOWLThing();
        }
        if (node.isAtomic()) {
	        return getInstancesOfAtomicNode(node);
        }
        if (node.isDataValueRestriction()) {
	        return getInstancesOfDataValueRestriction(node, suspects);
        }
		List<Expansion> instances = null;
		for (Expansion suspect : suspects) {			
			if (isInstanceOf(suspect, node)) {
				if (instances == null) {
					instances = new LinkedList<>();
				}
				instances.add(suspect);
			}
		}		
		return instances;
	}



    private List<Expansion> getInstancesOfOWLThing() {
	    OWLClass thing = factory.getOWLThing();
        if (classExpansionMap.containsKey(thing)) {
            return classExpansionMap.get(thing);
        }
        List<Expansion> instances = new LinkedList<>(expansionClusterMap.keySet());
        classExpansionMap.put(thing, instances);
        return instances;
    }



    private List<Expansion> getInstancesOfAtomicNode(ALCNode node) {
	    OWLClassExpression atom = null;
	    for (OWLClassExpression label : node.clabels) {
	        atom = label;
	        break;
        }
        if (atom == null) {
            return getInstancesOfOWLThing();
        }
        if (classExpansionMap.containsKey(atom)) {
	        return classExpansionMap.get(atom);
        }
        Set<OWLNamedIndividual> individuals = classInstanceMap.get(atom);
        List<Expansion> instances = new LinkedList<>();
        if (individuals != null) {
            for (OWLNamedIndividual ind : individuals) {
                Expansion exp = individualClusterMap.get(ind);
                if (exp != null) {
                    instances.add(exp);
                }
            }
        }
        classExpansionMap.put(atom, instances);
        return instances;
    }


    private List<Expansion> getInstancesOfDataValueRestriction(ALCNode node, Collection<Expansion> suspects) {
        OWLClassExpression dataRestriction = node.getConcept();
        if (classExpansionMap.containsKey(dataRestriction)) {
            return classExpansionMap.get(dataRestriction);
        }
        DataEdge de = null;
        for (CEdge e : node.getOutEdges()) {
            de = (DataEdge) e;
        }
        // assuming that most suspects are in fact instances
        List<Double> thresholds = dataPropertyThresholdsMap.get(de.label);
        Map<Double, Set<OWLNamedIndividual>> instMap = dataPropertyInstancesMap.get(de.label);
        double value = ((NumericNode) de.object).value;
        int step = dataPropertyStepMap.get(de.label);
        int index = thresholds.indexOf(value);
        int prevIndex;
        int start = -1;
        int end = -1;
        if (de instanceof GDataEdge) {
            // first
            if (index == 0) {
                return getInstancesOfOWLThing();
            }
            // refinements increase index
            prevIndex = index - step;
            prevIndex = (prevIndex < 0) ? 0 : prevIndex;
            start = prevIndex;
            end = index - 1;
        }
        if (de instanceof LDataEdge) {
            // last
            if (index == thresholds.size()-1) {
                return getInstancesOfOWLThing();
            }
            // refinements decrease index
            prevIndex = index + step;
            prevIndex = (prevIndex >= thresholds.size()) ? thresholds.size()-1 : prevIndex;
            start = index + 1;
            end = prevIndex;
        }
        // remove non-instances
        List<Expansion> instances = new LinkedList<>(suspects);
        for (int i=start; i<=end; i++) {
            Set<OWLNamedIndividual> removals = instMap.get(thresholds.get(i));
            for (OWLNamedIndividual rem : removals) {
                Expansion exp = individualClusterMap.get(rem);
                if (exp != null) {
                    instances.remove(exp);
                }
            }
        }
        classExpansionMap.put(dataRestriction, instances);
        return instances;
    }


    // if it is equivalent or more specific,
	// then it is an instance
	private static boolean isInstanceOf(Expansion suspect, ALCNode node) {		
		return suspect.isMoreSpecificThan(node);
	}



	
	public void clusterExpansions(List<Expansion> expansions) {
		Map<Expansion, List<Expansion>> localExpClusterMap = new HashMap<>();
		for (Expansion exp : expansions) {
			List<Expansion> cluster = localExpClusterMap.get(exp);
			if (cluster == null) {
				cluster = new LinkedList<>();				
				localExpClusterMap.put(exp, cluster);
			}
			cluster.add(exp);			
		}		
		// find unique expansions
		expansionClusterMap = new HashMap<>();
		individualClusterMap = new HashMap<>();
		for (List<Expansion> cluster : localExpClusterMap.values()) {
		    Expansion first = cluster.get(0);
			expansionClusterMap.put(first, cluster);
            individualClusterMap.put(first.individual, first);
		}		
		Out.p(expansionClusterMap.size() + " clusters for " + expansions.size() + " expansions");
	}
	
	


	
	public void addInstancesForClusters(Map<ALCNode, List<Expansion>> nodeClusterMap, 
			Map<ALCNode, List<Expansion>> nodeExpansionMap) {
		// gather all instances
        int count = 0;
		for (ALCNode expr : nodeClusterMap.keySet()) {
            // debug
            if (++count % 100 == 0) {
                Out.p(count + "/" + nodeClusterMap.keySet().size()
                        + " concepts are assigned instances as nodes");
            }
			if (nodeExpansionMap.containsKey(expr)) {
				continue;
			}
            List<Expansion> instances = nodeClusterMap.get(expr);
			List<Expansion> allInstances = new LinkedList<>();
			if (instances != null) {
                for (Expansion inst : instances) {
                    allInstances.addAll(expansionClusterMap.get(inst));
                }
            }
			nodeExpansionMap.put(expr, allInstances);
		}
	}



	public int countAllInstances(List<Expansion> instances) {
		if (instances == null) {
			return 0;
		}
		int count = 0;
		for (Expansion inst : instances) {
			count += expansionClusterMap.get(inst).size();
		}		
		return count;
	}



	public int countIntersection(List<Expansion> insts1, List<Expansion> insts2) {
		if (insts1 == null || insts2 == null) {
			return 0;
		}		
		int count = 0;
		if (insts1.size() <= insts2.size()) {
			for (Expansion inst1 : insts1) {
				if (insts2.contains(inst1)) {
					count += expansionClusterMap.get(inst1).size();
				}
			}
		} else {
			for (Expansion inst2 : insts2) {
				if (insts1.contains(inst2)) {
					count += expansionClusterMap.get(inst2).size();
				}
			}
		}		
		return count;
	}


}
