package io.dlminer.learn;

import io.dlminer.main.DLMinerMode;
import io.dlminer.main.DLMinerOutputI;
import io.dlminer.ont.AxiomMetric;
import io.dlminer.ont.LengthMetric;
import io.dlminer.ont.OWLObjectPropertyChain;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import io.dlminer.sort.ConceptLengthComparator;
import io.dlminer.sort.HypothesisSorter;
import io.dlminer.sort.MapSetEntry;
import io.dlminer.sort.MapValueSizeComparator;
import io.dlminer.sort.SortingOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.ObjectPropertyInclusionManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class AxiomBuilder {
	
			
	// components
	private OWLDataFactory factory;
	private OWLReasoner ontologyReasoner;
	private OntologyHandler ontologyHandler;
	private ConceptBuilder conceptBuilder;
	private OWLReasoner hypothesisReasoner;
	private OntologyHandler hypothesisHandler;
	
	// parameters
	private double minSupport;	
	private double minPrecision;
	
	private boolean useMinSupport;
	private boolean useMinPrecision;	
	private DLMinerMode dlminerMode;
	
	// axioms
	private Set<OWLAxiom> classAxioms;
	private Set<OWLAxiom> roleAxioms;
	
	private Set<OWLClass> seedClasses;
	
	
	public AxiomBuilder(ConceptBuilder builder, 
			double minSupport, double minPrecision, 
			boolean useMinSupport, 
			boolean useMinPrecision,
			DLMinerMode dlminerMode, Set<OWLClass> seedClasses) {		

		this.factory = builder.getFactory();
		this.ontologyReasoner = builder.getReasoner();
		this.ontologyHandler = builder.getHandler();
		this.conceptBuilder = builder;	
		
		this.minSupport = minSupport;
		this.minPrecision = minPrecision;
		
		this.useMinSupport = useMinSupport;
		this.useMinPrecision = useMinPrecision;
		this.dlminerMode = dlminerMode;
		this.seedClasses = seedClasses;
		
		classAxioms = new HashSet<>();
		roleAxioms = new HashSet<>();
		
		if (dlminerMode.equals(DLMinerMode.KBC)) {
			initInternalReasoner();
		}		
	}
	
	
	
	private void initInternalReasoner() {
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


	private boolean isEmpty(OWLClass cl1, OWLClass cl2,
			Map<OWLClass, Set<OWLNamedIndividual>> posMap) {		
		Set<OWLNamedIndividual> pos1 = posMap.get(cl1);
		Set<OWLNamedIndividual> pos2 = posMap.get(cl2);
		return pos1 == null || pos1.isEmpty() || pos2 == null || pos2.isEmpty();		
	}
	
	
	private boolean isEmpty(OWLObjectProperty prop1, OWLObjectProperty prop2,
			Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> posMap) {		
		Set<List<OWLNamedIndividual>> pos1 = posMap.get(prop1);
		Set<List<OWLNamedIndividual>> pos2 = posMap.get(prop2);
		return pos1 == null || pos1.isEmpty() || pos2 == null || pos2.isEmpty();		
	}
	
	
	
	
	
	// sort concepts by the number of instances descending
	private Map<OWLClass, Set<OWLNamedIndividual>> sortConceptsByInstanceNumber(
			Map<OWLClass, Set<OWLNamedIndividual>> conInsMap, 
			SortingOrder order) {		
		List<MapSetEntry<OWLClass, OWLNamedIndividual>> entryList = new LinkedList<>();
		for (OWLClass cl : conInsMap.keySet()) {
			entryList.add(new MapSetEntry<>(cl, conInsMap.get(cl)));
		}
		MapValueSizeComparator<OWLClass, OWLNamedIndividual> comparator = 
				new MapValueSizeComparator<>(order);
		Collections.sort(entryList, comparator);
		Map<OWLClass, Set<OWLNamedIndividual>> sortedConInsMap = new LinkedHashMap<>();
		for (MapSetEntry<OWLClass, OWLNamedIndividual> entry : entryList) {
            sortedConInsMap.put(entry.key, entry.value);
        }		
		return sortedConInsMap;
	}
	
	
	// sort roles by the number of instances descending
	private Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> sortRolesByInstanceNumber(
			Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap,
			SortingOrder order) {		
		List<MapSetEntry<OWLObjectProperty, List<OWLNamedIndividual>>> entryList = new LinkedList<>();
		for (OWLObjectProperty prop : roleInstMap.keySet()) {
			entryList.add(new MapSetEntry<>(prop, roleInstMap.get(prop)));
		}
		MapValueSizeComparator<OWLObjectProperty, List<OWLNamedIndividual>> comparator = 
				new MapValueSizeComparator<>(order);
		Collections.sort(entryList, comparator);
		Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> sortedRoleInsMap = new LinkedHashMap<>();
		for (MapSetEntry<OWLObjectProperty, List<OWLNamedIndividual>> entry : entryList) {
			sortedRoleInsMap.put(entry.key, entry.value);
		}		
		return sortedRoleInsMap;			
	}


	public Set<Hypothesis> generateInitialClassAxioms(int maxHypothesesNumber) {
		Set<Hypothesis> hypotheses = new HashSet<>();
		Map<OWLClass, Set<OWLNamedIndividual>> classInstanceMap = conceptBuilder.getClassInstanceMap();
		Set<OWLClass> sortCls = sortConceptsByInstanceNumber(classInstanceMap, SortingOrder.DESC).keySet();
		List<OWLClass> cls = new LinkedList<>(sortCls);
		Collections.sort(cls, new ConceptLengthComparator(SortingOrder.ASC));
		int total = cls.size()*cls.size() - cls.size();
		int maxLength = findMaxLength(cls);
		Out.p(total + " axioms to check");
		// if KBC
		if (dlminerMode.equals(DLMinerMode.KBC)) {
		    loop:
			for (int length = 1; length <= 2*maxLength; length++) {
				for (OWLClass cl2 : cls) {
					OWLClassExpression expr2 = conceptBuilder.getExpressionByClass(cl2);				
					int len2 = LengthMetric.length(expr2);				
					if (len2 > length) {
						continue;
					}
					for (OWLClass cl1 : cls) {					
						if (cl1.equals(cl2)) {
							continue;
						}
						OWLClassExpression expr1 = conceptBuilder.getExpressionByClass(cl1);
						int len1 = LengthMetric.length(expr1);				
						if (len1 + len2 > length) {
							continue;
						}						
						Hypothesis h = generateClassAxiom(cl1, cl2);
						// debug
						if (classAxioms.size() % 1e3 == 0) {
							Out.p(classAxioms.size() + " / " + total + " axioms checked; " + hypotheses.size() + " axioms added");
						}
						if (h == null) {
							continue;
						}
						// add a hypothesis
						hypotheses.add(h);
						// handle redundancy
						if (h.precision >= minPrecision) {						
							try {
								hypothesisHandler.addAxioms(h.axioms);
								if (hypothesisReasoner.isConsistent()) {
									hypothesisReasoner.flush();
								} else {
									hypothesisHandler.removeAxioms(h.axioms);
									hypothesisReasoner.flush();
								}
							} catch (Exception e) {
								hypothesisHandler.removeAxioms(h.axioms);
								hypothesisReasoner.flush();
								Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
							}
						}
						// check if the limit is exceeded
                        if (hypotheses.size() >= maxHypothesesNumber) {
                            break loop;
                        }
					}
				}
			}
		}
		// if CDL or NORM
		else {		
			int count = 0;
			loop:
			for (OWLClass cl2 : cls) {
				OWLClassExpression expr2 = conceptBuilder.getExpressionByClass(cl2);
				for (OWLClass cl1 : cls) {					
					if (cl1.equals(cl2)) {
						continue;
					}
					// debug
					if (++count % 1e5 == 0) {
						Out.p(count + " / " + total + " axioms checked; " + hypotheses.size() + " axioms added");
					}
					OWLClassExpression expr1 = conceptBuilder.getExpressionByClass(cl1);
					if (dlminerMode.equals(DLMinerMode.CDL) 
							&& expr1.isAnonymous() && expr2.isAnonymous()) {
						continue;
					}
					// prediction
					OWLClass posClass = conceptBuilder.getPositiveClass();
					OWLClass negClass = conceptBuilder.getNegativeClass();
					if (posClass != null && negClass != null) {
						if (!expr1.equals(posClass) && !expr1.equals(negClass)
								&& !expr2.equals(posClass) && !expr2.equals(negClass)) {
							continue;
						}
					}
					Hypothesis h = generateClassAxiom(cl1, cl2);
					if (h == null) {
						continue;
					}
					// add a hypothesis
					hypotheses.add(h);
                    // check if the limit is exceeded
                    if (hypotheses.size() >= maxHypothesesNumber) {
                        break loop;
                    }
				}
			}			
		}		
		Out.p("\n" + hypotheses.size() + " class axioms are added");
		return hypotheses;
	}
	
	
	
	
	private Hypothesis generateClassAxiom(OWLClass cl1, OWLClass cl2) {				
		Map<OWLClass, Set<OWLNamedIndividual>> classInstanceMap = conceptBuilder.getClassInstanceMap();
		if (isEmpty(cl1, cl2, classInstanceMap)) {
			return null;
		}
		// check if already processed
		OWLAxiom codedAxiom = factory.getOWLSubClassOfAxiom(cl1, cl2);
		if (classAxioms.contains(codedAxiom)) {
			return null;
		}		
		classAxioms.add(codedAxiom);		
		double t1 = System.nanoTime();
		double support = HypothesisEvaluator.getSupport(cl1, cl2, classInstanceMap);				
		if (useMinSupport && support < minSupport) {
			return null;
		}
		Set<OWLNamedIndividual> pos1 = classInstanceMap.get(cl1);
		Set<OWLNamedIndividual> pos2 = classInstanceMap.get(cl2);
		double assumption = pos1.size() - support;
		double precision = support/pos1.size();
		double t2 = System.nanoTime();
		if (useMinPrecision && precision < minPrecision) {
			return null;
		}					
		// check redundancy
		OWLClassExpression expr1 = conceptBuilder.getExpressionByClass(cl1);
		OWLClassExpression expr2 = conceptBuilder.getExpressionByClass(cl2);
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(expr1, expr2);		
		// check seed signature
		if (seedClasses != null && !seedClasses.containsAll(axiom.getClassesInSignature())) {
			return null;
		}
		if (dlminerMode.equals(DLMinerMode.KBC)) {
			boolean isRed = true;
			try {							
				isRed = hypothesisReasoner.isEntailed(axiom);
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
			}
			if (isRed) {
				return null;
			}
		}
		// check consistency and informativeness		
		double informTime = 0;					
		if (ontologyReasoner != null && ontologyReasoner.isEntailmentCheckingSupported(axiom.getAxiomType())) {
			boolean isEnt = true;
			try {								
				double start = System.nanoTime();
				isEnt = ontologyReasoner.isEntailed(axiom);
				informTime = (System.nanoTime() - start)/1e9;				
			} catch (Exception e) {
				Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
			}
			if (isEnt) {
				return null;
			}
		}										
		// create a hypothesis
		Set<OWLAxiom> axSet = new HashSet<>(2);
		axSet.add(axiom);					
		Set<OWLAxiom> codedAxSet = new HashSet<>(2);
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
		double t3 = System.nanoTime();
		int indNumber = ontologyHandler.getIndividuals().size();
		double prob1 = (double)pos1.size() / indNumber;
		double prob2 = (double)pos2.size() / indNumber;
		double prob12 = support / indNumber;
		double prob1not2 = assumption / indNumber;
//		double probnot12 = prob2 - prob12;
//		double probnot1not2 = 1 - prob1 - prob2 + prob12;
		h.support = support;				
		h.assumption = assumption;				
		h.precision = precision;
		h.recall = prob12 / prob2;				
		h.lift = (precision < prob2 * Double.MAX_VALUE) ? precision / prob2 : Double.POSITIVE_INFINITY;
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
		double t4 = System.nanoTime();
		// performance
		Double te1 = conceptBuilder.getTimeByExpression(expr1);
		Double te2 = conceptBuilder.getTimeByExpression(expr2);
		h.basicTime = te1 + te2 + (t2 - t1 + t4 - t3)/1e9;
		h.informTime = informTime;
		// logical measures				
		try {
			h.noveltyApprox = (double) HypothesisEvaluator.getNoveltyApprox(expr1, expr2, ontologyReasoner).size();
			h.dissimilarityApprox = HypothesisEvaluator.getDissimilarityApprox(expr1, expr2, ontologyReasoner);
		} catch (Exception e) {
			Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
		}
		return h;
	}
	
	
	
		
	private int findMaxLength(Collection<OWLClass> cls) {
		int maxLength = 0;
		for (OWLClass cl : cls) {
			OWLClassExpression expr = conceptBuilder.getExpressionByClass(cl);
			int length = LengthMetric.length(expr);
			if (maxLength < length) {
				maxLength = length;
			}
		}		
		return maxLength;
	}



	public Set<Hypothesis> generateInitialRoleAxioms() {
		Set<Hypothesis> hypos = new HashSet<>();
		Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstanceMap = 
				conceptBuilder.getRoleInstanceMap();
//		Set<OWLObjectProperty> props = sortRolesByInstanceNumber(roleInstanceMap, SortingOrder.DESC).keySet();
		Set<OWLObjectProperty> props = roleInstanceMap.keySet();
		int total = props.size()*props.size() - props.size();
		Out.p(total + " role axioms to check");		
		int indNumber = ontologyHandler.getIndividuals().size();
		indNumber = indNumber*indNumber;
		int count = 0;
		for (OWLObjectProperty prop2 : props) {
			for (OWLObjectProperty prop1 : props) {
				if (prop1.equals(prop2)) {
					continue;
				}				
				// debug
				if (++count % 1e5 == 0) {
					Out.p(count + " / " + total + " role axioms checked; " + hypos.size() + " axioms added");
				}
				if (isEmpty(prop1, prop2, roleInstanceMap)) {
					continue;
				}				
				OWLObjectPropertyExpression expr1 = conceptBuilder.getExpressionByRole(prop1);
				OWLObjectPropertyExpression expr2 = conceptBuilder.getExpressionByRole(prop2);
				// a chain can only be on LHS
				if (expr2 instanceof OWLObjectPropertyChain) {
					continue;
				}
				OWLAxiom codedAxiom = factory.getOWLSubObjectPropertyOfAxiom(prop1, prop2);
				if (roleAxioms.contains(codedAxiom)) {
					continue;
				}
				roleAxioms.add(codedAxiom);
				double t1 = System.currentTimeMillis();
				double support = HypothesisEvaluator.getSupport(prop1, prop2, roleInstanceMap);
				if (useMinSupport && support <= minSupport) {
					continue;
				}
				Set<List<OWLNamedIndividual>> pos1 = roleInstanceMap.get(prop1);
				Set<List<OWLNamedIndividual>> pos2 = roleInstanceMap.get(prop2);
				double assumption = pos1.size() - support;
				double precision = support/pos1.size();
				double t2 = System.currentTimeMillis();
				if (useMinPrecision && precision < minPrecision) {
					continue;
				}
				// generate the axiom
				OWLAxiom axiom = null;					
				if (expr1 instanceof OWLObjectPropertyChain) {
					OWLObjectPropertyChain chain = (OWLObjectPropertyChain)expr1;
					axiom = factory.getOWLSubPropertyChainOfAxiom(chain.getPropertyExpressions(), expr2);
				} else {
					axiom = factory.getOWLSubObjectPropertyOfAxiom(expr1, expr2);
				}
				// check consistency and informativeness
				double informTime = 0;								
				if (ontologyReasoner != null && ontologyReasoner.isEntailmentCheckingSupported(axiom.getAxiomType())) {
					boolean isEnt = true;
					try {						
						double start = System.nanoTime();
						isEnt = ontologyReasoner.isEntailed(axiom);
						informTime = (System.nanoTime() - start)/1e9;						
					} catch (Exception e) {
						Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
					}
					if (isEnt) {
						continue;
					}
				}				
				// create a hypothesis
				Set<OWLAxiom> axSet = new HashSet<>(2);
				axSet.add(axiom);				
				Set<OWLAxiom> codedAxSet = new HashSet<>(2);
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
				double t3 = System.currentTimeMillis();
				double prob1 = (double)pos1.size() / indNumber;
				double prob2 = (double)pos2.size() / indNumber;
				double prob12 = support / indNumber;
				h.support = support;
				double prob1not2 = assumption / indNumber;
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
				double t4 = System.currentTimeMillis();
				// performance
				h.basicTime = (t2 - t1 + t4 - t3)/1e3;
				h.informTime = informTime;
				// logical measures
				try {
					h.noveltyApprox = (double) HypothesisEvaluator.getNoveltyApprox(expr1, expr2, ontologyReasoner).size();
					h.dissimilarityApprox = HypothesisEvaluator.getDissimilarityApprox(expr1, expr2, ontologyReasoner);
				} catch (Exception e) {
					Out.p(e + DLMinerOutputI.AXIOM_BUILDING_ERROR);
				}
				// add a hypothesis
				hypos.add(h);
			}
		}		
		Out.p(hypos.size() + " role axioms are added");
		return hypos;
	}
	
	
	
	public void dispose() {
		if (hypothesisReasoner != null) {
			hypothesisReasoner.dispose();
		}
	}
	
}
