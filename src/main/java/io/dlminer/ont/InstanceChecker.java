package io.dlminer.ont;

import io.dlminer.graph.SomeEdge;
import io.dlminer.graph.ALCNode;
import io.dlminer.graph.CEdge;
import io.dlminer.graph.CNode;
import io.dlminer.graph.ELNode;
import io.dlminer.graph.Expansion;
import io.dlminer.graph.OnlyEdge;
import io.dlminer.print.Out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class InstanceChecker {

	// internal maps for instance checking
	private Map<OWLNamedIndividual, Set<OWLClassExpression>> indClasMap;
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> clasIndMap;
	private Map<OWLObjectPropertyExpression, Map<OWLNamedIndividual, Set<OWLNamedIndividual>>> propIndMap;	

	// clusters of identical individuals
	private Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indClusterMap0;
	private Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indClusterMap1;
	private Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indClusterMap2;
	// clusters of identical trees
	private Map<Expansion, List<Expansion>> expClusterMap;
	private Map<Expansion, LinkedList<Expansion>> expOrderMap;
	
	private OntologyHandler handler;
	
	
	public InstanceChecker(OntologyHandler handler, int roleDepth) {
		this.handler = handler;
		if (handler != null) {
			init();
			clusterIndividuals(roleDepth);
		} else {
			throw new IllegalArgumentException("Ontology is empty!");
		}
	}
	
		
		
	public InstanceChecker(OntologyHandler handler) {
		this.handler = handler;
		if (handler != null) {					
			init();
		} else {
			throw new IllegalArgumentException("Ontology is empty!");
		}
	}
		
			

	private void init() {		
		if (!handler.hasAxiomVisitor()) {
			handler.initAxiomVisitor();
		}
		Set<OWLClassAssertionAxiom> clasAxioms = handler.getClassAssertions();
		Set<OWLObjectPropertyAssertionAxiom> propAxioms = handler.getObjectPropertyAssertions();
		if (clasAxioms != null && !clasAxioms.isEmpty()) {
			initClasMaps(clasAxioms);
		}
		if (propAxioms != null && !propAxioms.isEmpty()) {
			initPropMaps(propAxioms);
		}		
	}
	
	
	
	private void init(int roleDepth) {
		init();
		clusterIndividuals(roleDepth);
	}
	
	

	private void initClasMaps(Set<OWLClassAssertionAxiom> clasAxioms) {
		indClasMap = new HashMap<>();
		clasIndMap = new HashMap<>();
		OWLDataFactory factory = handler.getDataFactory();
		OWLClass owlThing = factory.getOWLThing();		
		Set<OWLNamedIndividual> inds = handler.getIndividuals();
		clasIndMap.put(owlThing, inds);
		for (OWLNamedIndividual ind : inds) {
			Set<OWLClassExpression> indCls = new HashSet<>(2);
			indCls.add(owlThing);
			indClasMap.put(ind, indCls);
		}
		for (OWLClassAssertionAxiom ax : clasAxioms) {
			if (ax.getIndividual().isAnonymous()) {
				continue;
			}
			OWLNamedIndividual ind = ax.getIndividual().asOWLNamedIndividual();
			OWLClassExpression cl = ax.getClassExpression();
			// ind to class
			Set<OWLClassExpression> indCls = indClasMap.get(ind);
			if (indCls == null) {
				indCls = new HashSet<>(2);
				indClasMap.put(ind, indCls);
			}
			indCls.add(cl);			
			// class to ind
			Set<OWLNamedIndividual> clInds = clasIndMap.get(cl);
			if (clInds == null) {
				clInds = new HashSet<>(2);
				clasIndMap.put(cl, clInds);
			}
			clInds.add(ind);
		}		
	}
	
	private void initPropMaps(Set<OWLObjectPropertyAssertionAxiom> propAxioms) {
		propIndMap = new HashMap<>();		
		// fill individuals maps
		for (OWLObjectPropertyAssertionAxiom ax : propAxioms) {
			OWLObjectPropertyExpression prop = ax.getProperty();
			Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap = 
					propIndMap.get(prop);
			if (indToIndMap == null) {
				indToIndMap = new HashMap<>();
				propIndMap.put(prop, indToIndMap);
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
	
	
	
	private void clusterIndividuals(int roleDepth) {
		// individuals are identical wrt to classes
		Out.p("\tClustering individuals by classes");
		clusterIndividualsByClasses();				
		if (roleDepth >= 1) {
			// individuals are identical wrt to classes and roles
			Out.p("\tClustering individuals by classes and roles");
			clusterIndividualsByClassesAndSimpleRoles();			
			// individuals are identical wrt to classes and roles with their objects
			Out.p("\tClustering individuals by classes and roles with their objects");
			clusterIndividualsByClassesAndRoles();					
		}				
	}
	
	
	private void clusterIndividualsByClasses() {
		indClusterMap0 = new HashMap<>();
		Map<Set<OWLClassExpression>, Set<OWLNamedIndividual>> clsIndsMap = new HashMap<>();
		for (OWLNamedIndividual ind : indClasMap.keySet()) {
			Set<OWLClassExpression> cls = indClasMap.get(ind);
			Set<OWLNamedIndividual> inds = clsIndsMap.get(cls);
			if (inds == null) {
				inds = new HashSet<>();
				clsIndsMap.put(cls, inds);
			}
			inds.add(ind);
		}		
		for (Set<OWLNamedIndividual> inds : clsIndsMap.values()) {			
			for (OWLNamedIndividual ind : inds) {
				indClusterMap0.put(ind, inds);
			}
		}
	}
	
	
	
	
	private void clusterIndividualsByClassesAndSimpleRoles() {
		indClusterMap1 = new HashMap<>();
		for (Set<OWLNamedIndividual> inds : indClusterMap0.values()) {				
			for (OWLNamedIndividual ind1 : inds) {
				Set<OWLNamedIndividual> identicals = indClusterMap1.get(ind1);
				if (identicals == null) {
					identicals = new HashSet<>();
					identicals.add(ind1);
					indClusterMap1.put(ind1, identicals);
				}
				// find identical individuals
				for (OWLNamedIndividual ind2 : inds) {
					if (!ind2.equals(ind1) && isIdenticalToSimpleRole(ind1, ind2)) {							
						identicals.add(ind2);
					}
				}					
			}
		}		
	}



	private boolean isIdenticalToSimpleRole(OWLNamedIndividual ind1,
			OWLNamedIndividual ind2) {
		if (ind1.equals(ind2)) {
			return true;
		}
		// check roles
		for (Map<OWLNamedIndividual,Set<OWLNamedIndividual>> indIndMap : propIndMap.values()) {
			if (!indIndMap.containsKey(ind1) && !indIndMap.containsKey(ind2)) {
				continue;
			}
			// check if there is a role which is different
			if ( (indIndMap.containsKey(ind1) && !indIndMap.containsKey(ind2))
					|| (!indIndMap.containsKey(ind1) && indIndMap.containsKey(ind2)) ) {
				return false;
			}			
		}			
		return true;
	}
	
	
	
	
	private void clusterIndividualsByClassesAndRoles() {
		indClusterMap2 = new HashMap<>();
		for (Set<OWLNamedIndividual> inds : indClusterMap1.values()) {				
			for (OWLNamedIndividual ind1 : inds) {
				Set<OWLNamedIndividual> identicals = indClusterMap2.get(ind1);
				if (identicals == null) {
					identicals = new HashSet<>();
					identicals.add(ind1);
					indClusterMap2.put(ind1, identicals);
				}
				// find identical individuals
				for (OWLNamedIndividual ind2 : inds) {
					if (!ind2.equals(ind1) && isIdenticalToRole(ind1, ind2)) {							
						identicals.add(ind2);
					}
				}					
			}
		}
	}



	private boolean isIdenticalToRole(OWLNamedIndividual ind1, OWLNamedIndividual ind2) {
		if (ind1.equals(ind2)) {
			return true;
		}		
		// check successors
		for (Map<OWLNamedIndividual,Set<OWLNamedIndividual>> indIndMap : propIndMap.values()) {
			if (!indIndMap.containsKey(ind1) && !indIndMap.containsKey(ind2)) {
				continue;
			}			
			// check if there is a successor which is different
			if (!haveSameSuccessors(ind1, ind2, indIndMap)) {
				return false;
			}
		}		
		return true;
	}



	private boolean haveSameSuccessors(OWLNamedIndividual ind1,
			OWLNamedIndividual ind2,
			Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indIndMap) {
		Set<OWLNamedIndividual> sucs1 = indIndMap.get(ind1);
		Set<OWLNamedIndividual> sucs2 = indIndMap.get(ind2);
		// set 1
		for (OWLNamedIndividual suc1 : sucs1) {
			if (sucs2.contains(suc1)) {
				continue;
			}
			boolean hasIdentical = false;				
			Set<OWLNamedIndividual> identicals1 = indClusterMap0.get(suc1);
			for (OWLNamedIndividual suc2 : sucs2) {
				if (identicals1.contains(suc2)) {
					hasIdentical = true;
					break;
				}
			}
			if (!hasIdentical) {
				return false;
			}
		}
		// set 2
		for (OWLNamedIndividual suc2 : sucs2) {
			if (sucs1.contains(suc2)) {
				continue;
			}
			boolean hasIdentical = false;
			Set<OWLNamedIndividual> identicals2 = indClusterMap0.get(suc2);
			for (OWLNamedIndividual suc1 : sucs1) {
				if (identicals2.contains(suc1)) {
					hasIdentical = true;
					break;
				}
			}
			if (!hasIdentical) {
				return false;
			}
		}
		return true;
	}
	
		
	
	public Set<OWLNamedIndividual> getInstances(ELNode node,
			Set<OWLNamedIndividual> suspects) {		
		// check if the node is simple
		Set<OWLNamedIndividual> instances = processSimpleNode(node);
		if (instances != null) {
			return instances;
		}
		// if the node is complex, check instances
		if (indClusterMap0 == null) {
			for (OWLNamedIndividual suspect : suspects) {			
				if (isInstanceOf(suspect, node)) {
					if (instances == null) {
						instances = new HashSet<>();
					}
					instances.add(suspect);
				}
			}		
			return instances;
		} else {
			Set<OWLNamedIndividual> nonInstances = null;
			for (OWLNamedIndividual suspect : suspects) {
				// check if there is an identical instance
				if (isIdenticalToOneOf(suspect, instances, node)) {
					continue;
				}
				// check if there is an identical non-instance
				if (isIdenticalToOneOf(suspect, nonInstances, node)) {
					continue;
				}
				if (isInstanceOf(suspect, node)) {
					if (instances == null) {
						instances = new HashSet<>();
					}
					instances.add(suspect);
				} else {
					if (nonInstances == null) {
						nonInstances = new HashSet<>();
					}
					nonInstances.add(suspect);
				}
			}
			// gather all instances
			Set<OWLNamedIndividual> allInstances = null;
			if (instances != null) {
				allInstances = new HashSet<>();
				for (OWLNamedIndividual ind : instances) {
					allInstances.addAll(indClusterMap0.get(ind));
				}
			}
			return allInstances;
		}
	}
	
	
	private boolean isInstanceOf(OWLNamedIndividual ind, ELNode node) {
		// if node == TOP, return true
		if (node.isOWLThing()) {
			return true;
		}
		// check labels
		Set<OWLClassExpression> cls = indClasMap.get(ind);		
		if (cls == null || !cls.containsAll(node.labels)) {
			return false;
		}		
		if (node.getOutEdges() == null || node.getOutEdges().isEmpty()) {
			return true;
		}
		// check edges
		for (CEdge edge : node.getOutEdges()) {
			if (!isInstanceOf(ind, edge)) {
				return false;
			}
		}		
		return true;
	}



	private boolean isInstanceOf(OWLNamedIndividual ind, CEdge edge) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap = 
				propIndMap.get(edge.label);
		if (indToIndMap == null) {
			return false;
		}			
		// check successors				
		Set<OWLNamedIndividual> sucs = indToIndMap.get(ind);
		if (sucs == null) {
			return false;
		}
		// one instance is enough (existential)
		if (indClusterMap0 == null) {
			for (OWLNamedIndividual suc : sucs) {
				if (isInstanceOf(suc, (ELNode) edge.object)) {
					return true;
				}
			}
		} else {
			Set<OWLNamedIndividual> nonInstances = null;
			for (OWLNamedIndividual suc : sucs) {
				// check if there is an identical non-instance
				if (isIdenticalToOneOf(suc, nonInstances, (ELNode) edge.object)) {
					continue;
				}
				if (isInstanceOf(suc, (ELNode) edge.object)) {
					return true;
				} else {
					if (nonInstances == null) {
						nonInstances = new HashSet<>();
					}
					nonInstances.add(suc);
				}
			}
		}
		return false;		
	}
	
	
	private boolean isIdenticalToOneOf(OWLNamedIndividual ind, 
			Set<OWLNamedIndividual> suspects, ELNode node) {
		if (suspects == null) {
			return false;
		}		
		if (node.depth() == 0) {
			for (OWLNamedIndividual suspect : suspects) {
				if (indClusterMap0.get(suspect).contains(ind)) {
					return true;
				}
			}
		}
		else if (node.hasOnlySimpleExistentials()) {
			for (OWLNamedIndividual suspect : suspects) {
				if (indClusterMap1.get(suspect).contains(ind)) {
					return true;
				}
			}
		} else if (node.depth() == 1) {
			for (OWLNamedIndividual suspect : suspects) {
				if (indClusterMap2.get(suspect).contains(ind)) {
					return true;
				}
			}
		}
		return false;
	}



	public Set<OWLNamedIndividual> getInstances(ELNode node) {
		if (node != null) {
			return processNode(node);
		}
		return null;
	}
	
	
	private Set<OWLNamedIndividual> processSimpleNode(ELNode node) {
		// check if node == TOP
		if (node.isOWLThing()) {
			return indClasMap.keySet();
		}				
		// check if node == A
		if (node.isAtomic()) {			
			OWLClassExpression concept = null;
			for (OWLClassExpression label : node.labels) {
				concept = label;
				break;
			}
			return clasIndMap.get(concept);
		}
		// check if node == (R some TOP)		
		if (node.isSimpleExistential()) {
			CEdge edge = node.getOutEdges().getFirst();
			if (edge instanceof SomeEdge) {
				return processSimpleEdge((SomeEdge) edge);
			}
		}
		return null;
	}
	
	
	
	private Set<OWLNamedIndividual> processNode(ELNode node) {
		// check if node == TOP
		if (node.isOWLThing()) {
			return indClasMap.keySet();
		}
		// process labels		
		// if there is one label and no edges
		if (node.isAtomic()) {			
			OWLClassExpression concept = null;
			for (OWLClassExpression label : node.labels) {
				concept = label;
				break;
			}
			return clasIndMap.get(concept);
		}
		// if there are many labels
		Set<OWLNamedIndividual> instances = null;		
		for (OWLClassExpression label : node.labels) {
			Set<OWLNamedIndividual> objs = clasIndMap.get(label);
			if (objs == null || objs.isEmpty()) {
				return null;
			}			
			if (instances == null) {
				instances = new HashSet<>(clasIndMap.get(label));				
			} else {
				instances.retainAll(clasIndMap.get(label));
			}
			if (instances.isEmpty()) {
				return null;
			}			
		}
		// process edges
		if (node.getOutEdges() == null) {
			return instances;
		}
		// if there is one edge and no labels
		if (instances == null && node.getOutEdges().size() == 1) {
			CEdge edge = node.getOutEdges().getFirst();
			return processEdge(edge);
		}
		// if there are many edges or labels
		for (CEdge edge : node.getOutEdges()) {
			Set<OWLNamedIndividual> objs = processEdge(edge);
			if (objs == null || objs.isEmpty()) {
				return null;
			}
			if (instances == null) {
				instances = new HashSet<>(objs);
			} else {
				instances.retainAll(objs);
			}
			if (instances.isEmpty()) {
				return null;
			}			
		}		
		return instances;
	}
	
	
	
	private Set<OWLNamedIndividual> processSimpleEdge(SomeEdge edge) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap = 
				propIndMap.get(edge.label);
		if (indToIndMap == null) {
			return null;
		}
		ELNode obj = (ELNode) edge.object;
		// if edge.object == TOP, return all subjects
		if (obj.isOWLThing()) {
			return indToIndMap.keySet();
		}	
		// retrieve objects
		Set<OWLNamedIndividual> objs = processSimpleNode(obj);
		if (objs == null || objs.isEmpty()) {
			return null;
		}
		// check if objects are all individuals
		if (objs.size() == indClasMap.keySet().size()) {
			return indToIndMap.keySet();
		}						
		return null;
	}
	
		
	
	private Set<OWLNamedIndividual> processEdge(CEdge edge) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap = 
				propIndMap.get(edge.label);
		if (indToIndMap == null) {
			return null;
		}				
		ELNode obj = (ELNode) edge.object;
		// if edge.object == TOP, return all subjects
		if (obj.isOWLThing()) {
			return indToIndMap.keySet();
		}	
		// retrieve objects
		Set<OWLNamedIndividual> objs = processNode(obj);
		if (objs == null || objs.isEmpty()) {
			return null;
		}
		// check if objects are all individuals
		if (objs.size() == indClasMap.keySet().size()) {
			return indToIndMap.keySet();
		}
		// check successors
		Set<OWLNamedIndividual> instances = null;
		for (OWLNamedIndividual subj : indToIndMap.keySet()) {
			Set<OWLNamedIndividual> sucs = indToIndMap.get(subj);
			for (OWLNamedIndividual suc : sucs) {
				if (objs.contains(suc)) {
					if (instances == null) {
						instances = new HashSet<>();
					}
					instances.add(subj);
					break;
				}
			}			
		}					
		return instances;
	}



	public Set<OWLNamedIndividual> getInstances(OWLClassExpression expr) {				
		// process literals
		if (clasIndMap.containsKey(expr)) {
			return clasIndMap.get(expr);				
		}
		Set<OWLNamedIndividual> instances = new HashSet<>();
		ClassExpressionType exprType = expr.getClassExpressionType();
		// process existential restrictions
		if (exprType.equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {				
			// process the filler of an existential, i.e. C in R some C				
			instances.addAll(getInstancesOfExistential((OWLObjectSomeValuesFrom)expr));
		} else
			// process conjunctions
			if (exprType.equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {
				instances.addAll(getInstancesOfConjunctions((OWLObjectIntersectionOf)expr));
			} else
				// process disjunctions
				if (exprType.equals(ClassExpressionType.OBJECT_UNION_OF)) {
					instances.addAll(getInstancesOfDisjunctions((OWLObjectUnionOf)expr));
				} else {
					// process other types

				}				
		return instances;
	}
	
	
	private Set<OWLNamedIndividual> getInstancesOfExistential(OWLObjectSomeValuesFrom expr) {
		// process an existential
		Set<OWLNamedIndividual> insts = new HashSet<>();
		Set<OWLNamedIndividual> clInsts = clasIndMap.get(expr.getFiller());
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> indToIndMap = 
				propIndMap.get(expr.getProperty());
		for (OWLNamedIndividual ind : indToIndMap.keySet()) {
			Set<OWLNamedIndividual> sucs = indToIndMap.get(ind);
			for (OWLNamedIndividual suc : sucs) {
				if (clInsts.contains(suc)) {
					insts.add(ind);
					break;
				}
			}			
		}
		return insts;
	}
	
	private Set<OWLNamedIndividual> getInstancesOfConjunctions(OWLObjectIntersectionOf expr) {
		// process conjunctions of the form A1 and A2 and R some A3 
		Set<OWLClassExpression> conjs = expr.getOperands();
		Set<OWLNamedIndividual> intersect = null;										
		for (OWLClassExpression conj : conjs) {
			if (clasIndMap.containsKey(conj)) {
				if (intersect == null) {
					intersect = new HashSet<>(clasIndMap.get(conj));
				}
				intersect.retainAll(clasIndMap.get(conj));
			} else
				if (conj.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
					Set<OWLNamedIndividual> insts = getInstancesOfExistential((OWLObjectSomeValuesFrom)conj);
					if (intersect == null) {
						intersect = new HashSet<>(insts);
					}
					intersect.retainAll(insts);
				} else {

				}
		}
		return intersect;
	}
	
	
	private Set<OWLNamedIndividual> getInstancesOfDisjunctions(OWLObjectUnionOf expr) {
		// process disjunctions of the form A1 or A2 or R some A3 
		Set<OWLClassExpression> disjs = expr.getOperands();
		Set<OWLNamedIndividual> union = null;										
		for (OWLClassExpression disj : disjs) {
			if (clasIndMap.containsKey(disj)) {
				if (union == null) {
					union = new HashSet<>(clasIndMap.get(disj));
				}
				union.addAll(clasIndMap.get(disj));
			} else
				if (disj.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
					Set<OWLNamedIndividual> insts = getInstancesOfExistential((OWLObjectSomeValuesFrom)disj);
					if (union == null) {
						union = new HashSet<>(insts);
					}
					union.addAll(insts);
				} else {

				}
		}
		return union;
	}
	
	
	public static Set<OWLNamedIndividual> getPropertyChainValues(
			OWLNamedIndividual subj, OWLObjectPropertyChain chain, OWLReasoner reasoner) {
		Set<OWLNamedIndividual> values = null;
		List<OWLObjectPropertyExpression> roles = chain.getPropertyExpressions();
		// two-step role chains
		if (subj != null) {
			Set<OWLNamedIndividual> middles = 
					reasoner.getObjectPropertyValues(subj, roles.get(0)).getFlattened();
			if (middles != null) {
				for (OWLNamedIndividual middle : middles) {
					if (middle != null) {
						Set<OWLNamedIndividual> objs = 
								reasoner.getObjectPropertyValues(middle, roles.get(1)).getFlattened();
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
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> instMap = propIndMap.get(expr);
		if (instMap == null) {
			return null;
		}
		return instMap.get(subj);
	}
	
	
	
	public Set<OWLNamedIndividual> getObjectPropertySubjects(OWLObjectPropertyExpression expr) {
		Map<OWLNamedIndividual, Set<OWLNamedIndividual>> instMap = propIndMap.get(expr);
		if (instMap == null) {
			return null;
		}
		return instMap.keySet();
	}
			

	public List<Expansion> getInstances(ALCNode node) {
		List<Expansion> instances = null;
		for (Expansion suspect : getClusterCenters()) {			
			if (isInstanceOf(suspect, node)) {
				if (instances == null) {
					instances = new LinkedList<>();
				}
				instances.add(suspect);
			}
		}		
		return instances;
	}
	


	public static List<Expansion> getInstances(ALCNode node,
			List<Expansion> suspects) {
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
		expClusterMap = new HashMap<>();
		for (List<Expansion> cluster : localExpClusterMap.values()) {
			expClusterMap.put(cluster.get(0), cluster);
		}		
		Out.p(expClusterMap.size() + " clusters for " + expansions.size() + " expansions");
	}
	
	
	
	public Set<Expansion> getClusterCenters() {
		return expClusterMap.keySet();
	}
	
	
		
	public List<Expansion> orderExpansions(List<Expansion> expansions) {		
		expOrderMap = new HashMap<>();
		final int window = 5;
		int count = 0;
		for (Expansion current : expansions) {			
			boolean found = false;
			// find an ordering		
			loop:
			for (LinkedList<Expansion> order : expOrderMap.values()) {
				// check first and last
				for (int i=0; i<window && i<order.size(); i++) {
					Expansion exp = order.get(i);
					if (current.isMoreSpecificThan(exp)) {						
						order.add(i, current);
						expOrderMap.put(current, order);
						found = true;
						break loop;
					}					
				}
				for (int i=order.size()-1; i>order.size()-window && i>=0; i--) {
					Expansion exp = order.get(i);
					if (exp.isMoreSpecificThan(current)) {
						order.add(i+1, current);
						expOrderMap.put(current, order);
						found = true;
						break loop;
					}
				}
				// check all others
				for (int i=window; i<order.size(); i++) {
					Expansion exp = order.get(i);
					if (current.isMoreSpecificThan(exp)) {						
						order.add(i, current);
						expOrderMap.put(current, order);
						found = true;
						break loop;
					}					
				}
				for (int i=order.size()-window; i>=0; i--) {
					Expansion exp = order.get(i);
					if (exp.isMoreSpecificThan(current)) {
						order.add(i+1, current);
						expOrderMap.put(current, order);
						found = true;
						break loop;
					}
				}
			}
			// if not found, create a new one
			if (!found) {
				LinkedList<Expansion> order = new LinkedList<>();
				order.add(current);
				expOrderMap.put(current, order);
			}
			// debug
			count++;
			if (count % 100 == 0) {
				Set<LinkedList<Expansion>> orders = new HashSet<>(expOrderMap.values());
				Out.p(count + " / " + expansions.size() + " expansions are ordered; "
						+ orders.size() + " orders so far");
			}
		}		
		// find most specific expansions
		List<Expansion> specifics = new LinkedList<>();		
		Set<LinkedList<Expansion>> orders = new HashSet<>(expOrderMap.values());
		for (List<Expansion> order : orders) {
			specifics.add(order.get(0));
		}
		Out.p(specifics.size() 
				+ " orders for " + expansions.size() + " expansions");
		return specifics;
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
				allInstances.addAll(expClusterMap.get(inst));
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
			count += expClusterMap.get(inst).size();
		}		
		return count;
	}



	public Map<OWLClassExpression, Set<OWLNamedIndividual>> getClasIndMap() {
		return clasIndMap;
	}



	public int countIntersection(List<Expansion> insts1, List<Expansion> insts2) {
		if (insts1 == null || insts2 == null) {
			return 0;
		}		
		int count = 0;
		if (insts1.size() <= insts2.size()) {
			for (Expansion inst1 : insts1) {
				if (insts2.contains(inst1)) {
					count += expClusterMap.get(inst1).size();
				}
			}
		} else {
			for (Expansion inst2 : insts2) {
				if (insts1.contains(inst2)) {
					count += expClusterMap.get(inst2).size();
				}
			}
		}		
		return count;
	}


	

	public List<Expansion> getCenterExpansions(Set<OWLNamedIndividual> inds) {
		Set<Expansion> centers = new HashSet<>();		
		for (Expansion center : expClusterMap.keySet()) {
			List<Expansion> cluster = expClusterMap.get(center);
			for (Expansion exp : cluster) {
				if (inds.contains(exp.individual)) {
					centers.add(center);
					break;
				}
			}			
		}				
		return new ArrayList<>(centers);
	}




	
}
