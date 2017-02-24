package io.dlminer.learn;

import io.dlminer.ont.DepthMetric;
import io.dlminer.ont.LengthMetric;
import io.dlminer.ont.OWLObjectPropertyChain;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.sort.Distance;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;



public class Hypothesis {
	
	// generated id
	public String id;
	public Integer rank;
	
	// axioms	
	public Set<OWLAxiom> axioms;
	public Set<OWLAxiom> codedAxioms;
	public Set<OWLAxiom> definitions;	
	public Set<OWLEntity> signature;
	public Integer length;
	public Integer depth;

	// logical measures
	public Double noveltyApprox;
	public Double novelty;	
	public Double dissimilarityApprox;
	public Double dissimilarity;
	public Double strength;
	public boolean isConsistent;
	
	// statistical measures
	public Double support;
	public Double assumption;	
	public Double fitness;	
	public Double braveness;	
	public Double precision;
	public Double recall;
	public Double lift;
	public Double leverage;
	public Double addedValue;
	public Double jaccard;
	public Double certaintyFactor;
	public Double klosgen;
	public Double conviction;
	public Double convictionNeg;
	public Double convictionQue;
	public Double shapiro;
	public Double cosine;
	public Double informGain;
	public Double sebag;
	public Double contradiction;
	public Double oddMultiplier;
	public Double linearCorrelation;
	public Double jmeasure;
	
	// contrapositive measures
	public Double mainSupport;
	public Double mainAssumption;
	public Double mainContradiction;
	public Double mainPrecision;
	public Double mainLift;
	public Double mainConvictionNeg;
	public Double mainConvictionQue;
	
	
	// ranking
	public Double[] measures = null;
	public Double[] scaledMeasures = null;
	public Double quality;
	
	// comparison
	public Double noveltySum;
	public Double fitnessSum;
	public Double bravenessSum;
	
	// performance
	public Double fitnessTime;
	public Double bravenessTime;
	public Double noveltyTime;
	public Double dissimTime;
	public Double strengthTime;
	public Double basicTime;
	public Double mainTime;
	public Double consistTime;
	public Double informTime;
	public Double cleanTime;
	
	
	// concept for faster reasoning
	private OWLClassExpression concept;
		
	
	
	public Hypothesis() {
		this.id = UUID.randomUUID().toString();
	}
	
	
	public Hypothesis(Set<OWLAxiom> axioms, Set<OWLAxiom> codedAxioms, Set<OWLAxiom> definitions) {
		this.axioms = axioms;		
		this.codedAxioms = codedAxioms;
		this.definitions = definitions;
		this.length = LengthMetric.length(axioms);
		this.depth = DepthMetric.depth(axioms);
		this.signature = OntologyHandler.getSignature(axioms);
		this.id = UUID.randomUUID().toString();
	}
	

	public Hypothesis(Hypothesis h) {
		// generated id
		this.id = UUID.randomUUID().toString();
		// axioms
		this.axioms = new HashSet<>(h.axioms);
		this.codedAxioms = new HashSet<>(h.codedAxioms);
		this.definitions = new HashSet<>(h.definitions);
		this.signature = new HashSet<>(h.signature);
		this.length = h.length;
		this.depth = h.depth;	
		// statistical measures
		this.fitness = h.fitness;
		this.support = h.support;
		this.braveness = h.braveness;
		this.assumption = h.assumption;
		this.precision = h.precision;
		this.recall = h.recall;
		this.lift = h.lift;
		this.leverage = h.leverage;
		this.addedValue = h.addedValue;
		this.jaccard = h.jaccard;
		this.certaintyFactor = h.certaintyFactor;
		this.klosgen = h.klosgen;
		this.conviction = h.conviction;
		this.convictionNeg = h.convictionNeg;
		this.convictionQue = h.convictionQue;		
		this.shapiro = h.shapiro;
		this.cosine = h.cosine;
		this.informGain = h.informGain;
		this.sebag = h.sebag;
		this.contradiction = h.contradiction;	
		this.oddMultiplier = h.oddMultiplier;
		this.linearCorrelation = h.linearCorrelation;
		this.jmeasure = h.jmeasure;
		// contrapositive measures
		this.mainSupport = h.mainSupport;
		this.mainAssumption = h.mainAssumption;
		this.mainContradiction = h.mainContradiction;
		this.mainPrecision = h.mainPrecision;
		this.mainLift = h.mainLift;
		this.mainConvictionNeg = h.mainConvictionNeg;
		this.mainConvictionQue = h.mainConvictionQue;
		// logical measures		
		this.noveltyApprox = h.noveltyApprox;
		this.novelty = h.novelty;		
		this.dissimilarityApprox = h.dissimilarityApprox;
		this.dissimilarity = h.dissimilarity;
		this.strength = h.strength;
		this.isConsistent = h.isConsistent;
		// comparison
		this.fitnessSum = h.fitnessSum;
		this.bravenessSum = h.bravenessSum;
		this.noveltySum = h.noveltySum;
		// performance
		this.fitnessTime = h.fitnessTime;
		this.bravenessTime = h.bravenessTime;
		this.noveltyTime = h.noveltyTime;
		this.dissimTime = h.dissimTime;
		this.strengthTime = h.strengthTime;
		this.basicTime = h.basicTime;
		this.mainTime = h.mainTime;
		this.consistTime = h.consistTime;
		this.informTime = h.informTime;
		this.cleanTime = h.cleanTime;
	}

