package io.dlminer.refine;

import java.util.*;


import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import io.dlminer.graph.ALCNode;
import io.dlminer.graph.CEdge;
import io.dlminer.graph.CNode;
import io.dlminer.graph.ELNode;
import io.dlminer.graph.OnlyEdge;
import io.dlminer.graph.SomeEdge;
import io.dlminer.learn.ClassPair;
import io.dlminer.learn.PropertyPair;
import io.dlminer.ont.LengthMetric;
import io.dlminer.print.Out;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;


public class ALCOperator extends RefinementOperator {
	
	// length restriction
	private int maxLength;
	// depth restriction
	private int maxDepth;
	
		
	// configuration
	private boolean useNegation;
	private boolean useUniversalRestriction;
	private boolean useDisjunction;
	private boolean useDataProperties;
	
	// structures
	private Map<OWLClassExpression, Set<OWLClassExpression>> classHierarchy;
	private Map<OWLClassExpression, OWLClassExpression> negationMap;
	private Map<OWLClassExpression, Set<OWLNamedIndividual>> classInstanceMap;
	private Map<OWLDataProperty, List<Double>> dataPropertyThresholdsMap;
	
	
	public ALCOperator(OWLReasoner reasoner, 
			Set<OWLClass> classes, Set<OWLObjectProperty> properties,
                       Set<OWLDataProperty> dataProperties,
			int maxLength, int maxDepth, boolean checkDisjointness,
                       boolean useDataProperties) {
		this.reasoner = reasoner;	
		this.classes = classes;
		this.properties = properties;
		this.dataProperties = dataProperties;
		this.maxLength = maxLength;
		this.maxDepth = maxDepth;
		this.checkDisjointness = checkDisjointness;
		this.useDataProperties = useDataProperties;
		initDefaultConfig();
		init();
	}
	
	
	
	public ALCOperator(OWLReasoner reasoner, 
			Set<OWLClass> classes, Set<OWLObjectProperty> properties,
                       Set<OWLDataProperty> dataProperties,
			int maxLength, int maxDepth, boolean checkDisjointness,
                       boolean useDataProperties,
			boolean useNegation, boolean useUniversalRestriction, 
			boolean useDisjunction) {
		this.reasoner = reasoner;	
		this.classes = classes;
		this.properties = properties;
        this.dataProperties = dataProperties;
		this.maxLength = maxLength;
		this.maxDepth = maxDepth;
		this.checkDisjointness = checkDisjointness;
        this.useDataProperties = useDataProperties;
		this.useNegation = useNegation;
		this.useUniversalRestriction = useUniversalRestriction;
		this.useDisjunction = useDisjunction;
		init();
	}
	
	
	private void init() {
		factory = new OWLDataFactoryImpl();
		initNegationMap();
		initClassHierachy();
		mapRedundantClassesAndProperties();
		initInstanceMap();
		initDataPropertiesThresholds();
	}



    private void initDataPropertiesThresholds() {
	    if (!useDataProperties) {
	        return;
        }
        dataPropertyThresholdsMap = new HashMap<>();
	    Set<OWLNamedIndividual> inds = reasoner.getRootOntology().getIndividualsInSignature();
	    for (OWLDataProperty prop : dataProperties) {
            List<Double> thresholds = dataPropertyThresholdsMap.get(prop);
            if (thresholds == null) {
                thresholds = new ArrayList<>();
                dataPropertyThresholdsMap.put(prop, thresholds);
            }
            for (OWLNamedIndividual ind : inds) {
                Set<OWLLiteral> dataPropertyValues = reasoner.getDataPropertyValues(ind, prop);
                for (OWLLiteral lit : dataPropertyValues) {
                    if (lit.isInteger()) {
                        thresholds.add((double) lit.parseInteger());
                    } else if (lit.isFloat()) {
                        thresholds.add((double) lit.parseFloat());
                    } else if (lit.isDouble()) {
                        thresholds.add(lit.parseDouble());
                    }
                }
            }
            Collections.sort(thresholds);
        }
    }


