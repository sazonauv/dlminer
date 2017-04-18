package io.dlminer.refine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import io.dlminer.graph.CEdge;
import io.dlminer.graph.CNode;
import io.dlminer.graph.SomeEdge;
import io.dlminer.learn.ClassPair;
import io.dlminer.print.Out;


public abstract class RefinementOperator implements Operator {
	
	// signature to use
    protected Set<OWLDataProperty> dataProperties;
    protected Set<OWLObjectProperty> properties;
	protected Set<OWLClass> classes;
	
	// reasoner	
	protected OWLReasoner reasoner;
	
	protected OWLDataFactory factory;

	protected OperatorConfig config;
	

	// handling redundancy	
	protected Map<OWLClass, Set<OWLClass>> equivClassMap;
	protected Map<OWLClass, Set<OWLClass>> subClassMap;
	protected Map<OWLClass, Set<OWLClass>> superClassMap;
	protected Map<OWLClass, Set<OWLClass>> disjClassMap;
	
	protected Map<OWLObjectProperty, Set<OWLObjectPropertyExpression>> equivPropertyMap;
	protected Map<OWLObjectProperty, Set<OWLObjectPropertyExpression>> subPropertyMap;
	protected Map<OWLObjectProperty, Set<OWLObjectPropertyExpression>> superPropertyMap;
	protected Map<OWLObjectProperty, Set<OWLObjectPropertyExpression>> disjPropertyMap;
	protected Map<OWLObjectProperty, Set<OWLObjectPropertyExpression>> invPropertyMap;	
	
	protected Map<OWLObjectProperty, Set<OWLClass>> propDomainMap;
	protected Map<OWLObjectProperty, Set<OWLClass>> propRangeMap;

	

	// faster checks via mappings
	protected void mapRedundantClassesAndProperties() {

		// map redundant classes
		equivClassMap = new HashMap<>();
		subClassMap = new HashMap<>();
		superClassMap = new HashMap<>();
		disjClassMap = new HashMap<>();

		if (config.checkClassHierarchy) {
            Out.p("\nMapping redundant classes");
            for (OWLClass cl : classes) {
                // check equivalent classes
                Set<OWLClass> eqCls = new HashSet<>(reasoner.getEquivalentClasses(cl).getEntities());
                eqCls.remove(factory.getOWLNothing());
                equivClassMap.put(cl, eqCls);
                // check super classes
                Set<OWLClass> superCls = new HashSet<>(reasoner.getSuperClasses(cl, false).getFlattened());
                superCls.remove(factory.getOWLNothing());
                superClassMap.put(cl, superCls);
                // check sub classes
                Set<OWLClass> subCls = new HashSet<>(reasoner.getSubClasses(cl, false).getFlattened());
                subCls.remove(factory.getOWLNothing());
                subClassMap.put(cl, subCls);
            }
        }

        // check disjoint classes
        if (config.checkDisjointness) {
            for (OWLClass cl : classes) {
                Set<OWLClass> disjCls = new HashSet<>(reasoner.getDisjointClasses(cl).getFlattened());
                disjCls.remove(factory.getOWLNothing());
                disjClassMap.put(cl, disjCls);
            }
        }

		// map redundant properties
		equivPropertyMap = new HashMap<>();
		subPropertyMap = new HashMap<>();
		superPropertyMap = new HashMap<>();
		disjPropertyMap = new HashMap<>();
		invPropertyMap = new HashMap<>();

		if (config.checkPropertyHierarchy) {
            Out.p("\nMapping redundant properties");
            for (OWLObjectProperty prop : properties) {
                // check equivalent properties
                Set<OWLObjectPropertyExpression> eqProps =
                        new HashSet<>(reasoner.getEquivalentObjectProperties(prop).getEntities());
                eqProps.remove(factory.getOWLBottomObjectProperty());
                eqProps.remove(factory.getOWLTopObjectProperty());
                equivPropertyMap.put(prop, eqProps);
                // check super properties
                Set<OWLObjectPropertyExpression> superProps =
                        new HashSet<>(reasoner.getSuperObjectProperties(prop, false).getFlattened());
                superProps.remove(factory.getOWLBottomObjectProperty());
                superProps.remove(factory.getOWLTopObjectProperty());
                superPropertyMap.put(prop, superProps);
                // check sub properties
                Set<OWLObjectPropertyExpression> subProps =
                        new HashSet<>(reasoner.getSubObjectProperties(prop, false).getFlattened());
                subProps.remove(factory.getOWLBottomObjectProperty());
                subProps.remove(factory.getOWLTopObjectProperty());
                subPropertyMap.put(prop, subProps);
                // check disjoint properties
                if (config.checkDisjointness) {
                    Set<OWLObjectPropertyExpression> disjProps =
                            new HashSet<>(reasoner.getDisjointObjectProperties(prop).getFlattened());
                    disjProps.remove(factory.getOWLBottomObjectProperty());
                    disjProps.remove(factory.getOWLTopObjectProperty());
                    disjPropertyMap.put(prop, disjProps);
                }
                // check inverse properties
                Set<OWLObjectPropertyExpression> invProps =
                        new HashSet<>(reasoner.getInverseObjectProperties(prop).getEntities());
                invProps.remove(factory.getOWLBottomObjectProperty());
                invProps.remove(factory.getOWLTopObjectProperty());
                invPropertyMap.put(prop, invProps);
            }
        }

		// fill property domains and ranges
        propDomainMap = new HashMap<>();
        propRangeMap = new HashMap<>();

        if (config.checkPropertyDomainsAndRanges) {
            Out.p("\nFilling property domains and ranges");
            for (OWLObjectProperty prop : properties) {
                Set<OWLClass> domains = new HashSet<>(reasoner.getObjectPropertyDomains(prop, false).getFlattened());
                Set<OWLClass> ranges = new HashSet<>(reasoner.getObjectPropertyRanges(prop, false).getFlattened());
                propDomainMap.put(prop, domains);
                propRangeMap.put(prop, ranges);
            }
        }
	}
	
	
	
