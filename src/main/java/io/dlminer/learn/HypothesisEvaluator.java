package io.dlminer.learn;

import io.dlminer.main.DLMinerComponent;
import io.dlminer.main.DLMinerOutput;
import io.dlminer.main.DLMinerOutputI;
import io.dlminer.main.DLMinerStats;
import io.dlminer.ont.LengthMetric;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import io.dlminer.sort.Distance;
import io.dlminer.sort.HypoDominanceComparator;
import io.dlminer.sort.HypoLengthComparator;
import io.dlminer.sort.SortingOrder;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;


public class HypothesisEvaluator implements DLMinerComponent {
	
	
	// tbox
	private OntologyHandler ontologyHandler;
	private OWLReasoner ontologyReasoner;	
	// hypothesis
	private OntologyHandler hypothesisHandler;
	private OWLReasoner hypothesisReasoner;
	// definitions
	private OntologyHandler defaultHandler;
	private OWLReasoner defaultReasoner;	
	// internal class mappings
	private Map<OWLClass, Set<OWLNamedIndividual>> classInstanceMap;
	private Map<OWLNamedIndividual, Set<OWLClass>> instanceClassMap;
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> posExpressionInstanceMap;
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> negExpressionInstanceMap;
	// cluster individuals
	private Map<OWLNamedIndividual, Set<OWLNamedIndividual>> centerIndividualMap;
	private Map<OWLClass, Set<OWLNamedIndividual>> classCenterMap;
	private Map<OWLNamedIndividual, Set<OWLClass>> centerClassMap;
	// internal role mappings
	private Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstanceMap;
	private Map<List<OWLNamedIndividual>, Set<OWLObjectProperty>> instanceRoleMap;


	private ConceptBuilder conceptBuilder;
	
	private OWLDataFactory factory;
		

	public HypothesisEvaluator(DLMinerOutput output) {
		this.ontologyHandler = output.getHandler();
		this.ontologyReasoner = output.getReasoner();
		this.conceptBuilder = output.getConceptBuilder();
		this.classInstanceMap = conceptBuilder.getClassInstanceMap();
		this.roleInstanceMap = conceptBuilder.getRoleInstanceMap();
		this.factory = ontologyHandler.getDataFactory();
	}


	
	public void init() {
		// init maps
		initMaps();
		// cluster individuals
		clusterIndividuals();
		// hypothesis handler and reasoner		
		hypothesisHandler = new OntologyHandler();
		try {
			// Hermit is required here because Pellet is not updated 
			// if axioms are added to an empty ontology
			hypothesisReasoner = ReasonerLoader.initReasoner(ReasonerName.HERMIT, hypothesisHandler.getOntology());
			hypothesisReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
					InferenceType.OBJECT_PROPERTY_HIERARCHY);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
		
	

	private void initMaps() {
		Out.p("\nInitialising maps for evaluation");
		// init the instance-concept map
		if (instanceClassMap == null) {
			instanceClassMap = new HashMap<>();
		}
		for (OWLClass cl : classInstanceMap.keySet()) {
			Set<OWLNamedIndividual> insts = classInstanceMap.get(cl);			
			for (OWLNamedIndividual inst : insts) {
				Set<OWLClass> cls = instanceClassMap.get(inst);
				if (cls == null) {
					cls = new HashSet<>(2);
					instanceClassMap.put(inst, cls);
				}
				cls.add(cl);
			}
		}
		// init the instance-role map
		if (instanceRoleMap == null) {
			instanceRoleMap = new HashMap<>();
		}
		for (OWLObjectProperty prop : roleInstanceMap.keySet()) {
			Set<List<OWLNamedIndividual>> insts = roleInstanceMap.get(prop);
			for (List<OWLNamedIndividual> inst : insts) {
				Set<OWLObjectProperty> props = instanceRoleMap.get(inst);
				if (props == null) {
					props = new HashSet<>(2);
					instanceRoleMap.put(inst, props);					
				}
				props.add(prop);
			}
		}
		// expressions
		if (posExpressionInstanceMap == null) {
			posExpressionInstanceMap = new HashMap<>();
		}
		// negated expressions
		if (negExpressionInstanceMap == null) {
			negExpressionInstanceMap = new HashMap<>();
		}
	}
	
	
	
	private void clusterIndividuals() {
		Out.p("\nClustering individuals");		
		Map<Set<OWLClass>, Set<OWLNamedIndividual>> typeIndMap = new HashMap<>();
		for (OWLNamedIndividual inst : instanceClassMap.keySet()) {
			Set<OWLClass> type = instanceClassMap.get(inst);
			Set<OWLNamedIndividual> insts = typeIndMap.get(type);
			if (insts == null) {
				insts = new HashSet<>(2);
				typeIndMap.put(type, insts);
			}
			insts.add(inst);			
		}		
		centerIndividualMap = new HashMap<>();
		centerClassMap = new HashMap<>();
		for (Set<OWLClass> type : typeIndMap.keySet()) {
			Set<OWLNamedIndividual> insts = typeIndMap.get(type);
			OWLNamedIndividual center = null;
			for (OWLNamedIndividual inst : insts) {
				center = inst;
				break;
			}
			centerIndividualMap.put(center, insts);
			centerClassMap.put(center, type);
		}		
		// init maps		
		classCenterMap = new HashMap<>();
		for (OWLNamedIndividual center : centerClassMap.keySet()) {			
			Set<OWLClass> type = centerClassMap.get(center);				
			for (OWLClass cl  : type) {
				Set<OWLNamedIndividual> centers = classCenterMap.get(cl);
				if (centers == null) {
					centers = new HashSet<>(2);
					classCenterMap.put(cl, centers);
				}
				centers.add(center);
			}			
		}		
		Out.p(centerClassMap.size() + " clusters for " + instanceClassMap.size() + " individuals");
	}
	
	
	
	// =============== Evaluation methods ===================
	
	private double countRoleAssumptions(Hypothesis h) {
		// can only propagate to super-classes due to TBox, 
		// therefore, do not count those;
		// only count unique assumptions
		int assumNumber = 0;
		for (OWLAxiom codedAxiom : h.codedAxioms) {
			assumNumber += countRoleAssumptions(codedAxiom, h);
		}
		return assumNumber;
	}
	
	

	private double countRoleAssumptions(OWLAxiom axiom, Hypothesis h) {
		// can only propagate to super-classes due to TBox, 
		// therefore, do not count those;
		// only count unique assumptions		
		if (!(axiom instanceof OWLSubObjectPropertyOfAxiom)) {
			return 0;
		}
		OWLSubObjectPropertyOfAxiom codedAxiom = (OWLSubObjectPropertyOfAxiom) axiom;
		OWLObjectProperty prop1 = codedAxiom.getSubProperty().asOWLObjectProperty();
		OWLObjectProperty prop2 = codedAxiom.getSuperProperty().asOWLObjectProperty();		
		Set<List<OWLNamedIndividual>> insts1 = roleInstanceMap.get(prop1);			
		Set<List<OWLNamedIndividual>> insts2 = roleInstanceMap.get(prop2);		
		double assumNumber = countDifference(insts1, insts2);
		if (assumNumber == 0) {
			return 0;
		}
		OWLObjectPropertyExpression expr2 = conceptBuilder.getExpressionByRole(prop2);
		return assumNumber * LengthMetric.length(expr2);
	}
	
	
	
	
	
	
	private double countClassAssumptions(Hypothesis h) {
		// can only propagate to super-classes due to TBox, 
		// therefore, do not count those;
		// only count unique assumptions
		int assumNumber = 0;
		for (OWLAxiom codedAxiom : h.codedAxioms) {
			assumNumber += countClassAssumptions(codedAxiom, h);
		}
		return assumNumber;
	}
	
	
	private double countClassAssumptions(OWLAxiom axiom, Hypothesis h) {
		// can only propagate to super-classes due to TBox, 
		// therefore, do not count those;
		// only count unique assumptions		
		if (!(axiom instanceof OWLSubClassOfAxiom)) {
			return 0;
		}
		OWLSubClassOfAxiom codedAxiom = (OWLSubClassOfAxiom) axiom;
		OWLClass cl1 = codedAxiom.getSubClass().asOWLClass();
		OWLClass cl2 = codedAxiom.getSuperClass().asOWLClass();
		Set<OWLNamedIndividual> insts1 = classInstanceMap.get(cl1);			
		Set<OWLNamedIndividual> insts2 = classInstanceMap.get(cl2);		
		double assumNumber = countDifference(insts1, insts2);
		if (assumNumber == 0) {
			return 0;
		}
		OWLClassExpression expr2 = conceptBuilder.getExpressionByClass(cl2);
		return (assumNumber - h.mainContradiction) * LengthMetric.length(expr2);		
	}
	
	
		
	
	
	private double getBravenessLight(Hypothesis h) {
		double braveness = 0;		
		braveness += countClassAssumptions(h);		
		braveness += countRoleAssumptions(h);			
		return braveness;
	}
	
	
	

	

	

	

	
	private double getFitnessLight(Hypothesis h) {		
		double fitness = 0;
		Set<OWLSubClassOfAxiom> classAxioms = null;
		Set<OWLSubObjectPropertyOfAxiom> roleAxioms = null;
		for (OWLAxiom ax : h.codedAxioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				if (classAxioms == null) {
					classAxioms = new HashSet<>(2);
				}
				classAxioms.add((OWLSubClassOfAxiom)ax);
			}
			if (ax instanceof OWLSubObjectPropertyOfAxiom) {
				if (roleAxioms == null) {
					roleAxioms = new HashSet<>(2);
				}
				roleAxioms.add((OWLSubObjectPropertyOfAxiom)ax);
			}
		}
		if (classAxioms != null) {
			fitness += countClassShrinkage(classAxioms);
		}
		if (roleAxioms != null) {
			fitness += countRoleShrinkage(roleAxioms);
		}
		return fitness;
	}
	
	
	