	@Override
	public String toString() {
		if (axioms != null) {
			return axioms.toString();
		}
		return id;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Hypothesis)) {
			return false;
		}
		Hypothesis other = (Hypothesis) obj;
		if (axioms != null && other.axioms != null) {			
			return other.axioms.equals(axioms);					
		}
		return other.id.equals(id);
	}
	
			
	@Override
	public int hashCode() {
		if (axioms != null) {
			return axioms.hashCode();
		}
		return id.hashCode();
	}


	public boolean add(Hypothesis h) {
		boolean result = axioms.addAll(h.axioms);
		this.codedAxioms.addAll(h.codedAxioms);
		this.definitions.addAll(h.definitions);		
		this.signature.addAll(h.signature);
		this.length += h.length;
		this.depth = DepthMetric.depth(axioms);
		// statistical measures
		this.support += h.support;		
		this.assumption += h.assumption;
		this.precision += h.precision;
		this.recall += h.recall;
		this.lift += h.lift;
		this.leverage += h.leverage;
		this.addedValue += h.addedValue;
		this.jaccard += h.jaccard;
		this.certaintyFactor += h.certaintyFactor;
		this.klosgen += h.klosgen;
		this.conviction += h.conviction;
		this.shapiro += h.shapiro;
		this.cosine += h.cosine;
		this.informGain += h.informGain;
		this.sebag += h.sebag;
		this.contradiction += h.contradiction;	
		this.oddMultiplier += h.oddMultiplier;
		this.linearCorrelation += h.linearCorrelation;
		this.jmeasure += h.jmeasure;
		// logical measures		
		this.noveltyApprox = (this.noveltyApprox == null || h.noveltyApprox == null) 
				? null : this.noveltyApprox + h.noveltyApprox;
		this.dissimilarityApprox = (this.dissimilarityApprox == null || h.dissimilarityApprox == null) 
				? null : this.dissimilarityApprox + h.dissimilarityApprox;
		return result;
	}


	public boolean containsClassAxioms() {
		for (OWLAxiom ax : codedAxioms) {
			if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
				return true;
			}
		}		
		return false;
	}
	
		
	public boolean containsRoleAxioms() {
		for (OWLAxiom ax : codedAxioms) {
			if (ax.isOfType(AxiomType.SUB_OBJECT_PROPERTY)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public int countClassAxioms() {
		int count = 0;
		for (OWLAxiom ax : codedAxioms) {
			if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
				count++;
			}
		}		
		return count;
	}	
	
	

	public int countRoleAxioms() {
		int count = 0;
		for (OWLAxiom ax : codedAxioms) {
			if (ax.isOfType(AxiomType.SUB_OBJECT_PROPERTY)) {
				count++;
			}
		}
		return count;
	}
	


		
	public boolean containsNullAxiom() {
		for (OWLAxiom ax : axioms) {
			if (ax == null) {
				return true;
			}
		}
		return false;
	}
	
	
	public void initMeasures() {
		measures = new Double[]{				
				support, -assumption, 
//				precision,
//				fitness, -braveness,
//				lift, conviction
				};
	}
	
	
	
	public void scaleMeasures(double[] maxima) {		
		scaledMeasures = new Double[measures.length];
		for (int i=0; i<measures.length; i++) {
			if (maxima[i] == 0) {
				scaledMeasures[i] = i != 1 ? 0.0 : 1.0;
				continue;
			}
			if (Double.isFinite(measures[i])) {
				scaledMeasures[i] = measures[i]/maxima[i];
			} else if (Double.isInfinite(measures[i])) {
				scaledMeasures[i] = 1.0;
			} else {
				scaledMeasures[i] = 0.0;
			}
			if (i == 1) {
				scaledMeasures[i] += 1;
			}
		}
	}
	
	
	
	public void calculateQuality(Distance distance) {		
		if (distance.equals(Distance.EUCLIDIAN)) {
			quality = calculateEuclidian();
		} else if (distance.equals(Distance.MANHATTAN)) {
			quality = calculateManhattan();
		} else {
			throw new IllegalArgumentException("Please specify the distance metric!");
		}		
	}
	
	
	private double calculateEuclidian() {
		double q = 0;
		for (int i=0; i<scaledMeasures.length; i++) {
			q += scaledMeasures[i]*scaledMeasures[i];
		}
		return Math.sqrt(q);
	}
	
	
	private double calculateManhattan() {
		double q = 0;
		for (int i=0; i<scaledMeasures.length; i++) {
			q += scaledMeasures[i];
		}
		return q;
	}


	public boolean hasSignatureOverlapWith(Hypothesis h) {				
		return hasSignatureOverlapAtLeast(h, 1);
	}
	
	
	
	public boolean hasSignatureOverlapAtLeast(Hypothesis h, int minOverlap) {				
		return HypothesisEvaluator.countIntersection(
				signature, h.signature) >= minOverlap;
	}


	
	public OWLSubClassOfAxiom getFirstClassAxiom() {
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				return (OWLSubClassOfAxiom) ax;
			}
		}
		return null;
	}
	
	
	public OWLAxiom getFirstRoleAxiom() {
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLSubObjectPropertyOfAxiom) {
				return (OWLSubObjectPropertyOfAxiom) ax;
			}
			if (ax instanceof OWLObjectPropertyChain) {
				return (OWLSubPropertyChainOfAxiom) ax;
			}
		}
		return null;
	}


	public OWLAxiom getFirstAxiom() {
		OWLAxiom ax = getFirstClassAxiom();
		if (ax != null) {
			return ax;
		}
		return getFirstRoleAxiom();
	}
	
	
	public OWLClassExpression getConcept() {
		if (concept != null) {
			return concept;
		}
		if (containsRoleAxioms() || !containsClassAxioms()) {
			return null;
		}
		Set<OWLClassExpression> disjs = new HashSet<>();
		OWLDataFactory df = new OWLDataFactoryImpl();
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
				OWLClassExpression disj = df.getOWLObjectUnionOf(
						df.getOWLObjectComplementOf(axiom.getSubClass()),
						axiom.getSuperClass());
				disjs.add(disj);
			}
		}
		if (disjs.size() == 1) {
			for (OWLClassExpression disj : disjs) {
				concept = disj;
				break;
			}
		} else {
			concept = df.getOWLObjectIntersectionOf(disjs);
		}
		return concept;
	}


	
	
	public OWLSubClassOfAxiom getFirstCodedClassAxiom() {
		for (OWLAxiom ax : codedAxioms) {
			if (ax instanceof OWLSubClassOfAxiom) {
				return (OWLSubClassOfAxiom) ax;
			}
		}
		return null;
	}


	public OWLSubObjectPropertyOfAxiom getFirstCodedRoleAxiom() {
		for (OWLAxiom ax : codedAxioms) {
			if (ax instanceof OWLSubObjectPropertyOfAxiom) {
				return (OWLSubObjectPropertyOfAxiom) ax;
			}
		}
		return null;
	}

	
	


					
}
