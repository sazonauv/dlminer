package io.dlminer.learn;

import io.dlminer.ont.*;
import io.dlminer.print.Out;
import io.dlminer.sort.AbstractComparator;
import io.dlminer.sort.AxiomLengthComparator;
import io.dlminer.sort.HypothesisSorter;
import io.dlminer.sort.OWLClassExpressionSignatureComparator;
import io.dlminer.sort.SortingOrder;

import java.util.*;

import org.semanticweb.owl.explanation.impl.laconic.OPlusGenerator;
import org.semanticweb.owl.explanation.impl.laconic.OPlusSplitting;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLFacet;


public class HypothesisCleaner extends AxiomCleaner {
	
	public static final String HYPOTHESIS_CLEANING_ERROR = ": Ignore and continue cleaning hypotheses";

	private OWLReasoner hypothesisReasoner;
	private OntologyHandler hypothesisHandler;
	private ConceptBuilder conceptBuilder;
	private Set<Hypothesis> hypotheses;
	private Set<OWLAxiom> hypoAxioms;
	private Set<OWLAxiom> entAxioms;
	private OWLReasoner ontologyReasoner;
	
	public HypothesisCleaner(ConceptBuilder conceptBuilder, 
			Set<Hypothesis> hypotheses, OWLReasoner ontologyReasoner) {
		// in order to check transformed axioms
		this.conceptBuilder = conceptBuilder;
		this.hypotheses = hypotheses;
		entAxioms = new HashSet<>();
		hypoAxioms = new HashSet<>();
		for (Hypothesis h : hypotheses) {
			hypoAxioms.addAll(h.axioms);
		}
		this.ontologyReasoner = ontologyReasoner;
		// create the empty handler		
		hypothesisHandler = new OntologyHandler();		
		// init the internal reasoner
		hypothesisReasoner = null; 
		try {
			// Hermit is required because Pellet is not updated once axioms are added
			hypothesisReasoner = ReasonerLoader.initReasoner(ReasonerName.HERMIT, 
					hypothesisHandler.getOntology());
			hypothesisReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
					InferenceType.OBJECT_PROPERTY_HIERARCHY);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	public Set<Hypothesis> cleanMutually() {
		// clean all axioms		
		Out.p("Cleaning axioms");
		Set<OWLAxiom> cleanAxioms = cleanAxioms(hypoAxioms);
		// clean hypotheses separately
		Out.p("Cleaning hypotheses separately");
		Set<Hypothesis> cleanHypos = cleanSeparately();		
		// keep a hypothesis only if all its axioms are clean
		Out.p("Removing redundant hypotheses");
		Set<Hypothesis> mutCleanHypos = new HashSet<>();
		for (Hypothesis cleanHypo : cleanHypos) {			
			for (OWLAxiom axiom : cleanHypo.axioms) {
				if (cleanAxioms.contains(axiom)) {
					mutCleanHypos.add(cleanHypo);
					break;
				}
			}			
		}
		Out.p(mutCleanHypos.size() + " / " + hypotheses.size()
				+ " hypotheses are non-redundant wrt all others");			
		return mutCleanHypos;
	}
	
	
	public Set<Hypothesis> cleanSeparately() {
		Set<Hypothesis> cleanHypos = new HashSet<>();
		int count = 0;
		for (Hypothesis h : hypotheses) {
			// debug
			if (++count % 1e4 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses are cleaned");
			}
			Hypothesis cleanHypo = cleanHypothesis(h);
			// add only one-axiom hypotheses
			if (cleanHypo.axioms.size() == 1) {				
				cleanHypos.add(cleanHypo);
			}
		}
		Out.p(cleanHypos.size() + " / " +
				hypotheses.size() + " unique by equals() hypotheses");		
		return cleanHypos;
	}



    public Set<Hypothesis> cleanDataRestrictions(Set<Hypothesis> hypos) {
	    // check if there are any data restrictions
        boolean hasDataRestrictions = false;
        for (Hypothesis h : hypos) {
            if (AxiomMetric.containsDataRestrictions(h.axioms)) {
                hasDataRestrictions = true;
                break;
            }
        }
        if (!hasDataRestrictions) {
            return hypos;
        }
        Set<Hypothesis> cleanHypos = new HashSet<>();
        // build LHS-RHS mappings
        Map<OWLClassExpression, Set<OWLSubClassOfAxiom>> LhsToRhsMap = new HashMap<>();
        Map<OWLClassExpression, Set<OWLSubClassOfAxiom>> RhsToLhsMap = new HashMap<>();
        Map<OWLSubClassOfAxiom, Hypothesis> axiomHypothesisMap = new HashMap<>();
        int count = 0;
        for (Hypothesis h : hypos) {
            // debug
            if (++count % 1e4 == 0) {
                Out.p(count + " / " + hypos.size() + " hypotheses are cleaned");
            }
            if (AxiomMetric.containsDataRestrictions(h.axioms)) {
                OWLAxiom axiom = null;
                // only one axiom
                for (OWLAxiom ax : h.axioms) {
                    axiom = ax;
                    break;
                }
                OWLSubClassOfAxiom classAxiom = (OWLSubClassOfAxiom) axiom;
                axiomHypothesisMap.put(classAxiom, h);
                OWLClassExpression lhs = classAxiom.getSubClass();
                OWLClassExpression rhs = classAxiom.getSuperClass();
                Set<OWLSubClassOfAxiom> rhsSet = LhsToRhsMap.get(lhs);
                if (rhsSet == null) {
                    rhsSet = new HashSet<>();
                    LhsToRhsMap.put(lhs, rhsSet);
                }
                rhsSet.add(classAxiom);
                Set<OWLSubClassOfAxiom> lhsSet = RhsToLhsMap.get(rhs);
                if (lhsSet == null) {
                    lhsSet = new HashSet<>();
                    RhsToLhsMap.put(rhs, lhsSet);
                }
                lhsSet.add(classAxiom);
            } else {
                cleanHypos.add(h);
            }
        }
        // find redundant data restrictions
        Set<OWLSubClassOfAxiom> removals = new HashSet<>();
        // LHS
        for (OWLClassExpression lhs : LhsToRhsMap.keySet()) {
            Set<OWLSubClassOfAxiom> rhsSet = LhsToRhsMap.get(lhs);
            for (OWLSubClassOfAxiom rhs1 : rhsSet) {
                if (removals.contains(rhs1)) {
                    continue;
                }
                for (OWLSubClassOfAxiom rhs2 : rhsSet) {
                    if (rhs1.equals(rhs2) || removals.contains(rhs2)) {
                        continue;
                    }
                    if (isMoreSpecificDataRestriction(rhs1, rhs2)) {
                        removals.add(rhs2);
                    }
                }
            }
        }
        // RHS
        for (OWLClassExpression rhs : RhsToLhsMap.keySet()) {
            Set<OWLSubClassOfAxiom> lhsSet = RhsToLhsMap.get(rhs);
            for (OWLSubClassOfAxiom lhs1 : lhsSet) {
                if (removals.contains(lhs1)) {
                    continue;
                }
                for (OWLSubClassOfAxiom lhs2 : lhsSet) {
                    if (lhs1.equals(lhs2) || removals.contains(lhs2)) {
                        continue;
                    }
                    if (isMoreSpecificDataRestriction(lhs1, lhs2)) {
                        removals.add(lhs2);
                    }
                }
            }
        }

        // add non-redundant data restrictions
        for (OWLSubClassOfAxiom axiom : axiomHypothesisMap.keySet()) {
            if (!removals.contains(axiom)) {
                cleanHypos.add(axiomHypothesisMap.get(axiom));
            }
        }

        Out.p(cleanHypos.size() + " / " +
                hypos.size() + " have most specific (or no) data restrictions");
        return cleanHypos;
    }


    private boolean isMoreSpecificDataRestriction(
            OWLSubClassOfAxiom axiom1, OWLSubClassOfAxiom axiom2) {
	    if (axiom1.equals(axiom2)) {
	        return true;
        }
        OWLClassExpression sub1 = axiom1.getSubClass();
        OWLClassExpression sub2 = axiom2.getSubClass();
        OWLClassExpression super1 = axiom1.getSuperClass();
        OWLClassExpression super2 = axiom2.getSuperClass();
	    if (sub1.equals(sub2)) {
	        return isMoreSpecificDataRestriction(super1, super2);
        }
        if (super1.equals(super2)) {
            return isMoreSpecificDataRestriction(sub2, sub1);
        }
        return false;
    }

    private boolean isMoreSpecificDataRestriction(
            OWLClassExpression exp1, OWLClassExpression exp2) {
	    // only compare data restrictions
        if (!AxiomMetric.containsDataRestrictions(exp1)
                || !AxiomMetric.containsDataRestrictions(exp2)) {
            return false;
        }
        if (exp1.equals(exp2)) {
            return true;
        }
	    // no disjunctions
	    if (LengthMetric.length(exp1) < LengthMetric.length(exp2)) {
	        return false;
        }
        // compare: for each restriction in 2
        // there must be a more specific restriction in 1
        for (OWLClassExpression nest2 : exp2.getNestedClassExpressions()) {
            if (!(nest2 instanceof OWLDataSomeValuesFrom)) {
                continue;
            }
            boolean found = false;
            for (OWLClassExpression nest1 : exp1.getNestedClassExpressions()) {
                if (!(nest1 instanceof OWLDataSomeValuesFrom)) {
                    continue;
                }
                if (isMoreSpecificDataRestriction(
                        (OWLDataSomeValuesFrom) nest1, (OWLDataSomeValuesFrom) nest2)) {
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


    private boolean isMoreSpecificDataRestriction(
            OWLDataSomeValuesFrom exp1, OWLDataSomeValuesFrom exp2) {
	    if (!exp1.getProperty().equals(exp2.getProperty())) {
	        return false;
        }
        OWLDataRange range1 = exp1.getFiller();
        OWLDataRange range2 = exp2.getFiller();
        if (!(range1 instanceof OWLDatatypeRestriction)
                || !(range2 instanceof OWLDatatypeRestriction)) {
            return false;
        }
        OWLDatatypeRestriction dataRestr1 = (OWLDatatypeRestriction) range1;
        OWLDatatypeRestriction dataRestr2 = (OWLDatatypeRestriction) range2;
        // always one facet restriction
        OWLFacetRestriction facetRestr1 = null;
        for (OWLFacetRestriction fr : dataRestr1.getFacetRestrictions()) {
            facetRestr1 = fr;
            break;
        }
        OWLFacetRestriction facetRestr2 = null;
        for (OWLFacetRestriction fr : dataRestr2.getFacetRestrictions()) {
            facetRestr2 = fr;
            break;
        }
        OWLFacet facet1 = facetRestr1.getFacet();
        OWLFacet facet2 = facetRestr2.getFacet();
        if (!facet1.equals(facet2)) {
            return false;
        }
        OWLLiteral lit1 = facetRestr1.getFacetValue();
        OWLLiteral lit2 = facetRestr2.getFacetValue();
        if (lit1.equals(lit2)) {
            return true;
        }
        if (!lit1.isDouble() || !lit2.isDouble()) {
            return false;
        }
        double val1 = lit1.parseDouble();
        double val2 = lit2.parseDouble();
        // only min and max data restrictions
        if (facet1.equals(OWLFacet.MIN_INCLUSIVE)) {
            return val1 >= val2;
        }
        if (facet1.equals(OWLFacet.MAX_INCLUSIVE)) {
            return val1 <= val2;
        }
        return false;
    }


    private Set<Hypothesis> removeDuplicates(Set<Hypothesis> hypotheses) {
		// checking duplicates
		Out.p("\nChecking duplicates");
		List<Hypothesis> listHypotheses = new ArrayList<>(hypotheses);
		Set<Hypothesis> uniqueHypotheses = new HashSet<>();
		for (int i=0; i<listHypotheses.size(); i++) {
			Hypothesis h1 = listHypotheses.get(i);
			boolean hasEqual = false;
			for (int j=i+1; j<listHypotheses.size(); j++) {
				Hypothesis h2 = listHypotheses.get(j);
				if (equalSets(h1.axioms, h2.axioms)) {
					hasEqual = true;
					break;
				}				
			}
			if (!hasEqual) {
				uniqueHypotheses.add(h1);
			}
		}
		Out.p(uniqueHypotheses.size() + " / " + 
				hypotheses.size() + " unique by equalSets() hypotheses");
		return uniqueHypotheses;		
	}
	
	
	private static boolean equalSets(Set<? extends Object> set1, 
			Set<? extends Object> set2) {
		if (set1.size() != set2.size()) {
			return false;
		}
		for (Object o1 : set1) {
			boolean hasEqual = false;
			for (Object o2 : set2) {
				if (o1.equals(o2)) {
					hasEqual = true;
				}
				break;
			}
			if (!hasEqual) {
				return false;
			}
		}
		for (Object o2 : set2) {
			boolean hasEqual = false;
			for (Object o1 : set1) {
				if (o1.equals(o2)) {
					hasEqual = true;
				}
				break;
			}
			if (!hasEqual) {
				return false;
			}
		}		
		return true;
	}
	
		
	private Hypothesis cleanHypothesis(Hypothesis hypothesis) {	
		// create a clean hypothesis
		Hypothesis cleanHypo = new Hypothesis(hypothesis);
		double t1 = System.nanoTime();
		cleanHypo.axioms = cleanAxioms(hypothesis.axioms);
		double time = (System.nanoTime() - t1)/1e9;
		cleanHypo.cleanTime = time;
		cleanHypo.length = LengthMetric.length(cleanHypo.axioms);
		cleanHypo.signature = OntologyHandler.getSignature(cleanHypo.axioms);		
		return cleanHypo;
	}
	
	
	private Set<OWLAxiom> cleanAxioms(Set<OWLAxiom> axioms) {
		Set<OWLAxiom> trAxioms = transform(axioms);
		if (trAxioms.equals(axioms)) {
			return trAxioms;
		}
		Set<OWLAxiom> infAxioms = cleanUninformativeAxioms(trAxioms);
		if (infAxioms.equals(axioms)) {
			return infAxioms;
		}
		Set<OWLAxiom> nentAxioms = cleanEntailedAxioms(infAxioms);
		if (nentAxioms.containsAll(axioms)) {
			return axioms;
		}
		return nentAxioms;
	}
	
	
	private Set<OWLAxiom> cleanUninformativeAxioms(Set<OWLAxiom> suspects) {
		Set<OWLAxiom> infAxioms = new HashSet<>(suspects);		
		for (OWLAxiom suspect : suspects) {			
			if (hypoAxioms.contains(suspect)) {
				continue;
			}
			if (entAxioms.contains(suspect)) {
				infAxioms.remove(suspect);
				continue;
			}
			try {
				if (ontologyReasoner.isEntailed(suspect)) {
					entAxioms.add(suspect);
					infAxioms.remove(suspect);
				}
			} catch (Exception e) {
				Out.p(e + HYPOTHESIS_CLEANING_ERROR);
				// assume it is not entailed so we do not loose the axiom
			}
		}		
		return infAxioms;
	}


	private Set<OWLAxiom> cleanEntailedAxioms(Set<OWLAxiom> axioms) {
		Set<OWLAxiom> nentAxioms = new HashSet<>();		
		// initialise a reasoner
		hypothesisHandler.addAxioms(axioms);
		// remove entailed axioms one by one
		// sort axioms by length
		List<OWLAxiom> axiomsList = new LinkedList<>(axioms);
		Collections.sort(axiomsList, new AxiomLengthComparator(SortingOrder.DESC));
		int count = 0;
		for (OWLAxiom axiom : axiomsList) {
			count++;
			if (count % 100 == 0) {
				Out.p(count + " / " + axioms.size() + " axioms checked for cleaning");
			}
			hypothesisHandler.removeAxiom(axiom);
			hypothesisReasoner.flush();
			if (!hypothesisReasoner.isConsistent()) {
				// assume it is not entailed so we do not loose the axiom
				hypothesisHandler.addAxiom(axiom);
				nentAxioms.add(axiom);
				continue;
			}
			try {
				if (!hypothesisReasoner.isEntailed(axiom)) {
					hypothesisHandler.addAxiom(axiom);
					nentAxioms.add(axiom);
				}
			} catch (Exception e) {
				Out.p(e + HYPOTHESIS_CLEANING_ERROR);
				// assume it is not entailed so we do not loose the axiom
				hypothesisHandler.addAxiom(axiom);
				nentAxioms.add(axiom);
			}
		}		
		hypothesisHandler.removeAxioms();
		return nentAxioms;
	}
	
	
	private Set<OWLAxiom> transform(Set<OWLAxiom> axioms) {
		OPlusGenerator transformation = new OPlusGenerator(hypothesisHandler.getDataFactory(), 
				OPlusSplitting.TOP_LEVEL);
		Set<OWLAxiom> trAxioms = transformation.transform(axioms);
		// transformation can cause loosing the class-instance mapping for ALC
		Set<OWLAxiom> clAxioms = new HashSet<>();
		for (OWLAxiom ax : trAxioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
				OWLClass subClass = conceptBuilder.getClassByExpression(axiom.getSubClass());
				OWLClass superClass = conceptBuilder.getClassByExpression(axiom.getSuperClass());
				if (subClass != null && superClass != null) {
					clAxioms.add(axiom);
				}
			}
		}
		return clAxioms;
	}
	
	
	// works for single axioms:
	// filter A implies C if there are A implies B, B implies C (strict, non-equivalent)	
	private Set<OWLAxiom> cleanTransitive(Set<OWLAxiom> axioms) {		
		Map<OWLAxiom, OWLAxiom> axiomsMap = hypothesisHandler.mapAxiomsToCodedAxioms(axioms);
		Set<OWLAxiom> codedAxioms = axiomsMap.keySet();
		// update a reasoner
		hypothesisHandler.addAxioms(codedAxioms);		
		hypothesisReasoner.flush();			
		// keep non-transitive only
		Set<OWLAxiom> toKeep = new HashSet<>();
		for (OWLAxiom axiom : codedAxioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLClassExpression lhs = OntologyHandler.getSubClass(axiom);				
				OWLClassExpression rhs = OntologyHandler.getSuperClass(axiom);
				Set<OWLClass> eqs = hypothesisReasoner.getEquivalentClasses(lhs).getEntities();
				Set<OWLClass> directs = hypothesisReasoner.getSuperClasses(lhs, true).getFlattened();
				// direct super classes or equivalent classes
				if (directs.contains(rhs) || eqs.contains(rhs)) {
					toKeep.add(axiomsMap.get(axiom));
				} 
			}
			if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
				OWLObjectPropertyExpression lhs = OntologyHandler.getSubProperty(axiom);				
				OWLObjectPropertyExpression rhs = OntologyHandler.getSuperProperty(axiom);
				Set<OWLObjectPropertyExpression> eqs = hypothesisReasoner.getEquivalentObjectProperties(lhs).getEntities();
				Set<OWLObjectPropertyExpression> directs = hypothesisReasoner.getSuperObjectProperties(lhs, true).getFlattened();
				// direct super properties or equivalent properties
				if (directs.contains(rhs) || eqs.contains(rhs)) {
					toKeep.add(axiomsMap.get(axiom));
				}
			}
		}
		// update a reasoner
		hypothesisHandler.removeAxioms(codedAxioms);		
		hypothesisReasoner.flush();
		return toKeep;
	}
	
	
	// works for single axioms:
	// filter R some C implies R some D if there is C implies D
	public List<Hypothesis> filterExistentials(List<Hypothesis> hypos) {
		Set<OWLAxiom> axioms = OntologyHandler.hyposToAxioms(hypos);
		List<Hypothesis> toKeep = new LinkedList<>();
		for (Hypothesis h : hypos) {
			Set<OWLAxiom> haxs = h.axioms;			
			for (OWLAxiom hax : haxs) {
				if (!isRedundantExistentialAxiom(hax, axioms)) {
					toKeep.add(h);
					break;
				}
			}			
		}		
		return toKeep;
	}
	
	
	private boolean isRedundantExistentialAxiom(OWLAxiom hax, Set<OWLAxiom> axioms) {
		OWLClassExpression lhs = OntologyHandler.getSubClass(hax);		
		OWLClassExpression rhs = OntologyHandler.getSuperClass(hax);
		// check whether existentials are on both sides
		if (lhs.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)
				&& rhs.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
			Set<OWLClassExpression> lhsExprs = lhs.getNestedClassExpressions();
			Set<OWLClassExpression> rhsExprs = rhs.getNestedClassExpressions();					
			// find and compare the second biggest expression
			List<OWLClassExpression> lhsExprsList = new LinkedList<>(lhsExprs);
			List<OWLClassExpression> rhsExprsList = new LinkedList<>(rhsExprs);
			Collections.sort(lhsExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
			Collections.sort(rhsExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
			OWLClassExpression lhsSub = lhsExprsList.get(1);
			OWLClassExpression rhsSub = rhsExprsList.get(1);
			Set<OWLEntity> lhsSig = new HashSet<>(lhs.getSignature());
			lhsSig.removeAll(lhsSub.getSignature());
			Set<OWLEntity> rhsSig = new HashSet<>(rhs.getSignature());
			rhsSig.removeAll(rhsSub.getSignature());
			// the same role on both sides
			if (lhsSig.equals(rhsSig)) {
				for (OWLAxiom ax : axioms) {
					OWLClassExpression lhsAx = OntologyHandler.getSubClass(ax);		
					OWLClassExpression rhsAx = OntologyHandler.getSuperClass(ax);
					if (lhsSub.equals(lhsAx) && rhsSub.equals(rhsAx)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	// works for single axioms:
	// filter A and B implies C if there is A implies B
	public List<Hypothesis> filterConjunctions(List<Hypothesis> hypos) {
		Set<OWLAxiom> axioms = OntologyHandler.hyposToAxioms(hypos);
		List<Hypothesis> toKeep = new LinkedList<>();
		for (Hypothesis h : hypos) {
			Set<OWLAxiom> haxs = h.axioms;			
			for (OWLAxiom hax : haxs) {
				OWLClassExpression lhs = OntologyHandler.getSubClass(hax);		
				OWLClassExpression rhs = OntologyHandler.getSuperClass(hax);
				if (!hasRedundantConjunction(lhs, axioms)
						&& !hasRedundantConjunction(rhs, axioms)
						&& !isRedundantConjunctionAxiom(lhs, rhs, axioms)) {
					toKeep.add(h);
					break;
				}					
			}			
		}		
		return toKeep;
	}
	
	
	private boolean isRedundantConjunctionAxiom(OWLClassExpression lhs, 
			OWLClassExpression rhs, Set<OWLAxiom> axioms) {
		if (lhs.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {
			List<OWLClassExpression> exExprsList = 
					new LinkedList<>(lhs.getNestedClassExpressions());
			Collections.sort(exExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
			OWLClassExpression A = exExprsList.get(1);
			OWLClassExpression B = exExprsList.get(2);
			// check if A is RHS or B is RHS
			if (rhs.equals(A) || rhs.equals(B)) {
				return true;
			}
			// check if A implies RHS or B implies RHS
			for (OWLAxiom ax : axioms) {
				OWLClassExpression lax = OntologyHandler.getSubClass(ax);		
				OWLClassExpression rax = OntologyHandler.getSuperClass(ax);
				if (rax.equals(rhs) && (lax.equals(A) || lax.equals(B))) {
					return true;
				}
			}
		}		
		return false;
	}
	
	
	private boolean hasRedundantConjunction(OWLClassExpression lhs, Set<OWLAxiom> axioms) {
		Set<OWLClassExpression> lhsExprs = lhs.getNestedClassExpressions();
		for (OWLClassExpression ex : lhsExprs) {
			if (ex.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {											
				List<OWLClassExpression> exExprsList = 
						new LinkedList<>(ex.getNestedClassExpressions());
				Collections.sort(exExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
				OWLClassExpression A = exExprsList.get(1);
				OWLClassExpression B = exExprsList.get(2);
				// check if A implies B or B implies A	
				for (OWLAxiom ax : axioms) {
					OWLClassExpression lax = OntologyHandler.getSubClass(ax);		
					OWLClassExpression rax = OntologyHandler.getSuperClass(ax);
					if ( (lax.equals(A) && rax.equals(B))
							|| (lax.equals(B) && rax.equals(A)) ) {						
						return true;
					}										
				}
			}
		}
		return false;
	}
	
	
	public List<Hypothesis> filterDisjunctions(List<Hypothesis> hypos) {
		Set<OWLAxiom> axioms = OntologyHandler.hyposToAxioms(hypos);
		List<Hypothesis> toKeep = new LinkedList<>();
		for (Hypothesis h : hypos) {
			Set<OWLAxiom> haxs = h.axioms;			
			for (OWLAxiom hax : haxs) {
				OWLClassExpression lhs = OntologyHandler.getSubClass(hax);		
				OWLClassExpression rhs = OntologyHandler.getSuperClass(hax);
				if (!hasRedundantDisjunction(lhs, axioms)
						&& !hasRedundantDisjunction(rhs, axioms)
						&& !isRedundantDisjunctionAxiom(lhs, rhs, axioms)) {
					toKeep.add(h);
					break;
				}					
			}			
		}		
		return toKeep;
	}
	
	
	private boolean isRedundantDisjunctionAxiom(OWLClassExpression lhs, 
			OWLClassExpression rhs, Set<OWLAxiom> axioms) {
		if (rhs.getClassExpressionType().equals(ClassExpressionType.OBJECT_UNION_OF)) {
			List<OWLClassExpression> exExprsList = 
					new LinkedList<>(rhs.getNestedClassExpressions());
			Collections.sort(exExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
			OWLClassExpression A = exExprsList.get(1);
			OWLClassExpression B = exExprsList.get(2);
			// check if A is LHS or B is LHS
			if (lhs.equals(A) || lhs.equals(B)) {
				return true;
			}
			// check if A implies RHS or B implies RHS
			for (OWLAxiom ax : axioms) {
				OWLClassExpression lax = OntologyHandler.getSubClass(ax);		
				OWLClassExpression rax = OntologyHandler.getSuperClass(ax);
				if (lax.equals(lhs) && (rax.equals(A) || rax.equals(B))) {
					return true;
				}
			}
		}		
		return false;
	}
	
	
	private boolean hasRedundantDisjunction(OWLClassExpression lhs, Set<OWLAxiom> axioms) {
		Set<OWLClassExpression> lhsExprs = lhs.getNestedClassExpressions();
		for (OWLClassExpression ex : lhsExprs) {
			if (ex.getClassExpressionType().equals(ClassExpressionType.OBJECT_UNION_OF)) {											
				List<OWLClassExpression> exExprsList = 
						new LinkedList<>(ex.getNestedClassExpressions());
				Collections.sort(exExprsList, new OWLClassExpressionSignatureComparator(SortingOrder.DESC));
				OWLClassExpression A = exExprsList.get(1);
				OWLClassExpression B = exExprsList.get(2);
				// check if A implies B or B implies A	
				for (OWLAxiom ax : axioms) {
					OWLClassExpression lax = OntologyHandler.getSubClass(ax);		
					OWLClassExpression rax = OntologyHandler.getSuperClass(ax);
					if ( (lax.equals(A) && rax.equals(B))
							|| (lax.equals(B) && rax.equals(A)) ) {						
						return true;
					}										
				}
			}
		}
		return false;
	}


}