	private double countClassShrinkage(Set<OWLSubClassOfAxiom> axioms) {    	
    	double shrink = 0;    	    	
    	// prepare for counting
    	Set<OWLNamedIndividual> trigInsts = new HashSet<>();    	
    	Set<OWLClass> superCls = new HashSet<>();
    	for (OWLSubClassOfAxiom axiom : axioms) {
    		// find instances-triggers
			OWLClass cl1 = axiom.getSubClass().asOWLClass();			
			trigInsts.addAll(classCenterMap.get(cl1));
			// find all superclasses for counting
			OWLClass cl2 = axiom.getSuperClass().asOWLClass();
			if (superCls.contains(cl2)) {
				continue;
			}
			superCls.add(cl2);
			Set<OWLClass> sups2 = ontologyReasoner.getSuperClasses(cl2, false).getFlattened();			
			superCls.addAll(sups2);
    	}
    	// count assertions which are not entailed by TBox
    	for (OWLNamedIndividual trigInst : trigInsts) {
    		Set<OWLClass> instCls = centerClassMap.get(trigInst);
    		loop:
    		for (OWLClass cl : superCls) {
    			// if a super-class contains the trigger-instance
    			if (instCls.contains(cl)) {    			
    				// check if it is entailed by TBox
    				// equivalent classes
    				Set<OWLClass> eqs = ontologyReasoner.getEquivalentClasses(cl).getEntitiesMinusTop();
    				for (OWLClass eq : eqs) {
    					if (!eq.equals(cl)) {
    						Set<OWLNamedIndividual> eqInsts = classCenterMap.get(eq);
    						if (eqInsts != null && eqInsts.contains(trigInst)) {
    							continue loop;
    						}
    					}
    				}
    				// subclasses
    				Set<OWLClass> subs = ontologyReasoner.getSubClasses(cl, false).getFlattened();
    				for (OWLClass sub : subs) {
        				Set<OWLNamedIndividual> subInsts = classCenterMap.get(sub);
        				if (subInsts != null && subInsts.contains(trigInst)) {
        					continue loop;
        				}
    				}
    				// if it is not entailed by TBox, then count it
    				OWLClassExpression expr = conceptBuilder.getExpressionByClass(cl);
    				shrink += LengthMetric.length(expr) * centerIndividualMap.get(trigInst).size();
    			}
    		}
    	}    	
        return shrink;
    }
    
    
            
        
    	
    
    private double countRoleShrinkage(Set<OWLSubObjectPropertyOfAxiom> axioms) {
    	double shrink = 0;    	    	
    	// prepare for counting
    	Set<List<OWLNamedIndividual>> trigInsts = new HashSet<>();    	
    	Set<OWLObjectPropertyExpression> superRoles = new HashSet<>();
    	for (OWLSubObjectPropertyOfAxiom axiom : axioms) {
    		// find instances-triggers
    		OWLObjectProperty r1 = axiom.getSubProperty().asOWLObjectProperty();			
			trigInsts.addAll(roleInstanceMap.get(r1));
			// find all superclasses for counting
			OWLObjectProperty r2 = axiom.getSuperProperty().asOWLObjectProperty();
			if (superRoles.contains(r2)) {
				continue;
			}
			superRoles.add(r2);
			Set<OWLObjectPropertyExpression> sups2 = ontologyReasoner.getSuperObjectProperties(r2, false).getFlattened();			
			superRoles.addAll(sups2);
    	}
    	// count assertions which are not entailed by TBox
    	for (List<OWLNamedIndividual> trigInst : trigInsts) {
    		loop:
    		for (OWLObjectPropertyExpression role : superRoles) {
    			Set<OWLObjectProperty> instRoles = instanceRoleMap.get(trigInst);
    			// if a superclass contains the trigger-instance
    			if (instRoles.contains(role)) {
    				// check if it is entailed by TBox
    				// equivalent roles
    				Set<OWLObjectPropertyExpression> eqs = ontologyReasoner.getEquivalentObjectProperties(role).getEntitiesMinusTop();
    				for (OWLObjectPropertyExpression eq : eqs) {
    					if (!eq.equals(role)) {
    						Set<List<OWLNamedIndividual>> eqInsts = roleInstanceMap.get(eq);
    						if (eqInsts != null && eqInsts.contains(trigInst)) {
    							continue loop;
    						}    	
    					}
    				}
    				// subroles
    				Set<OWLObjectPropertyExpression> subs = ontologyReasoner.getSubObjectProperties(role, false).getFlattened();
    				for (OWLObjectPropertyExpression sub : subs) {
    					Set<List<OWLNamedIndividual>> subInsts = roleInstanceMap.get(sub);
        				if (subInsts != null && subInsts.contains(trigInst)) {
        					continue loop;
        				}
    				}
    				// if it is not entailed by TBox, then count it
    				OWLObjectPropertyExpression expr = role;
    				if (!role.isAnonymous()) {
    					expr = conceptBuilder.getExpressionByRole(role.asOWLObjectProperty());
    				}    				
    				shrink += LengthMetric.length(expr);
    			}
    		}
    	}    	 	
    	return shrink;
	}
    
    	
	
	
	public boolean setObjectivesLight(Hypothesis h) throws Exception {		
		double t1 = System.nanoTime();
		// fitness
		h.fitness = getFitnessLight(h);
		double t2 = System.nanoTime();
		h.fitnessTime = (t2 - t1)/1e9;
		// braveness
		h.braveness = getBravenessLight(h);
		double t3 = System.nanoTime();
		h.bravenessTime = (t3 - t2)/1e9;
		// novelty
		h.novelty = (double) getNovelty(h).size();
		double t4 = System.nanoTime();
		h.noveltyTime = (t4 - t3)/1e9;
		// dissimilarity
		h.dissimilarity = getDissimilarity(h);
		double t5 = System.nanoTime();
		h.dissimTime = (t5 - t4)/1e9;
		// check fitness
		if (h.fitness > 0) {		
			return true;
		}
		return false;		
	}
	
	
		
		
	private Set<OWLEntity> getNovelty(Hypothesis h) {
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		for (OWLAxiom ax : h.codedAxioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom classAxiom = (OWLSubClassOfAxiom) ax;
				OWLClass subClass = (OWLClass) classAxiom.getSubClass();
				OWLClass superClass = (OWLClass) classAxiom.getSuperClass();
				signature.addAll(getNovelty(subClass, superClass));
			}
			if (ax instanceof OWLSubObjectPropertyOfAxiom) {
				OWLSubObjectPropertyOfAxiom propAxiom = 
						(OWLSubObjectPropertyOfAxiom) ax;
				OWLObjectProperty subProp = (OWLObjectProperty) propAxiom.getSubProperty();
				OWLObjectProperty superProp = (OWLObjectProperty) propAxiom.getSuperProperty();
				signature.addAll(getNovelty(subProp, superProp));
			}
		}
		return signature;
	}
	
	
	
	public static double countDifference(Set<?> set1, Set<?> set2) {
		double difference = 0;
		for (Object o : set1) {
			if (!set2.contains(o)) {
				difference++;
			}
		}
		return difference;
	}
	
	
	

	public static double getAssumption(OWLClassExpression cl1, OWLClassExpression cl2,
			Map<? extends OWLClassExpression, Set<OWLNamedIndividual>> classInstMap) {
		Set<OWLNamedIndividual> pos1 = classInstMap.get(cl1);
		Set<OWLNamedIndividual> pos2 = classInstMap.get(cl2);		
		if (pos1 != null && pos2 != null) {			
			return countDifference(pos1, pos2);
		}		
		return 0;
	}
	
	public static double getAssumption(OWLObjectProperty prop1, OWLObjectProperty prop2,
			Map<? extends OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap) {
		Set<List<OWLNamedIndividual>> pos1 = roleInstMap.get(prop1);
		Set<List<OWLNamedIndividual>> pos2 = roleInstMap.get(prop2);
		if (pos1 != null && pos2 != null) {			
			return countDifference(pos1, pos2);
		}		
		return 0;
	}
	
	
	public static double countIntersection(Set<?> set1, Set<?> set2) {
		double intersect = 0;
		// should be faster for HashSet
		if (set1.size() <= set2.size()) {
			for (Object o : set1) {
				if (set2.contains(o)) {
					intersect++;
				}
			}
		} else {
			for (Object o : set2) {
				if (set1.contains(o)) {
					intersect++;
				}
			}
		}
		return intersect;
	}
	
	
	
	public static double countUnion(Set<?> set1, Set<?> set2) {	
		return set1.size() + set2.size() - countIntersection(set1, set2);
	}
	
	
	

	public static double getSupport(OWLClassExpression cl1, OWLClassExpression cl2,
			Map<? extends OWLClassExpression, Set<OWLNamedIndividual>> classInstMap) {
		Set<OWLNamedIndividual> pos1 = classInstMap.get(cl1);
		Set<OWLNamedIndividual> pos2 = classInstMap.get(cl2);		
		if (pos1 != null && pos2 != null) {			
			return countIntersection(pos1, pos2);
		}
		return 0;
	}
	
	
	public static double getSupport(OWLObjectProperty prop1, OWLObjectProperty prop2, 
			Map<? extends OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap) {		
		Set<List<OWLNamedIndividual>> pos1 = roleInstMap.get(prop1);
		Set<List<OWLNamedIndividual>> pos2 = roleInstMap.get(prop2);
		if (pos1 != null && pos2 != null) {			
			return countIntersection(pos1, pos2);
		}
		return 0;
	}
	
	
	
	public static double getLift(OWLClassExpression cl1, OWLClassExpression cl2,
			Map<? extends OWLClassExpression, Set<OWLNamedIndividual>> classInstMap, 
			double precision) {		
		Set<OWLNamedIndividual> pos2 = classInstMap.get(cl2);
		return precision / pos2.size();
	}
	
	
	
	public static double getLift(OWLObjectProperty prop1,
			OWLObjectProperty prop2,
			Map<? extends OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap,
			double precision) {
		Set<List<OWLNamedIndividual>> pos2 = roleInstMap.get(prop2);
		return precision / pos2.size();
	}
	
	
	
	
	public static double getPrecision(OWLClassExpression cl1, OWLClassExpression cl2,
			Map<? extends OWLClassExpression, Set<OWLNamedIndividual>> classInstMap, double support) {
		Set<OWLNamedIndividual> pos1 = classInstMap.get(cl1);
		return support / pos1.size();
	}
	
	
	public static double getPrecision(OWLObjectProperty prop1,
			OWLObjectProperty prop2,
			Map<? extends OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap,
			double support) {
		Set<List<OWLNamedIndividual>> pos1 = roleInstMap.get(prop1);
		return support / pos1.size();
	}
	
	
	
	
	public static double getRecall(OWLClassExpression cl1, OWLClassExpression cl2,
			Map<? extends OWLClassExpression, Set<OWLNamedIndividual>> classInstMap, double support) {
		Set<OWLNamedIndividual> pos2 = classInstMap.get(cl2);
		return support / pos2.size();
	}
	
	
	
	public static double getRecall(OWLObjectProperty prop1,
			OWLObjectProperty prop2,
			Map<? extends OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap, 
			double support) {
		Set<List<OWLNamedIndividual>> pos2 = roleInstMap.get(prop2);
		return support / pos2.size();
	}

	
	
		
	private Set<OWLEntity> getNovelty(OWLClass cl1, OWLClass cl2) {
		Set<OWLEntity> signature = new HashSet<>();
		// LHS
		Set<OWLClass> subs = ontologyReasoner.getSubClasses(cl1, false).getFlattened();
		Set<OWLClass> defSubs = defaultReasoner.getSubClasses(cl1, false).getFlattened();
		for (OWLClass sub : subs) {
			if (!defSubs.contains(sub)) {
				OWLClassExpression expr = conceptBuilder.getExpressionByClass(sub);
				if (expr != null) {
					signature.addAll(expr.getSignature());
				} else {
					signature.addAll(sub.getSignature());
				}
			}
		}		
		Set<OWLClass> eqs1 = ontologyReasoner.getEquivalentClasses(cl1).getEntities();
		Set<OWLClass> defEqs1 = defaultReasoner.getEquivalentClasses(cl1).getEntities();
		for (OWLClass eq : eqs1) {
			if (!eq.equals(cl1) 
					&& !defEqs1.contains(eq)
					) {
				OWLClassExpression expr = conceptBuilder.getExpressionByClass(eq);
				if (expr != null) {
					signature.addAll(expr.getSignature());
				} else {
					signature.addAll(eq.getSignature());
				}
			}
		}
		// RHS
		Set<OWLClass> supers = ontologyReasoner.getSuperClasses(cl2, false).getFlattened();
		Set<OWLClass> defSupers = defaultReasoner.getSuperClasses(cl2, false).getFlattened();
		for (OWLClass sup : supers) {
			if (!defSupers.contains(sup)) {
				OWLClassExpression expr = conceptBuilder.getExpressionByClass(sup);
				if (expr != null) {
					signature.addAll(expr.getSignature());
				} else {
					signature.addAll(sup.getSignature());
				}
			}
		}
		Set<OWLClass> eqs2 = ontologyReasoner.getEquivalentClasses(cl2).getEntities();
		Set<OWLClass> defEqs2 = defaultReasoner.getEquivalentClasses(cl2).getEntities();
		for (OWLClass eq : eqs2) {
			if (!eq.equals(cl2) 
					&& !defEqs2.contains(eq)
					) {
				OWLClassExpression expr = conceptBuilder.getExpressionByClass(eq);
				if (expr != null) {
					signature.addAll(expr.getSignature());
				} else {
					signature.addAll(eq.getSignature());
				}
			}
		}		
		return signature;
	}
		
	
	public static Set<OWLEntity> getNoveltyApprox(
			OWLClassExpression expr1, OWLClassExpression expr2,
			OWLReasoner reasoner) throws Exception {
		Set<OWLEntity> signature = new HashSet<>();
		// LHS
		if (!expr1.isAnonymous() && !expr1.isOWLNothing() && !expr1.isOWLThing()) {
			OWLClass cl1 = expr1.asOWLClass();
			for (OWLClass sub : reasoner.getSubClasses(cl1, false).getFlattened()) {
				if (!sub.isOWLNothing()) {
					signature.add(sub);
				}
			}		
			for (OWLClass eq : reasoner.getEquivalentClasses(cl1)) {
				if (!eq.equals(cl1) && !eq.isOWLNothing() && !eq.isOWLThing()) {
					signature.add(eq);
				}
			}
		}
		// RHS
		if (!expr2.isAnonymous() && !expr2.isOWLNothing() && !expr2.isOWLThing()) {
			OWLClass cl2 = expr2.asOWLClass();
			for (OWLClass sup : reasoner.getSuperClasses(cl2, false).getFlattened()) {
				if (!sup.isOWLThing()) {
					signature.add(sup);
				}
			}
			for (OWLClass eq : reasoner.getEquivalentClasses(cl2)) {
				if (!eq.equals(cl2) && !eq.isOWLNothing() && !eq.isOWLThing()) {
					signature.add(eq);
				}
			}
		}
		return signature;
	}

	
	
	private Set<OWLEntity> getNovelty(OWLObjectProperty prop1,
			OWLObjectProperty prop2) {
		Set<OWLEntity> signature = new HashSet<>();
		// LHS
		Set<OWLObjectPropertyExpression> subs = ontologyReasoner.getSubObjectProperties(prop1, false).getFlattened();
		Set<OWLObjectPropertyExpression> defSubs = defaultReasoner.getSubObjectProperties(prop1, false).getFlattened();
		for (OWLObjectPropertyExpression sub : subs) {
			if (!defSubs.contains(sub)) {
				if (sub instanceof OWLObjectProperty) {
					OWLObjectPropertyExpression expr = conceptBuilder.getExpressionByRole((OWLObjectProperty)sub);
					if (expr != null) {
						signature.addAll(expr.getSignature());
					} else {
						signature.addAll(sub.getSignature());
					}
				} else {
					signature.addAll(sub.getSignature());
				}
			}
		}	
		Set<OWLObjectPropertyExpression> eqs1 = ontologyReasoner.getEquivalentObjectProperties(prop1).getEntities();
		Set<OWLObjectPropertyExpression> defEqs1 = defaultReasoner.getEquivalentObjectProperties(prop1).getEntities();
		for (OWLObjectPropertyExpression eq : eqs1) {
			if (!eq.equals(prop1) 
					&& !defEqs1.contains(eq)
					) {
				if (eq instanceof OWLObjectProperty) {
					OWLObjectPropertyExpression expr = conceptBuilder.getExpressionByRole((OWLObjectProperty)eq);
					if (expr != null) {
						signature.addAll(expr.getSignature());
					} else {
						signature.addAll(eq.getSignature());
					}
				} else {
					signature.addAll(eq.getSignature());
				}
			}
		}
		// RHS
		Set<OWLObjectPropertyExpression> supers = ontologyReasoner.getSuperObjectProperties(prop2, false).getFlattened();
		Set<OWLObjectPropertyExpression> defSupers = defaultReasoner.getSuperObjectProperties(prop2, false).getFlattened();		
		for (OWLObjectPropertyExpression sup : supers) {
			if (!defSupers.contains(sup)) {
				if (sup instanceof OWLObjectProperty) {
					OWLObjectPropertyExpression expr = conceptBuilder.getExpressionByRole((OWLObjectProperty)sup);
					if (expr != null) {
						signature.addAll(expr.getSignature());
					} else {
						signature.addAll(sup.getSignature());
					}
				} else {
					signature.addAll(sup.getSignature());
				}
			}
		}
		Set<OWLObjectPropertyExpression> eqs2 = ontologyReasoner.getEquivalentObjectProperties(prop2).getEntities();
		Set<OWLObjectPropertyExpression> defEqs2 = defaultReasoner.getEquivalentObjectProperties(prop2).getEntities();
		for (OWLObjectPropertyExpression eq : eqs2) {
			if (!eq.equals(prop2) 
					&& !defEqs2.contains(eq)
					) {
				if (eq instanceof OWLObjectProperty) {
					OWLObjectPropertyExpression expr = conceptBuilder.getExpressionByRole((OWLObjectProperty)eq);
					if (expr != null) {
						signature.addAll(expr.getSignature());
					} else {
						signature.addAll(eq.getSignature());
					}
				} else {
					signature.addAll(eq.getSignature());
				}
			}
		}		
		return signature;
	}
	
		
	public static Set<OWLEntity> getNoveltyApprox(
			OWLObjectPropertyExpression expr1,
			OWLObjectPropertyExpression expr2, OWLReasoner reasoner) 
			throws Exception {
		Set<OWLEntity> signature = new HashSet<>();
		// LHS
		if (!expr1.isAnonymous() && !expr1.isOWLBottomObjectProperty() && !expr1.isOWLTopObjectProperty()) {
			OWLObjectProperty prop1 = (OWLObjectProperty) expr1;
			for (OWLObjectPropertyExpression sub : reasoner.getSubObjectProperties(prop1, false).getFlattened()) {
				if (!sub.isOWLBottomObjectProperty()) {
					signature.addAll(sub.getSignature());
				}
			}		
			for (OWLObjectPropertyExpression eq : reasoner.getEquivalentObjectProperties(prop1)) {
				if (!eq.equals(prop1) && !eq.isOWLTopObjectProperty() && !eq.isOWLBottomObjectProperty()) {
					signature.addAll(eq.getSignature());
				}
			}
		}
		// RHS
		if (!expr2.isAnonymous()  && !expr2.isOWLBottomObjectProperty() && !expr2.isOWLTopObjectProperty()) {
			OWLObjectProperty prop2 = (OWLObjectProperty) expr2;
			for (OWLObjectPropertyExpression sup : reasoner.getSuperObjectProperties(prop2, false).getFlattened()) {
				if (!sup.isOWLTopObjectProperty()) {
					signature.addAll(sup.getSignature());
				}
			}
			for (OWLObjectPropertyExpression eq : reasoner.getEquivalentObjectProperties(prop2)) {
				if (!eq.equals(prop2) && !eq.isOWLTopObjectProperty() && !eq.isOWLBottomObjectProperty()) {
					signature.addAll(eq.getSignature());
				}
			}
		}		
		return signature;
	}
	
	
	
	public static Double getDissimilarityApprox(OWLClassExpression expr1,
			OWLClassExpression expr2, OWLReasoner reasoner) throws Exception {
		if (expr1.equals(expr2)) {
			return 0.0;
		}
		// find superclasses and equivalent classes
		Set<OWLClass> cls1 = expr1.getClassesInSignature();
		Set<OWLClass> cls2 = expr2.getClassesInSignature();
		Set<OWLClass> sig1 = new HashSet<>();
		for (OWLClass cl1 : cls1) {
			Set<OWLClass> superCls = 
					reasoner.getSuperClasses(cl1, false).getFlattened();
			sig1.addAll(superCls);			
			Set<OWLClass> eqCls = 
					reasoner.getEquivalentClasses(cl1).getEntities();
			sig1.addAll(eqCls);
		}
		Set<OWLClass> sig2 = new HashSet<>();
		for (OWLClass cl2 : cls2) {
			Set<OWLClass> superCls = 
					reasoner.getSuperClasses(cl2, false).getFlattened();
			sig2.addAll(superCls);
			Set<OWLClass> eqCls = 
					reasoner.getEquivalentClasses(cl2).getEntities();
			sig2.addAll(eqCls);
		}		
		// find intersection size
		double intersect = 0;
		for (OWLClass cl2 : sig2) {
			if (sig1.contains(cl2)) {
				intersect++;
			}
		}		
		double jaccard = intersect / (sig1.size() + sig2.size() - intersect);
		return 1 - jaccard;
	}
	
	
	
	public static Double getDissimilarityApprox(OWLObjectPropertyExpression expr1,
			OWLObjectPropertyExpression expr2, OWLReasoner reasoner) 
					throws Exception {
		if (expr1.equals(expr2)) {
			return 0.0;
		}
		// find superproperties and equivalent properties
		Set<OWLObjectProperty> props1 = expr1.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> props2 = expr2.getObjectPropertiesInSignature();
		Set<OWLObjectPropertyExpression> sig1 = new HashSet<>();
		for (OWLObjectProperty prop1 : props1) {
			Set<OWLObjectPropertyExpression> superProps = 
					reasoner.getSuperObjectProperties(prop1, false).getFlattened();
			sig1.addAll(superProps);
			Set<OWLObjectPropertyExpression> eqProps = 
					reasoner.getEquivalentObjectProperties(prop1).getEntities();
			sig1.addAll(eqProps);
		}
		Set<OWLObjectPropertyExpression> sig2 = new HashSet<>();
		for (OWLObjectProperty prop2 : props2) {
			Set<OWLObjectPropertyExpression> superProps = 
					reasoner.getSuperObjectProperties(prop2, false).getFlattened();
			sig2.addAll(superProps);
			Set<OWLObjectPropertyExpression> eqProps = 
					reasoner.getEquivalentObjectProperties(prop2).getEntities();
			sig2.addAll(eqProps);
		}		
		// find intersection size
		double intersect = 0;
		for (OWLObjectPropertyExpression prop2 : sig2) {
			if (sig1.contains(prop2)) {
				intersect++;
			}
		}		
		double jaccard = intersect / (sig1.size() + sig2.size() - intersect);
		return 1 - jaccard;
	}
	
	
	
	private Double getDissimilarity(Hypothesis h) {
		double dissim = 0;
		for (OWLAxiom ax : h.axioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom classAxiom = (OWLSubClassOfAxiom) ax;
				OWLClass subClass = conceptBuilder.getClassByExpression(classAxiom.getSubClass());
				OWLClass superClass = conceptBuilder.getClassByExpression(classAxiom.getSuperClass());
				try {
					dissim += getDissimilarity(subClass, superClass);
				} catch (Exception e) {
					Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);
				}
			}
			if (ax instanceof OWLSubObjectPropertyOfAxiom) {
				OWLSubObjectPropertyOfAxiom propAxiom = 
						(OWLSubObjectPropertyOfAxiom) ax;
				OWLObjectProperty subProp = conceptBuilder.getRoleByExpression(propAxiom.getSubProperty());
				OWLObjectProperty superProp = conceptBuilder.getRoleByExpression(propAxiom.getSuperProperty());				
				try {
					dissim += getDissimilarity(subProp, superProp);
				} catch (Exception e) {
					Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);
				}
			}
		}
		return dissim;
	}
	
	
	
	private double getDissimilarity(OWLClass subClass, OWLClass superClass)
		throws Exception {
		return getDissimilarityApprox(subClass, superClass, ontologyReasoner);
	}
	

	private double getDissimilarity(OWLObjectProperty subProp, OWLObjectProperty superProp)
		throws Exception {
		return getDissimilarityApprox(subProp, superProp, ontologyReasoner);
	}
	
	

	public void dispose() {		
		if (hypothesisReasoner != null) {
			hypothesisReasoner.dispose();
		}
		if (defaultReasoner != null) {
			defaultReasoner.dispose();
		}
	}
	
	


	public static Double calculateAverageSupport(Collection<Hypothesis> hypotheses) {
		double averageSupport = 0;
		for (Hypothesis h : hypotheses) {
			averageSupport += h.support;
		}
		return averageSupport/hypotheses.size();
	}

	public static Double calculateAverageAssumption(Collection<Hypothesis> hypotheses) {
		double averageAssumption = 0;
		for (Hypothesis h : hypotheses) {
			averageAssumption += h.assumption;
		}
		return averageAssumption/hypotheses.size();
	}

	public static Double calculateAverageNovelty(Collection<Hypothesis> hypotheses) {
		double averageNovelty = 0;
		for (Hypothesis h : hypotheses) {
			averageNovelty += h.noveltyApprox;
		}
		return averageNovelty/hypotheses.size();
	}

	public static Double calculateAverageLength(Collection<Hypothesis> hypotheses) {
		double averageLength = 0;
		for (Hypothesis h : hypotheses) {
			averageLength += h.length;
		}
		return averageLength/hypotheses.size();
	}

	public static Double calculateMinSupport(Collection<Hypothesis> hypotheses) {
		double minSupport = Double.MAX_VALUE;
		for (Hypothesis h : hypotheses) {
			if (minSupport > h.support) {
				minSupport = h.support;
			}
		}
		return minSupport;
	}

	public static Double calculateMinAssumption(Collection<Hypothesis> hypotheses) {
		double minAssumption = Double.MAX_VALUE;
		for (Hypothesis h : hypotheses) {
			if (minAssumption > h.assumption) {
				minAssumption = h.assumption;
			}
		}
		return minAssumption;
	}

	public static Double calculateMinNovelty(Collection<Hypothesis> hypotheses) {
		double minNovelty = Double.MAX_VALUE;
		for (Hypothesis h : hypotheses) {
			if (minNovelty > h.noveltyApprox) {
				minNovelty = h.noveltyApprox;
			}
		}
		return minNovelty;
	}

	public static Double calculateMinLength(Collection<Hypothesis> hypotheses) {
		double minLength = Double.MAX_VALUE;
		for (Hypothesis h : hypotheses) {
			if (minLength > h.length) {
				minLength = h.length;
			}
		}
		return minLength;
	}

	public static Double calculateMaxSupport(Collection<Hypothesis> hypotheses) {
		double maxSupport = -1;
		for (Hypothesis h : hypotheses) {
			if (maxSupport < h.support) {
				maxSupport = h.support;
			}
		}
		return maxSupport;
	}

	public static Double calculateMaxAssumption(Collection<Hypothesis> hypotheses) {
		double maxAssumption = -1;
		for (Hypothesis h : hypotheses) {
			if (maxAssumption < h.assumption) {
				maxAssumption = h.assumption;
			}
		}
		return maxAssumption;
	}

	public static Double calculateMaxNovelty(Collection<Hypothesis> hypotheses) {
		double maxNovelty = -1;
		for (Hypothesis h : hypotheses) {
			if (maxNovelty < h.noveltyApprox) {
				maxNovelty = h.noveltyApprox;
			}
		}
		return maxNovelty;
	}

	public static Double calculateMaxLength(Collection<Hypothesis> hypotheses) {
		double maxLength = -1;
		for (Hypothesis h : hypotheses) {
			if (maxLength < h.length) {
				maxLength = h.length;
			}
		}
		return maxLength;
	}
	
	
	
	public void evaluateComplexMeasures(Collection<Hypothesis> hypotheses, DLMinerStats stats) {		
		Out.p("\nEvaluating " + hypotheses.size() + " hypotheses");
		Out.p("Adding necessary definitions");		
		double start = System.currentTimeMillis();		
		try {			
			addDefinitionsToReasoners(hypotheses);
		} catch (Exception e) {
			Out.p(e + DLMinerOutputI.REASONER_UPDATE_ERROR);
			double time = (System.currentTimeMillis() - start)/1e3;
			stats.setComplexMeasuresPrecompTime(time);
			return;
		}
		double time = (System.currentTimeMillis() - start)/1e3;
		Out.p("Reasoner is updated in " + Out.fn(time) + " seconds");
		stats.setComplexMeasuresPrecompTime(time);
		Out.p("Evaluation has started...");
		start = System.currentTimeMillis();
		int count = 0;
		for (Hypothesis h : hypotheses) {
			count++;
			if (count % 10 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses evaluated");
			}			
			// evaluate the hypothesis			
			try {
				setObjectivesLight(h);
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);
			}			
		}
		time = (System.currentTimeMillis() - start)/1e3;
		stats.setComplexMeasuresTime(time);
		if (!hypotheses.isEmpty()) {
			Out.p("\nAverage evaluation time = " + Out.fn(time/hypotheses.size()) 
					+ " seconds (" + hypotheses.size() + " hypotheses in " 
					+ Out.fn(time) + " seconds)");
		}
		// set summary measures for comparison
		Out.p("\nSetting summary measures");
		Map<OWLAxiom, Hypothesis> axiomHypoMap = new HashMap<>();
		// one axiom
		for (Hypothesis h : hypotheses) {
			if (h.axioms.size() == 1) {
				OWLAxiom axiom = null;
				for (OWLAxiom ax : h.axioms) {
					axiom = ax;
				}
				axiomHypoMap.put(axiom, h);
				// set summary measures
				h.noveltySum = h.novelty;
				h.fitnessSum = h.fitness;
				h.bravenessSum = h.braveness;
			}
		}
		// multiple axioms
		for (Hypothesis h : hypotheses) {
			if (h.axioms.size() > 1) {
				// summary measures
				h.noveltySum = 0.;
				h.fitnessSum = 0.;
				h.bravenessSum = 0.;
				// time
				h.basicTime = 0.;
				h.mainTime = 0.;
				h.consistTime = 0.;
				h.informTime = 0.;
				h.cleanTime = 0.;
				// contrapositive measures
				h.mainSupport = 0.;
				h.mainAssumption = 0.;
				h.mainContradiction = 0.;
				h.mainPrecision = 0.;
				h.mainLift = 0.;
				h.mainConvictionNeg = 0.;
				h.mainConvictionQue = 0.;
				for (OWLAxiom ax : h.axioms) {					
					Hypothesis axh = axiomHypoMap.get(ax);
					if (axh == null) {
						continue;
					}
					// summary measures
					h.noveltySum = (h.noveltySum == null || axh.novelty == null) ? null : h.noveltySum + axh.novelty;
					h.fitnessSum = (h.fitnessSum == null || axh.fitness == null) ? null : h.fitnessSum + axh.fitness;
					h.bravenessSum = (h.bravenessSum == null || axh.braveness == null) ? null : h.bravenessSum + axh.braveness;
					// time
					h.basicTime = (h.basicTime == null || axh.basicTime == null) ? null : h.basicTime + axh.basicTime;
					h.mainTime = (h.mainTime == null || axh.mainTime == null) ? null : h.mainTime + axh.mainTime;
					h.consistTime = (h.consistTime == null || axh.consistTime == null) ? null : h.consistTime + axh.consistTime;
					h.informTime = (h.informTime == null || axh.informTime == null) ? null : h.informTime + axh.informTime;
					h.cleanTime = (h.cleanTime == null || axh.cleanTime == null) ? null : h.cleanTime + axh.cleanTime;
					// contrapositive measures
					h.mainSupport = (h.mainSupport == null || axh.mainSupport == null) ? null : h.mainSupport + axh.mainSupport;
					h.mainAssumption = (h.mainAssumption == null || axh.mainAssumption == null) ? null : h.mainAssumption + axh.mainAssumption;
					h.mainContradiction = (h.mainContradiction == null || axh.mainContradiction == null) ? null : h.mainContradiction + axh.mainContradiction;
					h.mainPrecision = (h.mainPrecision == null || axh.mainPrecision == null) ? null : h.mainPrecision + axh.mainPrecision;
					h.mainLift = (h.mainLift == null || axh.mainLift == null) ? null : h.mainLift + axh.mainLift;
					h.mainConvictionNeg = (h.mainConvictionNeg == null || axh.mainConvictionNeg == null) ? null : h.mainConvictionNeg + axh.mainConvictionNeg;
					h.mainConvictionQue = (h.mainConvictionQue == null || axh.mainConvictionQue == null) ? null : h.mainConvictionQue + axh.mainConvictionQue;
				}
			}
		}
		// flush reasoners
		try {
			removeDefinitionsFromReasoners(hypotheses);
		} catch (Exception e) {
			Out.p(e + DLMinerOutputI.REASONER_UPDATE_ERROR);
		}
	}
	
	
	
	private void removeDefinitionsFromReasoners(Collection<Hypothesis> hypotheses) {
		Set<OWLAxiom> definitions = new HashSet<>(conceptBuilder.getClassDefinitions(hypotheses));
		definitions.addAll(conceptBuilder.getRoleDefinitions(hypotheses));
		defaultHandler.removeAxioms(definitions);
		defaultReasoner.flush();
		ontologyHandler.removeAxioms(definitions);
		ontologyReasoner.flush();
	}
	
	

	private void addDefinitionsToReasoners(Collection<Hypothesis> hypotheses) throws Exception {		
		// definitions handler and reasoner
		Out.p("Initialising the default reasoner");
		Set<OWLAxiom> definitions = new HashSet<>(conceptBuilder.getClassDefinitions(hypotheses));
		definitions.addAll(conceptBuilder.getRoleDefinitions(hypotheses));
		defaultHandler = new OntologyHandler(definitions);					
		defaultReasoner = ReasonerLoader.initReasoner(defaultHandler.getOntology());
		defaultReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
				InferenceType.OBJECT_PROPERTY_HIERARCHY);
		Out.p("Updating the main reasoner");
		ontologyHandler.addAxioms(definitions);       
        ontologyReasoner.flush();
        ontologyReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
        		InferenceType.OBJECT_PROPERTY_HIERARCHY);        
	}
	
	
	
	public static void initMeasures(Collection<Hypothesis> hypotheses) {
		for (Hypothesis h : hypotheses) {
			h.initMeasures();
		}
		scaleMeasures(hypotheses);
		calculateQuality(hypotheses, Distance.MANHATTAN);
	}
	
	
	
	
	private static void calculateQuality(Collection<Hypothesis> hypotheses, Distance distance) {
		for (Hypothesis h : hypotheses) {			
			h.calculateQuality(distance);
		}
	}
	
	

	private static void scaleMeasures(Collection<Hypothesis> hypotheses) {
		// find maxima
		Hypothesis first = null;
		for (Hypothesis h : hypotheses) {
			first = h;
			break;
		}
		double[] maxima = new double[first.measures.length];
		for (int i=0; i<maxima.length; i++) {
			double max = 0;
			for (Hypothesis h : hypotheses) {
				double abs = h.measures[i] >= 0 ? h.measures[i] : - h.measures[i];
				if (Double.isFinite(abs) && abs > max) {
					max = abs;
				}
			}
			maxima[i] = max;
		}
		// scale measures
		for (Hypothesis h : hypotheses) {
			h.scaleMeasures(maxima);			
		}
	}
		
	
	
	public static void rankHypothesesByDominance(Collection<Hypothesis> hypotheses) {
		Out.p("\nRanking hypotheses by dominance (starting from the top rank)");
		int rank = -1;		
		List<Integer> rankCounts = new ArrayList<>();
		while (containsUnrankedHypothesis(hypotheses)) {
			rank++;
			int count = 0;
			for (Hypothesis h1 : hypotheses) {
				if (h1.rank == null) {
					boolean isDominated = false;
					for (Hypothesis h2 : hypotheses) {
						if (!h1.equals(h2) && h2.rank == null
								&& HypoDominanceComparator.dominates(h2, h1)) {
							isDominated = true;
							break;
						}
					}
					if (!isDominated) {
						h1.rank = rank;
						count++;
					}
				}
			}
			rankCounts.add(count);
			Out.p(count);
		}		
	}
		
	

	private static boolean containsUnrankedHypothesis(Collection<Hypothesis> hypotheses) {
		for (Hypothesis h : hypotheses) {
			if (h.rank == null) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public static void calculateQualityPerRank(Collection<Hypothesis> hypotheses) {
		Out.p("\nCalculating average quality per rank (starting from the top rank)");
		int maxRank = -1;		
		for (Hypothesis h : hypotheses) {
			if (h.rank != null && maxRank < h.rank) {
				maxRank = h.rank;
			}			
		}		
		if (maxRank >= 0) {			
			double[] rankHist = new double[maxRank+1];
			double[] rankQuality = new double[maxRank+1];			
			for (Hypothesis h : hypotheses) {
				rankHist[h.rank]++;
				rankQuality[h.rank] += h.quality;				
			}			
			for (int i=0; i<rankHist.length; i++) {
				Out.p(Out.fn(rankQuality[i]/rankHist[i]));
			}			
		}		
	}
	
	
	public static double calculateAverageQuality(Collection<Hypothesis> hypotheses) {
		double dist = 0;
		for (Hypothesis h : hypotheses) {			
			dist += h.quality;
		}
		return dist / hypotheses.size();		
	}
	
	
	
	/**
	 * @param hypotheses the hypotheses to sample from
	 * @param sampleSize the size of a sample
	 * @return a sample of hypotheses
	 */
	public static Collection<Hypothesis> sampleHypotheses(Collection<Hypothesis> hypotheses, 
			int sampleSize) {		
		if (hypotheses.size() <= sampleSize) {			
			return new HashSet<>(hypotheses);
		}
		// select a sample from the hypotheses
		List<Hypothesis> hypoList = new ArrayList<>(hypotheses);
		Set<Hypothesis> sampleSet = new HashSet<>();		
		while (sampleSet.size() < sampleSize) {
			int index = (int)(hypoList.size()*Math.random());
			sampleSet.add(hypoList.get(index));
		}		
		return sampleSet;
	}
	
	
	
	public static Collection<Hypothesis> sampleHypothesesByLength(Collection<Hypothesis> hypotheses, 
			int sampleSize) {		
		if (hypotheses.size() <= sampleSize) {			
			return new HashSet<>(hypotheses);
		}
		List<Hypothesis> hypoList = new ArrayList<>(hypotheses);
		Collections.sort(hypoList, new HypoLengthComparator(SortingOrder.ASC));
		// build a list of bounds
		double[] bounds = new double[hypoList.size()];		
		for (int i=0; i<hypoList.size(); i++) {
			Hypothesis h = hypoList.get(i);
			bounds[i] = Math.exp(-h.length);			
		}
		double sum = 0;
		for (int i=0; i<bounds.length; i++) {
			sum += bounds[i];
		}
		for (int i=0; i<bounds.length; i++) {
			bounds[i] /= sum;
		}
		sum = 0;
		for (int i=0; i<bounds.length; i++) {
			sum += bounds[i];
			bounds[i] = sum;
		}
		
		// select a sample from the hypotheses		
		Set<Hypothesis> sampleSet = new HashSet<>();		
		while (sampleSet.size() < sampleSize) {
			double r = Math.random();
			int index = -1;
			for (int i=0; i<bounds.length; i++) {
				if (bounds[i] >= r) {
					index = i;
					break;
				}
			}
			sampleSet.add(hypoList.get(index));
		}		
		return sampleSet;
	}
	

	
	public static void rank(Collection<Hypothesis> hypotheses) {
		Out.p("\nRanking all hypotheses");
		HypothesisEvaluator.initMeasures(hypotheses);
        HypothesisEvaluator.rankHypothesesByDominance(hypotheses);
        HypothesisEvaluator.calculateQualityPerRank(hypotheses);
	}
	
	
	
	public void evaluateBasicMeasures(Collection<Hypothesis> hypotheses, DLMinerStats stats) {
		Out.p("\nEvaluating " + hypotheses.size() 
			+ " hypotheses by basic measures");
		double start = System.currentTimeMillis();
		int count = 0;
		for (Hypothesis h : hypotheses) {
			count++;
			if (count % 10 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses evaluated by basic measures");
			}			
			// evaluate the hypothesis
			try {
				setBasicMeasures(h);
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);				
			}			
		}
		double time = (System.currentTimeMillis() - start)/1e3;
		stats.setBasicMeasuresTime(time);
		if (!hypotheses.isEmpty()) {
			Out.p("\nAverage evaluation time = " + Out.fn(time/hypotheses.size()) 
			+ " seconds (" + hypotheses.size() + " hypotheses in " 
			+ Out.fn(time) + " seconds)");
		}		
	}

	
	
	public void evaluateMainMeasures(Collection<Hypothesis> hypotheses, DLMinerStats stats) {
		Out.p("\nEvaluating " + hypotheses.size() 
			+ " hypotheses by main measures (contraposition)");
		double start = System.currentTimeMillis();
		int count = 0;
		for (Hypothesis h : hypotheses) {
			count++;
			if (count % 10 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses evaluated by contraposition");
			}			
			// evaluate the hypothesis
			try {
				setContrapositiveMeasures(h);
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);				
			}			
		}
		double time = (System.currentTimeMillis() - start)/1e3;
		stats.setContraMeasuresTime(time);
		if (!hypotheses.isEmpty()) {
			Out.p("\nAverage evaluation time = " + Out.fn(time/hypotheses.size()) 
			+ " seconds (" + hypotheses.size() + " hypotheses in " 
			+ Out.fn(time) + " seconds)");
		}		
	}

	
	

	public void evaluateConsistency(Collection<Hypothesis> hypotheses, DLMinerStats stats) {
		Out.p("\nEvaluating " + hypotheses.size() 
			+ " hypotheses by consistency");
		double start = System.currentTimeMillis();
		int count = 0;
		for (Hypothesis h : hypotheses) {
			count++;
			if (count % 10 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses evaluated by consistency");
			}			
			// evaluate the hypothesis
			try {
				OWLAxiom classAxiom = h.getFirstAxiom();
				if (classAxiom == null) {
					continue;
				}
				double t1 = System.nanoTime();				
				h.isConsistent = HypothesisEvaluator.isConsistent(
						classAxiom, ontologyHandler, ontologyReasoner);
				h.consistTime = (System.nanoTime() - t1)/1e9;
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.HYPOTHESIS_EVALUATION_ERROR);				
			}			
		}
		double time = (System.currentTimeMillis() - start)/1e3;
		stats.setConsistencyTime(time);
		if (!hypotheses.isEmpty()) {
			Out.p("\nAverage evaluation time = " + Out.fn(time/hypotheses.size()) 
			+ " seconds (" + hypotheses.size() + " hypotheses in " 
			+ Out.fn(time) + " seconds)");
		}		
	}
	
	
	
	private void setBasicMeasures(Hypothesis h) throws Exception {
		// only for axioms
		if (h.axioms.size() > 1) {
			return;
		}		
		// compute measures
		OWLSubClassOfAxiom axiom = h.getFirstClassAxiom();		
		if (axiom == null) {
			// a role inclusion
			return;
		}
		double t1 = System.nanoTime();
		OWLClassExpression cl1 = axiom.getSubClass();
		OWLClassExpression cl2 = axiom.getSuperClass();
		// positive instances
		Set<OWLNamedIndividual> pos1 = getPositiveInstances(cl1);
		Set<OWLNamedIndividual> pos2 = getPositiveInstances(cl2);
		h.support = countIntersection(pos1, pos2);
		h.assumption = pos1.size() - h.support;
		h.precision = h.support/pos1.size();
		int indNumber = ontologyHandler.getIndividuals().size();
		double prob2 = (double)pos2.size() / indNumber;
		h.lift = (h.precision < prob2 * Double.MAX_VALUE) ? h.precision / prob2 : Double.POSITIVE_INFINITY;		
		double t2 = System.nanoTime();
		h.basicTime = (t2 - t1)/1e9;
	}
	
	
	
	
	private void setContrapositiveMeasures(Hypothesis h) throws Exception {
		// only for axioms
		if (h.axioms.size() > 1) {
			return;
		}		
		// compute measures
		OWLSubClassOfAxiom axiom = h.getFirstClassAxiom();
		if (axiom == null) {
			// a role inclusion
			h.mainSupport = h.support;
			h.mainAssumption = h.assumption;
			h.mainContradiction = 0.;
			h.mainPrecision = h.precision;
			h.mainLift = h.lift;
			h.mainConvictionNeg = 0.;
			h.mainConvictionQue = h.conviction;
			h.convictionNeg = 0.;
			h.convictionQue = h.conviction;
			h.mainTime = h.basicTime;			
			return;
		}		
		double t1 = System.nanoTime();
		OWLClassExpression cl1 = axiom.getSubClass();
		OWLClassExpression cl2 = axiom.getSuperClass();
		// positive instances
		Set<OWLNamedIndividual> pos1 = conceptBuilder.getInstancesByExpression(cl1);
		Set<OWLNamedIndividual> pos2 = conceptBuilder.getInstancesByExpression(cl2);
		// find negative instances
		Set<OWLNamedIndividual> neg1 = getNegativeInstances(cl1);
		Set<OWLNamedIndividual> neg2 = getNegativeInstances(cl2);
		// calculate main measures
		int indNumber = conceptBuilder.getHandler().getIndividuals().size();
		double intersect = countIntersection(neg1, neg2);
		h.mainSupport = h.support + intersect;
		h.mainContradiction = countIntersection(pos1, neg2);
		h.assumption -= h.mainContradiction;
		h.mainAssumption = h.assumption + (neg2.size() - intersect - h.mainContradiction);		
		double mainCoverage = h.mainSupport + h.mainAssumption + h.mainContradiction;
		h.mainPrecision = h.mainSupport / mainCoverage;		
		double probNeg1UPos2 = countUnion(neg1, pos2) / indNumber;
		h.mainLift = (h.mainPrecision < probNeg1UPos2 * Double.MAX_VALUE) ? h.mainPrecision / probNeg1UPos2 : Double.POSITIVE_INFINITY;
		double probMContr = h.mainContradiction / indNumber;
		if (h.mainContradiction == 0) {			
			h.mainConvictionNeg = 0.;			
		} else {
			h.mainConvictionNeg = mainCoverage / indNumber;
		}
		if (h.mainAssumption == 0) {
			if (1 - probNeg1UPos2 - probMContr == 0) {
				h.mainConvictionQue = 0.;
			} else {
				h.mainConvictionQue = Double.POSITIVE_INFINITY;
			}
		} else {
			h.mainConvictionQue = mainCoverage * (1 - probNeg1UPos2 - probMContr) / h.mainAssumption;
		}
		// basic convictions
		double coverage = h.support + h.assumption + h.mainContradiction;
		double probPos2 = (double)pos2.size()/indNumber;
		double probNeg2 = (double)neg2.size()/indNumber;
		if (h.mainContradiction == 0 || probNeg2 == 0) {			
			h.convictionNeg = 0.;			
		} else {
			h.convictionNeg = coverage * probNeg2 / h.mainContradiction;
		}
		if (h.assumption == 0) {
			if (1 - probPos2 - probNeg2 == 0) {
				h.convictionQue = 0.;
			} else {
				h.convictionQue = Double.POSITIVE_INFINITY;
			}
		} else {
			h.convictionQue = coverage * (1 - probPos2 - probNeg2) / h.assumption;
		}
		double t2 = System.nanoTime();
		h.mainTime = (t2 - t1)/1e9;
	}

	
	private Set<OWLNamedIndividual> getPositiveInstances(OWLClassExpression expr) 
			throws Exception {		
		// if already processed		
		if (posExpressionInstanceMap.containsKey(expr)) {
			return posExpressionInstanceMap.get(expr);
		}
		// if there is an equivalent concept processed
		OWLClassExpression equiv = findEquivalentConcept(expr, posExpressionInstanceMap);
		if (equiv != null) {
			return posExpressionInstanceMap.get(equiv);
		}		
		// otherwise call the reasoner
		Set<OWLNamedIndividual> insts = 
				ontologyReasoner.getInstances(expr, false).getFlattened();
		// add to the map
		posExpressionInstanceMap.put(expr, insts);
		return insts;
	}
	
	
	
	private Set<OWLNamedIndividual> getNegativeInstances(OWLClassExpression expr) 
			throws Exception {
		Map<OWLClassExpression, Set<OWLNamedIndividual>> exprInstMap = null;
		if (posExpressionInstanceMap != null) {
			exprInstMap = posExpressionInstanceMap;
		} else {
			exprInstMap = conceptBuilder.getClassExpressionInstanceMap();
		}
		OWLClassExpression negExpr = factory.getOWLObjectComplementOf(expr);
		// if already processed	
		if (exprInstMap.containsKey(negExpr)) {
			return exprInstMap.get(negExpr);
		}
		if (negExpressionInstanceMap.containsKey(negExpr)) {
			return negExpressionInstanceMap.get(negExpr);
		}
		// if there is an equivalent concept processed
		OWLClassExpression equiv = findEquivalentConcept(negExpr, exprInstMap);
		if (equiv != null) {
			return exprInstMap.get(equiv);
		}
		equiv = findEquivalentConcept(negExpr, negExpressionInstanceMap);
		if (equiv != null) {
			return negExpressionInstanceMap.get(equiv);
		}
		// otherwise call the reasoner
		Set<OWLNamedIndividual> insts = 
				ontologyReasoner.getInstances(negExpr, false).getFlattened();
		// add to the map
		negExpressionInstanceMap.put(negExpr, insts);
		return insts;
	}
	
	
	
	private OWLClassExpression findEquivalentConcept(
			OWLClassExpression expr, 
			Map<OWLClassExpression, Set<OWLNamedIndividual>> exprInstMap) {
		if (exprInstMap.containsKey(expr)) {
			return expr;
		}
		for (OWLClassExpression other : exprInstMap.keySet()) {
			try {
				if (isEquivalentTo(expr, other)) {
					return other;
				}
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.ENTAILMENT_CHEKING_ERROR);
			}
		}
		return null;
	}
	
	
	
	private boolean isEquivalentTo(OWLClassExpression cl1, 
			OWLClassExpression cl2) throws Exception {		
		OWLAxiom axiom1 = factory.getOWLSubClassOfAxiom(cl1, cl2);
		OWLAxiom axiom2 = factory.getOWLSubClassOfAxiom(cl2, cl1);
		return hypothesisReasoner.isEntailed(axiom1) && hypothesisReasoner.isEntailed(axiom2);
	}
	
	
	
	public static boolean isConsistent(OWLAxiom axiom, 
			OntologyHandler handler, OWLReasoner reasoner) throws Exception {		
		handler.addAxiom(axiom);
		// update a reasoner
		reasoner.flush();		
		boolean isCons = reasoner.isConsistent();
		handler.removeAxiom(axiom);
		// update a reasoner
		reasoner.flush();		
		return isCons;
	}

	
	public static boolean isConsistent(Collection<OWLAxiom> axioms, 
			OntologyHandler handler, OWLReasoner reasoner) throws Exception {
		handler.addAxioms(axioms);
		// update a reasoner
		reasoner.flush();		
		boolean isCons = reasoner.isConsistent();
		handler.removeAxioms(axioms);
		// update a reasoner
		reasoner.flush();		
		return isCons;
	}
	
	

	public OWLNamedIndividual getMostSimilarIndividual(
			OWLNamedIndividual ind, List<OWLNamedIndividual> predictions) {
		Set<OWLClass> indTypes = instanceClassMap.get(ind);
		int index = (int)(Math.random()*predictions.size());
		if (indTypes == null) {			
			return predictions.get(index);
		}
		double maxSimilarity = 0;
		OWLNamedIndividual simInd = null;
		for (OWLNamedIndividual pred : predictions) {
			Set<OWLClass> predTypes = instanceClassMap.get(pred);
			if (predTypes == null) {
				continue;
			}
			double similarity = countIntersection(indTypes, predTypes)/countUnion(indTypes, predTypes);
			if (maxSimilarity < similarity) {
				maxSimilarity = similarity;
				simInd = pred;
			}
		}
		if (simInd == null) {
			return predictions.get(index);
		}
		return simInd;
	}

	
	
	
	public Set<Hypothesis> getBasicMeasures(
			Set<OWLAxiom> axioms, OWLOntology ontology) {
		// add the ABox
		Out.p("\nAdding the ABox and updating the reasoner");
		ontologyHandler.addAxioms(ontology.getLogicalAxioms());
		ontologyReasoner.flush();		
		Out.p("\nComputing basic measures");
		Set<OWLClassExpression> exprs = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
				exprs.add(axiom.getSubClass());
				exprs.add(axiom.getSuperClass());
			}
		}
		Out.p(exprs.size() + " expressions");
		// put missing concepts
		Map<OWLClassExpression, Set<OWLNamedIndividual>> exprInstMap =
				conceptBuilder.getClassExpressionInstanceMap();		
		for (OWLClassExpression expr : exprs) {
			if (exprInstMap.containsKey(expr)) {
				continue;
			}
			Set<OWLNamedIndividual> insts = null;
			double t1 = System.nanoTime();
			insts = ontologyReasoner.getInstances(expr, false).getFlattened();
			double t2 = System.nanoTime();
			double time = (t2 - t1)/1e9;
			conceptBuilder.getExpressionTimeMap().put(expr, time);
			exprInstMap.put(expr, insts);	
		}
		// update
		conceptBuilder.generateAndMapDataConcepts();
		conceptBuilder.buildClassDefinitions();
		classInstanceMap = conceptBuilder.getClassInstanceMap();		
		initMaps();
		clusterIndividuals();
		// compute basic measures
		Set<Hypothesis> hypos = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
				Hypothesis h = evaluateAxiom(axiom);				
				hypos.add(h);				
			}
		}		
		return hypos;
	}

	
	

	private Hypothesis evaluateAxiom(OWLSubClassOfAxiom axiom) {
		OWLClassExpression expr1 = axiom.getSubClass();
		OWLClassExpression expr2 = axiom.getSuperClass();
		OWLClass cl1 = conceptBuilder.getClassByExpression(expr1);
		OWLClass cl2 = conceptBuilder.getClassByExpression(expr2);
		Map<OWLClass, Set<OWLNamedIndividual>> classInstanceMap = conceptBuilder.getClassInstanceMap();
		double t1 = System.nanoTime();
		double support = HypothesisEvaluator.getSupport(cl1, cl2, classInstanceMap);
		Set<OWLNamedIndividual> pos1 = classInstanceMap.get(cl1);
		Set<OWLNamedIndividual> pos2 = classInstanceMap.get(cl2);
		double assumption = pos1.size() - support;
		double precision = support/pos1.size();		
		// create a hypothesis
		Set<OWLAxiom> axSet = new HashSet<>(2);
		axSet.add(axiom);					
		Set<OWLAxiom> codedAxSet = new HashSet<>(2);
		OWLAxiom codedAxiom = factory.getOWLSubClassOfAxiom(cl1, cl2);
		codedAxSet.add(codedAxiom);
		Set<OWLAxiom> defSet = new HashSet<>(2);
		OWLAxiom def1 = conceptBuilder.getDefinitionByExpression(expr1);
		OWLAxiom def2 = conceptBuilder.getDefinitionByExpression(expr2);
		if (def1 != null) {
			defSet.add(def1);
		}
		if (def2 != null) {
			defSet.add(def2);
		}
		Hypothesis h = new Hypothesis(axSet, codedAxSet, defSet);
		// statistical measures		
		int indNumber = ontologyHandler.getIndividuals().size();
		double prob1 = (double)pos1.size() / indNumber;
		double prob2 = (double)pos2.size() / indNumber;
		double prob12 = support / indNumber;
		double prob1not2 = assumption / indNumber;
		h.support = support;				
		h.assumption = assumption;				
		h.precision = precision;
		h.recall = prob12 / prob2;				
		h.lift = (precision < prob2 * Double.MAX_VALUE) ? precision / prob2 : Double.MAX_VALUE;
		h.leverage = precision - prob1*prob2;
		h.addedValue = precision - prob2;
		h.jaccard = prob12 / (prob1 + prob2 - prob12);
		h.certaintyFactor = h.addedValue / (1 - prob2);
		h.klosgen = Math.sqrt(prob12) / (precision - prob2);
		if (prob1not2 == 0) {
			h.conviction = Double.POSITIVE_INFINITY;
		} else {
			h.conviction = prob1*(1 - prob2) / prob1not2;			
		}
		h.shapiro = prob12 - prob1*prob2;
		h.cosine = prob12 / Math.sqrt(prob1*prob2);
		h.informGain = Math.log(h.lift);				
		h.sebag = prob12 / prob1not2;				
		h.contradiction = (prob12 - prob1not2) / prob2;
		h.oddMultiplier = prob12*(1 - prob2) / (prob2*prob1not2);
		h.linearCorrelation = (prob12 == prob1*prob2) ? 0 : 
			(prob12 - prob1*prob2) / 
			Math.sqrt(prob1*prob2*(1 - prob1)*(1 - prob2));				
		h.jmeasure = prob12*Math.log(h.lift) + (prob1not2 == 0 ? 0 :
			prob1not2*Math.log(prob1not2 / (prob1*(1 - prob2))));
		double t2 = System.nanoTime();
		// performance
		Double te1 = conceptBuilder.getTimeByExpression(expr1);
		Double te2 = conceptBuilder.getTimeByExpression(expr2);		
		h.basicTime = te1 + te2 + (t2 - t1)/1e9;
		// logical measures				
		try {
			h.noveltyApprox = (double) getNoveltyApprox(expr1, expr2, ontologyReasoner).size();
			h.dissimilarityApprox = getDissimilarityApprox(expr1, expr2, ontologyReasoner);
		} catch (Exception e) {
			Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
		}
		return h;
	}
	
		
									
}
