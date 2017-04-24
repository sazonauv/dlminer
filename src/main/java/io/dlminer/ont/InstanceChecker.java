package io.dlminer.ont;

import io.dlminer.graph.*;
import io.dlminer.main.DLMinerOutputI;
import io.dlminer.print.Out;

import java.util.*;

import io.dlminer.refine.ALCOperator;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class InstanceChecker {

	// internal maps for instance checking
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> classInstanceMap;
    private Map<ALCNode, List<Expansion>> nodeExpansionMap;
    private Map<ALCNode, Set<OWLNamedIndividual>> nodeInstanceMap;
    private Map<OWLObjectPropertyExpression, Map<OWLNamedIndividual, Set<OWLNamedIndividual>>> propInstanceMap;


    // data properties
    private Map<OWLDataProperty, List<Double>> dataPropertyThresholdsMap;
    private Map<OWLDataProperty, Map<Double, Set<OWLNamedIndividual>>> dataPropertyInstancesMap;
    private Map<OWLDataProperty, Integer> dataPropertyStepMap;

    // clusters of identical trees
	private Map<Expansion, Set<OWLNamedIndividual>> expansionClusterMap;
    private Map<OWLNamedIndividual, Expansion> individualClusterMap;

	private OWLDataFactory factory;
	private OntologyHandler handler;
	private OWLReasoner reasoner;

		
	public InstanceChecker(ALCOperator operator, OntologyHandler handler) {
		classInstanceMap = operator.getClassInstanceMap();
        nodeExpansionMap = new HashMap<>();
        nodeInstanceMap = new HashMap<>();
        dataPropertyThresholdsMap = operator.getDataPropertyThresholdsMap();
        dataPropertyInstancesMap = operator.getDataPropertyInstancesMap();
        dataPropertyStepMap = operator.getDataPropertyStepMap();
        this.handler = handler;
        factory = handler.getDataFactory();
        reasoner = operator.getReasoner();
        initPropMaps();
	}



    private void initPropMaps() {
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




    public Set<OWLNamedIndividual> getInstancesByReasoner(ALCNode node) {
	    if (nodeInstanceMap.containsKey(node)) {
	        return nodeInstanceMap.get(node);
        }
        Set<OWLNamedIndividual> instances;
        try {
            instances = reasoner.getInstances(node.getConcept(), false).getFlattened();
        } catch (Exception e) {
            Out.p(e + DLMinerOutputI.CONCEPT_BUILDING_ERROR);
            instances = new HashSet<>(1);
            nodeInstanceMap.put(node, instances);
        }
        if (instances == null) {
            instances = new HashSet<>(1);
        }
        nodeInstanceMap.put(node, instances);
        return instances;
    }



    public Set<OWLNamedIndividual> getInstances(ALCNode node) {
        return getInstances(node, ALCNode.OWL_THING);
    }


	public Set<OWLNamedIndividual> getInstances(ALCNode node, ALCNode general) {
        if (nodeInstanceMap.containsKey(node)) {
            return nodeInstanceMap.get(node);
        }
	    if (node.isOWLThing()) {
	        return getInstancesOfOWLThing();
        }
        if (node.isAtomic()) {
	        return getInstancesOfAtomicNode(node);
        }
        return getInstancesFromSuspects(node, general);
	}



    private Set<OWLNamedIndividual> getInstancesOfOWLThing() {
	    ALCNode thing = ALCNode.OWL_THING;
        if (nodeInstanceMap.containsKey(thing)) {
            return nodeInstanceMap.get(thing);
        }
        List<Expansion> expansions = new LinkedList<>(expansionClusterMap.keySet());
        nodeExpansionMap.put(thing, expansions);
        Set<OWLNamedIndividual> individuals = handler.getIndividuals();
        nodeInstanceMap.put(thing, individuals);
        return individuals;
    }



    private Set<OWLNamedIndividual> getInstancesOfAtomicNode(ALCNode node) {
	    OWLClassExpression atom = node.getConcept();
        Set<OWLNamedIndividual> individuals = classInstanceMap.get(atom);
        nodeInstanceMap.put(node, individuals);
        List<Expansion> expansions = new LinkedList<>();
        if (individuals != null) {
            for (OWLNamedIndividual ind : individuals) {
                Expansion exp = individualClusterMap.get(ind);
                if (exp != null) {
                    expansions.add(exp);
                }
            }
        }
        nodeExpansionMap.put(node, expansions);
        return individuals;
    }


    private Set<OWLNamedIndividual> getInstancesOfDataValueRestriction(ALCNode node, ALCNode general) {
        OWLClassExpression dataRestriction = node.getConcept();
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
        List<Expansion> expansions = new LinkedList<>(nodeExpansionMap.get(general));
        Set<OWLNamedIndividual> individuals = new HashSet<>(nodeInstanceMap.get(general));
        for (int i=start; i<=end; i++) {
            Set<OWLNamedIndividual> removals = instMap.get(thresholds.get(i));
            individuals.removeAll(removals);
            for (OWLNamedIndividual rem : removals) {
                Expansion exp = individualClusterMap.get(rem);
                if (exp != null) {
                    expansions.remove(exp);
                }
            }
        }
        nodeInstanceMap.put(node, individuals);
        nodeExpansionMap.put(node, expansions);
        return individuals;
    }



    private Set<OWLNamedIndividual> getInstancesFromSuspects(ALCNode node, ALCNode general) {
        if (node.isDataValueRestriction()) {
            return getInstancesOfDataValueRestriction(node, general);
        }
        List<Expansion> suspects = nodeExpansionMap.get(general);
        List<Expansion> expansions = new LinkedList<>();
        Set<OWLNamedIndividual> individuals = new HashSet<>();
        for (Expansion suspect : suspects) {
            if (isInstanceOf(suspect, node)) {
                expansions.add(suspect);
                individuals.addAll(expansionClusterMap.get(suspect));
            }
        }
        nodeInstanceMap.put(node, individuals);
        nodeExpansionMap.put(node, expansions);
        return individuals;
    }




    // if it is equivalent or more specific,
	// then it is an instance
	private boolean isInstanceOf(Expansion suspect, ALCNode node) {
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
		    Set<OWLNamedIndividual> clusterInds = new HashSet<>();
		    for (Expansion clusterExp : cluster) {
                clusterInds.add(clusterExp.individual);
            }
		    Expansion first = cluster.get(0);
			expansionClusterMap.put(first, clusterInds);
            individualClusterMap.put(first.individual, first);
		}		
		Out.p(expansionClusterMap.size() + " clusters for " + expansions.size() + " expansions");
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


}
