package io.dlminer.ont;

import io.dlminer.graph.ALCNode;
import io.dlminer.graph.Expansion;
import io.dlminer.print.Out;

import java.util.*;

import io.dlminer.refine.ALCOperator;
import org.semanticweb.owlapi.model.*;

public class InstanceChecker {

	// internal maps for instance checking
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> classInstanceMap;
    private Map<OWLClassExpression, List<Expansion>> classExpansionMap;
    private Map<OWLObjectPropertyExpression, Map<OWLNamedIndividual, Set<OWLNamedIndividual>>> propInstanceMap;
	// clusters of identical trees
	private Map<Expansion, List<Expansion>> expansionClusterMap;

	private OWLDataFactory factory;

		
	public InstanceChecker(ALCOperator operator, OntologyHandler handler) {
		this.classInstanceMap = operator.getClassInstanceMap();
        classExpansionMap = new HashMap<>();
        initPropMaps(handler);
        factory = handler.getDataFactory();
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


    public List<Expansion> getInstances(ALCNode node) {
        return getInstances(node, expansionClusterMap.keySet());
    }


	public List<Expansion> getInstances(ALCNode node,
			Collection<Expansion> suspects) {
	    if (node.isOWLThing()) {
	        return getInstancesOfOWLThing();
        }
        if (node.isAtomic()) {
	        return getInstancesOfAtomicNode(node);
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
        if (classExpansionMap.containsKey(atom)) {
	        return classExpansionMap.get(atom);
        }
        Set<OWLNamedIndividual> individuals = classInstanceMap.get(atom);
        List<Expansion> instances = new LinkedList<>();
	    for (Expansion center : expansionClusterMap.keySet()) {
	        if (individuals.contains(center.individual)) {
                instances.add(center);
            }
        }
        classExpansionMap.put(atom, instances);
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
		for (List<Expansion> cluster : localExpClusterMap.values()) {
			expansionClusterMap.put(cluster.get(0), cluster);
		}		
		Out.p(expansionClusterMap.size() + " clusters for " + expansions.size() + " expansions");
	}
	
	
	
	public Set<Expansion> getClusterCenters() {
		return expansionClusterMap.keySet();
	}
	

	
	public void addInstancesForClusters(Map<ALCNode, List<Expansion>> nodeClusterMap, 
			Map<ALCNode, List<Expansion>> nodeExpansionMap) {
		// gather all instances		
		for (ALCNode expr : nodeClusterMap.keySet()) {
			if (nodeExpansionMap.containsKey(expr)) {
				continue;
			}
			List<Expansion> allInstances = new LinkedList<>();
			List<Expansion> instances = nodeClusterMap.get(expr);
			for (Expansion inst : instances) {
				allInstances.addAll(expansionClusterMap.get(inst));
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