	protected boolean isDisjointWithPropertyRanges(OWLClassExpression expr, CNode node) {
		List<CEdge> edges = node.getInEdges();
		if (edges == null) {
			return false;
		}
		if (disjClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> disjClasses = disjClassMap.get(expr);
		if (disjClasses == null || disjClasses.isEmpty()) {
			return false;
		}
		for (CEdge edge :edges) {
			// OK for OnlyEdge
			Set<OWLClass> ranges = propRangeMap.get(edge.label);					
			if (ranges.isEmpty() || ranges.contains(factory.getOWLThing())) {
				continue;
			}			
			for (OWLClass range : ranges) {								
				if (disjClasses.contains(range)) {					
					return true;
				}				
			}				
		}		
		return false;
	}
	
		
	
	
	protected boolean isDisjointWithPropertyDomains(OWLClassExpression expr, CNode node) {
		List<CEdge> edges = node.getOutEdges();
		if (edges == null) {
			return false;
		}
		if (disjClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> disjClasses = disjClassMap.get(expr);
		if (disjClasses == null || disjClasses.isEmpty()) {
			return false;
		}
		for (CEdge edge :edges) {
			if (!(edge instanceof SomeEdge)) {
				continue;
			}
			Set<OWLClass> domains = propDomainMap.get(edge.label);					
			if (domains.isEmpty() || domains.contains(factory.getOWLThing())) {
				continue;
			}			
			ClassPair pair = new ClassPair();
			pair.first = expr;
			for (OWLClass domain : domains) {								
				if (disjClasses.contains(domain)) {					
					return true;
				}				
			}				
		}		
		return false;		
	}
	
	
	
	protected boolean isRedundantWithPropertyRanges(OWLClassExpression expr, CNode node) {
		List<CEdge> edges = node.getInEdges();
		if (edges == null) {
			return false;
		}
		if (subClassMap.isEmpty()) {
			return false;
		}
		// equivalent ranges are not redundant
		Set<OWLClass> subClasses = subClassMap.get(expr);
		if (subClasses == null || subClasses.isEmpty()) {
			// negation
			return false;
		}
		for (CEdge edge :edges) {
			// OK for OnlyEdge
			Set<OWLClass> ranges = propRangeMap.get(edge.label);					
			if (ranges.isEmpty() || ranges.contains(factory.getOWLThing())) {
				continue;
			}			
			for (OWLClass range : ranges) {								
				if (subClasses.contains(range)) {					
					return true;
				}				
			}				
		}		
		return false;		
	}
	
	
	
	protected boolean isRedundantWithPropertyDomains(OWLClassExpression expr, CNode node) {
		List<CEdge> edges = node.getOutEdges();
		if (edges == null) {
			return false;
		}		
		if (equivClassMap.isEmpty() && subClassMap.isEmpty()) {
			return false;
		}
		Set<OWLClass> equivClasses = equivClassMap.get(expr);
		if (equivClasses == null || equivClasses.isEmpty()) {
			// negation
			return false;
		}
		Set<OWLClass> subClasses = subClassMap.get(expr);
		for (CEdge edge :edges) {
			if (!(edge instanceof SomeEdge)) {
				continue;
			}
			Set<OWLClass> domains = propDomainMap.get(edge.label);					
			if (domains.isEmpty() || domains.contains(factory.getOWLThing())) {
				continue;
			}			
			for (OWLClass domain : domains) {								
				if (equivClasses.contains(domain) || subClasses.contains(domain)) {					
					return true;
				}				
			}				
		}		
		return false;		
	}
	
	
	
	/**
	 * @return the equivClassMap
	 */
	public Map<OWLClass, Set<OWLClass>> getEquivClassMap() {
		return equivClassMap;
	}



	/**
	 * @return the subClassMap
	 */
	public Map<OWLClass, Set<OWLClass>> getSubClassMap() {
		return subClassMap;
	}



	/**
	 * @return the superClassMap
	 */
	public Map<OWLClass, Set<OWLClass>> getSuperClassMap() {
		return superClassMap;
	}



	public Map<OWLClass, Set<OWLClass>> getDisjointClassMap() {
		return disjClassMap;
	}
	
	
	public Map<OWLObjectProperty, Set<OWLClass>> getPropRangeMap() {
		return  propRangeMap;
	}
	
}