    private void initDefaultConfig() {
		this.useNegation = false;
		this.useUniversalRestriction = false;
		this.useDisjunction = false;
	}
	
	
	private void initInstanceMap() {
		classInstanceMap = new HashMap<>();		
		int count = 0;
		for (OWLClass cl : classes) {
			Set<OWLNamedIndividual> insts = new HashSet<>(reasoner.getInstances(cl, false).getFlattened());
			insts.remove(null);
			// disjoint classes
			if (!insts.isEmpty()) {
				classInstanceMap.put(cl, insts);
				Set<OWLClass> disjCls = disjClassMap.get(cl);
				if (disjCls != null) {
					for (OWLClass disjCl : disjCls) {
						OWLClassExpression negCl = negationMap.get(disjCl);
						Set<OWLNamedIndividual> negInsts = classInstanceMap.get(negCl);
						if (negInsts == null) {
							negInsts = new HashSet<>();
							classInstanceMap.put(negCl, negInsts);
						}
						negInsts.addAll(insts);
					}
				}
			}			
			Out.p(++count + " / " + classes.size() + " classes are checked for instances");
		}
		// told negative assertions
		if (!useNegation) {
			return;
		}		
		Set<OWLAxiom> aboxAxioms = reasoner.getRootOntology().getABoxAxioms(true);
		for (OWLAxiom ax : aboxAxioms) {
			if (ax instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) ax;
				if (axiom.getClassExpression() instanceof OWLObjectComplementOf) {
					OWLObjectComplementOf negCl = (OWLObjectComplementOf) axiom.getClassExpression();
					if (!negCl.getOperand().isAnonymous()) {
						Set<OWLNamedIndividual> negInsts = classInstanceMap.get(negCl);
						if (negInsts == null) {
							negInsts = new HashSet<>();
							classInstanceMap.put(negCl, negInsts);
						}
						OWLIndividual ind = axiom.getIndividual();
						if (ind != null && ind.isNamed()) {
							negInsts.add(ind.asOWLNamedIndividual());
						}
					}
				}
			}			
		}
		// reasoning: costly!
//		Out.p("\nChecking negative instances");
//		count = 0;
//		for (OWLClass cl : classes) {
//			OWLClassExpression negCl = negationMap.get(cl);
//			Set<OWLNamedIndividual> negInsts = classInstanceMap.get(negCl);
//			if (negInsts == null) {
//				negInsts = new HashSet<>();
//				classInstanceMap.put(negCl, negInsts);
//			}
//			Set<OWLNamedIndividual> insts = new HashSet<>(reasoner.getInstances(negCl, false).getFlattened());
//			insts.remove(null);
//			negInsts.addAll(insts);
//			Out.p(++count + " / " + classes.size() + " classes are checked for negative instances");
//		}
	}
	
	
		
	private void initNegationMap() {
		if (!useNegation) {
			return;
		}
		negationMap = new HashMap<>();
		for (OWLClass cl : classes) {
			OWLClassExpression neg = factory.getOWLObjectComplementOf(cl);
			negationMap.put(cl, neg);
			negationMap.put(neg, cl);
		}
	}



	private void initClassHierachy() {
		classHierarchy = new HashMap<>();
		// owl:Thing
		OWLClass thing = factory.getOWLThing();		
		classHierarchy.put(thing, getDirectSubClasses(thing));
		// classes
		for (OWLClass cl : classes) {
			classHierarchy.put(cl, getDirectSubClasses(cl));
		}
		// negations
		if (negationMap != null) {
			// owl:Thing			
			Set<OWLClassExpression> tsubs = classHierarchy.get(thing);
			Set<OWLClassExpression> nsups = getDirectSuperClasses(factory.getOWLNothing());
			for (OWLClassExpression nsup : nsups) {
				tsubs.add(negationMap.get(nsup));
			}			
			// classes
			for (OWLClass cl : classes) {
				Set<OWLClassExpression> subs = null;
				Set<OWLClassExpression> sups = getDirectSuperClasses(cl);				
				if (sups != null && !sups.isEmpty()) {
					subs = new HashSet<>();
					for (OWLClassExpression sup : sups) {
						subs.add(negationMap.get(sup));
					}
				}
				classHierarchy.put(negationMap.get(cl), subs);
			}
		}
	}
	
	
	private Set<OWLClassExpression> getDirectSubClasses(OWLClassExpression expr) {
		Set<OWLClassExpression> subs = null;
		for (OWLClass sub : reasoner.getSubClasses(expr, true).getFlattened()) {
			if (classes.contains(sub)) {
				if (subs == null) {
					subs = new HashSet<>();
				}
				subs.add(sub);
			}
		}
		return subs;
	}
	
	
	private Set<OWLClassExpression> getDirectSuperClasses(OWLClassExpression expr) {
		Set<OWLClassExpression> sups = null;
		for (OWLClass sup : reasoner.getSuperClasses(expr, true).getFlattened()) {
			if (classes.contains(sup)) {
				if (sups == null) {
					sups = new HashSet<>();
				}
				sups.add(sup);
			}
		}
		return sups;
	}




	public Set<ALCNode> refine(ALCNode current) {
		Set<ALCNode> extensions = new HashSet<>();
		if (current.length() > maxLength || current.depth() > maxDepth) {
			return extensions;
		}
		// traverse
		List<CNode> cnodes = current.traverse();
		for (CNode cnode : cnodes) {
			ALCNode node = (ALCNode) cnode;
			extensions.addAll(refineNode(node, current));			
		}
		return extensions;
	}
	
	
	
	
	private Set<ALCNode> refineNode(ALCNode node, ALCNode current) {
		Set<ALCNode> extensions = new HashSet<>();		
		// refine labels
		extensions.addAll(refineLabels(node, current));
		if (current.length() >= maxLength) {
			return extensions;
		}
		// extend edges
		if (current.depthOf(node) >= maxDepth) {
			return extensions;
		}
		// existential restrictions
		for (OWLObjectProperty prop : properties) {
			if (!isRedundantExistentialForAddition(prop, node)) {
				extensions.add(getExistential(node, current, prop));
			}
		}
		// universal restrictions
		if (useUniversalRestriction) {			
			for (OWLObjectProperty prop : properties) {				
				extensions.add(getUniversal(node, current, prop));				
			}
		}
		return extensions;
	}
	
	
	
	private boolean isRedundantExistentialForAddition(
			OWLObjectProperty prop, ALCNode node) {
		return isDisjointWithPropertyDomains(prop, node);
	}
	
		
	


	private boolean isDisjointWithPropertyDomains(OWLObjectProperty prop, ALCNode node) {
		// check domains
		if (disjClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> domains = propDomainMap.get(prop);
		if (domains.isEmpty() || domains.contains(factory.getOWLThing())) {
			return false;
		}
		if (domains.contains(factory.getOWLNothing())) {
			return true;
		}
		for (OWLClass domain : domains) {			
			Set<OWLClass> disjClasses = disjClassMap.get(domain);
			if (disjClasses == null || disjClasses.isEmpty()) {
				return false;
			}
			for (OWLClassExpression label : node.clabels) {				
				if (disjClasses.contains(label)) {
					return true;
				}				
			}				
		}				
		return false;
	}
	
	
	
	


	private Set<ALCNode> refineLabels(ALCNode node, ALCNode current) {
		// owl:Thing
		if (node.clabels.isEmpty() && node.dlabels.isEmpty()) {
			return refineLabelsEmpty(node, current);
		}
		return refineLabelsNonempty(node, current);			
	}
	
	
	
	private Set<ALCNode> refineLabelsNonempty(ALCNode node, ALCNode current) {
		// specialise classes
		OWLClassExpression concept = node.getConcept();
		if (concept.toString().contains("#Movie")
				&& concept.toString().contains("#Genre")
				&& concept.toString().contains("#Director")
				) {
			Out.p("\nFOUND: " + concept);
		}
		Set<ALCNode> extensions = specialiseLabels(node, current);		
		if (current.length() <= maxLength - 2) {
			// add classes
			extensions.addAll(extendLabels(node, current));
		}		
		return extensions;
	}



	private Set<ALCNode> extendLabels(ALCNode node, ALCNode current) {
		Set<ALCNode> extensions = new HashSet<>();
		Set<OWLClassExpression> mgcs = classHierarchy.get(factory.getOWLThing());
		if (mgcs == null) {
			return extensions;
		}
		for (OWLClassExpression expr : mgcs) {
			// check redundancy
			if (isRedundantConjunctionForAddition(expr, node)) {
				continue;
			}			
			// add to extension
			extensions.add(getConjunction(expr, node, current));						
		}
		return extensions;
	}
	
	
	
	private ALCNode getConjunction(OWLClassExpression expr, 
			ALCNode node, ALCNode current) {					
		// clone the root
		ALCNode extension = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) extension.find(node);
		// extend the equal node				
		equal.clabels.add(expr);
		return extension;
	}
	
	
	
	
	private boolean isRedundantWithClassExpressions(OWLClassExpression expr, 
			Set<OWLClassExpression> labels) {
		if (labels.isEmpty()) {
			return false;
		}
		if (isRedundantClassFor(expr, labels)) {
			return true;
		}
		return isRedundantNegationFor(expr, labels);		
	}
	
	
	
	private boolean isRedundantNegationFor(OWLClassExpression expr, 
			Set<OWLClassExpression> labels) {
		if (negationMap == null || !expr.isAnonymous()) {
			return false;
		}
		// negations
		OWLClassExpression atomicExpr = negationMap.get(expr);
		if (atomicExpr == null) {
			return false;
		}
		if (labels.contains(atomicExpr)) {
			return true;
		}
		if (equivClassMap.isEmpty() && subClassMap.isEmpty() && superClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> equivClasses = equivClassMap.get(atomicExpr);
		if (equivClasses == null) {
			return false;
		}
		Set<OWLClass> subClasses = subClassMap.get(atomicExpr);
		Set<OWLClass> superClasses = superClassMap.get(atomicExpr);
		// compare to other classes		
		for (OWLClassExpression label : labels) {			
			if (!label.isAnonymous()) {
				continue;
			}
			OWLClassExpression atomicLabel = negationMap.get(label);
			if (atomicLabel == null) {
				continue;
			}			
			if (equivClasses.contains(atomicLabel) 
					|| subClasses.contains(atomicLabel) 
					|| superClasses.contains(atomicLabel)) {
				return true;
			}			
		}
		return false;
	}



	private boolean isRedundantClassFor(OWLClassExpression expr, 
			Set<OWLClassExpression> labels) {
		if (expr.isAnonymous()) {
			return false;
		}
		if (labels.contains(expr)) {
			return true;
		}
		if (equivClassMap.isEmpty() && subClassMap.isEmpty() && superClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> equivClasses = equivClassMap.get(expr);
		if (equivClasses == null) {
			// negation
			return false;
		}
		Set<OWLClass> subClasses = subClassMap.get(expr);
		Set<OWLClass> superClasses = superClassMap.get(expr);
		// compare to other classes		
		for (OWLClassExpression label : labels) {			
			if (equivClasses.contains(label) || subClasses.contains(label) || superClasses.contains(label)) {
				return true;
			}			
		}
		return false;
	}
	
	
	
	private boolean isDisjointWithClassExpressions(OWLClassExpression expr, 
			Set<OWLClassExpression> labels) {
		if (labels.isEmpty()) {
			return false;
		}
		if (disjClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> disjClasses = disjClassMap.get(expr);
		if (disjClasses == null || disjClasses.isEmpty()) {
			return false;
		}
		for (OWLClassExpression label : labels) {
			if (disjClasses.contains(label)) {
				return true;
			}			
		}					
		return false;	
	}

	

	



	private Set<ALCNode> specialiseLabels(ALCNode node, ALCNode current) {
		Set<ALCNode> extensions = new HashSet<>();
		// conjunctions
		for (OWLClassExpression expr : node.clabels) {
			Set<OWLClassExpression> subs = classHierarchy.get(expr);
			// only if there are subclasses
			if (subs != null && !subs.isEmpty()) {
				for (OWLClassExpression sub : subs) {
					// check redundancy
					if (isRedundantConjunctionForSpecialisation(sub, node)) {
						continue;
					}								
					// add to extensions
					extensions.add(replaceConjunction(expr, sub, node, current));
				}
			}
		}
		// disjunctions
		for (OWLClassExpression expr : node.dlabels) {
			Set<OWLClassExpression> subs = classHierarchy.get(expr);
			// if there are subclasses, replace disjunction
			if (subs != null && !subs.isEmpty()) {				
				// never check redundancy (loss of concepts)					
				// add to extensions
				extensions.addAll(replaceDisjunction(expr, node, current));
			} else {
				// drop disjunction
				ALCNode extension = dropDisjunction(expr, node, current);
				if (extension != null) {
					extensions.add(extension);
				}
			}
			
		}		
		return extensions;
	}
	
	
	


	
	private boolean isRedundantConjunctionForSpecialisation(OWLClassExpression expr, 
			ALCNode node) {
		return isDisjointWithClassExpressions(expr, node.clabels)
				|| isDisjointWithPropertyDomains(expr, node)
				|| isDisjointWithPropertyRanges(expr, node);
	}
	
	
	
	private boolean isRedundantConjunctionForAddition(OWLClassExpression expr, 
			ALCNode node) {
		return isRedundantWithClassExpressions(expr, node.clabels)				
				|| isRedundantConjunctionForSpecialisation(expr, node);
	}
	
	
	
	private ALCNode dropDisjunction(OWLClassExpression expr, ALCNode node, ALCNode current) {
		// if {A}, then do not drop A because results in the empty set (owl:Thing)
		if (node.dlabels.size() <= 1 && node.clabels.isEmpty()) {
			return null;
		}
		// if {A, B} and expr=A, then drop A and B, 
		// then move B to conjunctions if not redundant
		OWLClassExpression remain = null;
		if (node.dlabels.size() == 2) {
			for (OWLClassExpression disj : node.dlabels) {
				if (!disj.equals(expr)) {
					remain = disj;
					break;
				}
			}
			if (isRedundantConjunctionForAddition(remain, node)) {
				return null;
			}
		}
		// clone the root
		ALCNode extension = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) extension.find(node);		
		// remove		
		equal.dlabels.remove(expr);		
		if (equal.dlabels.size() == 1) {
			equal.clabels.add(remain);
			equal.dlabels.remove(remain);
		}		
		return extension;
	}



	private Set<ALCNode> replaceDisjunction(OWLClassExpression expr, ALCNode node, ALCNode current) {		
		// clone the root
		ALCNode clone = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) clone.find(node);
		// remove the disjunction to be replaced
		equal.dlabels.remove(expr);
		// get disjunctions that satisfy maximal length
		Set<Set<OWLClassExpression>> disjs = generateDisjunctionsFor(expr, clone.length());
		if (disjs.isEmpty()) {
			return new HashSet<>();
		}		
		// extend labels		
		return extendDisjunctions(disjs, equal, clone);		
	}
	
	
	
	
	private ALCNode replaceConjunction(OWLClassExpression expr, 
			OWLClassExpression sub, ALCNode node, ALCNode current) {
		// clone the root
		ALCNode extension = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) extension.find(node);
		// extend the equal node				
		equal.clabels.add(sub);
		// remove
		equal.clabels.remove(expr);
		return extension;
	}
	



	private Set<ALCNode> refineLabelsEmpty(ALCNode node, ALCNode current) {		
		// get disjunctions that satisfy the maximal length
		Set<Set<OWLClassExpression>> disjs = generateDisjunctionsFor(
				factory.getOWLThing(), current.length()-1);
		if (disjs.isEmpty()) {
			return new HashSet<>();
		}
		// extend labels			
		return extendDisjunctions(disjs, node, current);
	}
	
	
	
	private Set<ALCNode> extendDisjunctions(Set<Set<OWLClassExpression>> disjs,
			ALCNode node, ALCNode current) {
		Set<ALCNode> extensions = new HashSet<>();
		for (Set<OWLClassExpression> disj : disjs) {
			if (disj.isEmpty()) {
				continue;
			}
			// clone the root
			ALCNode extension = current.clone();					
			// find the equal node
			ALCNode equal = (ALCNode) extension.find(node);
			// add disjunction classes
			if (disj.size() == 1) {
				OWLClassExpression disjExpr = null;
				for (OWLClassExpression expr : disj) {
					disjExpr = expr;
				}					
				if (!isRedundantConjunctionForSpecialisation(disjExpr, equal)) {
					equal.clabels.add(disjExpr);
				}
			} else {
				equal.dlabels.addAll(disj);
			}
			// add to extension
			extensions.add(extension);
		}
		return extensions;
	}
	
	
	
	
	private ALCNode getExistential(ALCNode node, ALCNode current, 
			OWLObjectProperty prop) {
		// clone the root
		ALCNode extension = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) extension.find(node);
		// extend the equal node
		Set<OWLClassExpression> l1 = new HashSet<OWLClassExpression>(2);
		Set<OWLClassExpression> l2 = new HashSet<OWLClassExpression>(2);
		ALCNode empty = new ALCNode(l1, l2);
		SomeEdge edge = new SomeEdge(equal, prop, empty);
		equal.addOutEdge(edge);
		return extension;
	}
	
	
	
	private ALCNode getUniversal(ALCNode node, ALCNode current, 
			OWLObjectProperty prop) {
		// clone the root
		ALCNode extension = current.clone();					
		// find the equal node
		ALCNode equal = (ALCNode) extension.find(node);
		// extend the equal node
		Set<OWLClassExpression> l1 = new HashSet<OWLClassExpression>(2);
		Set<OWLClassExpression> l2 = new HashSet<OWLClassExpression>(2);
		ALCNode empty = new ALCNode(l1, l2);
		OnlyEdge edge = new OnlyEdge(equal, prop, empty);
		equal.addOutEdge(edge);
		return extension;
	}



	private Set<Set<OWLClassExpression>> generateDisjunctionsFor(
			OWLClassExpression expr, int length) {
		Set<Set<OWLClassExpression>> disjs = new HashSet<>();
		int len = maxLength - length;
		if (len <= 0) {
			return disjs;			
		}
		Set<OWLClassExpression> mgcs = classHierarchy.get(expr);		
		if (mgcs == null) {
			return disjs;
		}
		// only atomic classes		
		for (OWLClassExpression mgc : mgcs) {
			if (classes.contains(mgc)) {
				Set<OWLClassExpression> disj = new HashSet<>();
				disj.add(mgc);
				disjs.add(disj);
			}
		}
		if (len == 1) {
			return disjs;
		}
		// only negations
		if (useNegation) {
			for (OWLClassExpression mgc : mgcs) {
				if (!classes.contains(mgc)) {
					Set<OWLClassExpression> disj = new HashSet<>();
					disj.add(mgc);
					disjs.add(disj);
				}
			}
		}
		// if disjunctions are not needed
		if (!useDisjunction || len == 2) {
			return disjs;
		}		
		// get all combinations		
		return generateCombinations(mgcs, len);
	}



	private int labelLength(Set<OWLClassExpression> comb) {
		int len = comb.size() - 1;
		for (OWLClassExpression cl : comb) {
			len += LengthMetric.length(cl);
		}
		return len;
	}



	private Set<Set<OWLClassExpression>> generateCombinations(
			Set<OWLClassExpression> mgcs, int len) {
		Set<Set<OWLClassExpression>> combs = new HashSet<>();
		for (OWLClassExpression mgc : mgcs) {			
			Set<OWLClassExpression> comb = new HashSet<>();
			comb.add(mgc);
			combs.add(comb);			
		}
		if (mgcs.size() == 1) {
			return combs;
		}
		int num = (len % 2 == 0) ? len/2 : len/2+1;
		for (int i=2; i<=num; i++) {
			Set<Set<OWLClassExpression>> newCombs = new HashSet<>();
			for (Set<OWLClassExpression> comb : combs) {
				if (comb.size() == i-1) {
					for (OWLClassExpression mgc : mgcs) {
						// do not add redundant combinations
						if (isRedundantWithClassExpressions(mgc, comb)) {
							continue;
						}
						// do not add concepts with no instances or subsumed instances
						Set<OWLNamedIndividual> insts = classInstanceMap.get(mgc);
						if (insts == null || insts.isEmpty()) {
							continue;
						}					
						Set<OWLClassExpression> newComb = new HashSet<>(comb);
						newComb.add(mgc);
						// add combinations that satisfy length
						if (labelLength(newComb) <= len) {
							newCombs.add(newComb);
						}
					}
				}
			}
			combs.addAll(newCombs);
		}		
		return combs;
	}



	private boolean areInstancesSubsumed(Set<OWLNamedIndividual> insts, 
			Set<OWLClassExpression> exprs) {
		for (OWLClassExpression expr : exprs) {
			Set<OWLNamedIndividual> exprInsts = classInstanceMap.get(expr);
			if (exprInsts != null && exprInsts.containsAll(insts)) {
				return true;
			}
		}
		return false;
	}



	public int getMaxLength() {
		return maxLength;
	}



	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}


	
	public boolean isRedundantNode(ALCNode node) {
		LinkedList<CNode> childs = node.traverse();	
		for (CNode child : childs) {
			ALCNode alcChild = (ALCNode) child;
			// owl:Thing is never redundant
			// check conjunctions
			for (OWLClassExpression expr : alcChild.clabels) {
				if (isRedundantConjunction(expr, alcChild)) {
					return true;
				}
			}
			// check disjunctions
			for (OWLClassExpression expr : alcChild.dlabels) {
				if (isRedundantDisjunction(expr, alcChild)) {
					return true;
				}
			}
			// check edges
			if (hasRedundantEdge(alcChild)) {
				return true;
			}
		}
		return false;
	}



	private boolean hasRedundantEdge(ALCNode node) {
		if (node.getOutEdges() == null) {
			return false;
		}		
		for (CEdge e1 : node.getOutEdges()) {
			boolean isEx1 = e1 instanceof SomeEdge;
			for (CEdge e2 : node.getOutEdges()) {
				if (!e1.equals(e2) && 
						e1.label.equals(e2.label)
						&& (isEx1 == e2 instanceof SomeEdge)
						&& (e1.object.isMoreSpecificThan(e2.object)
							|| e2.object.isMoreSpecificThan(e1.object))) {
					return true;
				}
			}
		}		
		return false;
	}


	
	private boolean isRedundantConjunction(OWLClassExpression expr, ALCNode node) {
		return isRedundantConjunctionForSpecialisation(expr, node)
				|| isRedundantWithClassExpressions(expr, node.dlabels)				
				|| isRedundantWithPropertyDomains(expr, node)
				|| isRedundantWithPropertyRanges(expr, node);
	}
	
	
	
	/**
	 * @return the useNegation
	 */
	public boolean isUseNegation() {
		return useNegation;
	}



	/**
	 * @return the useUniversalRestriction
	 */
	public boolean isUseUniversalRestriction() {
		return useUniversalRestriction;
	}



	/**
	 * @return the useDisjunction
	 */
	public boolean isUseDisjunction() {
		return useDisjunction;
	}



	private boolean isRedundantDisjunction(OWLClassExpression expr, ALCNode node) {
		Set<OWLClassExpression> dlabels = new HashSet<>(node.dlabels);
		dlabels.remove(expr);
		return isRedundantWithClassExpressions(expr, dlabels);
	}


	

	/**
	 * @return the classInstanceMap
	 */
	public Map<OWLClassExpression, Set<OWLNamedIndividual>> getClassInstanceMap() {
		return classInstanceMap;
	}



	public Set<ALCNode> getAtomicNodes() {
		Set<ALCNode> extensions = new HashSet<>();		
		for (OWLClass cl : classes) {
			Set<OWLClassExpression> clabels = new HashSet<>(2);
			clabels.add(cl);
			Set<OWLClassExpression> dlabels = new HashSet<>(2);
			ALCNode node = new ALCNode(clabels, dlabels);
			extensions.add(node);
		}
		return extensions;
	}



	public Set<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			OWLObjectPropertyExpression property) {		
		return equivPropertyMap.get(property);
	}



	public Set<OWLObjectPropertyExpression> getSuperObjectProperties(
			OWLObjectPropertyExpression property) {		
		return superPropertyMap.get(property);
	}



	public Set<OWLObjectPropertyExpression> getInverseObjectProperties(
			OWLObjectPropertyExpression property) {
		return invPropertyMap.get(property);
	}
	

}
