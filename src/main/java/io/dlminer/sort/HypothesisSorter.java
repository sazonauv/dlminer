package io.dlminer.sort;

import io.dlminer.graph.Graph;
import io.dlminer.graph.Node;
import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEntry;
import io.dlminer.ont.HypothesisReasoner;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class HypothesisSorter {
	
	private Collection<Hypothesis> hypotheses;
	
	private List<Hypothesis> hypothesesByLength;
	
	private List<Hypothesis> hypothesesByFitness;
	
	private List<Hypothesis> hypothesesByBraveness;
	
	private List<Hypothesis> hypothesesByInterest;
	
	private Graph<Hypothesis> strengthGraph;
	private Map<OWLAxiom, Hypothesis> axiomHypothesisMap;
	
	private Map<Hypothesis, Set<String>> hypothesisToEntitiesMap;
	
	private Map<String, Hypothesis> idToHypothesisMap;
	
		
	
	// internal reasoner and handler
	private OWLReasoner reasoner;
	private OntologyHandler handler;
	private boolean isReasonerError;
	
	
	// internal hypothesis reasoner
	private HypothesisReasoner hypoReasoner;
	
	
	
	public HypothesisSorter(Map<String, HypothesisEntry> hypothesisIdToEntryMap) {
		initHypotheses(hypothesisIdToEntryMap);		
	}
	

	
	private void initHypotheses(Map<String, HypothesisEntry> hypothesisIdToEntryMap) {
		hypotheses = new ArrayList<>(hypothesisIdToEntryMap.size());
		hypothesisToEntitiesMap = new HashMap<>();
		idToHypothesisMap = new HashMap<>();
		for (String id : hypothesisIdToEntryMap.keySet()) {
			Hypothesis hypothesis = new Hypothesis();
			hypothesis.id = id;
			HypothesisEntry entry = hypothesisIdToEntryMap.get(id);
			hypothesis.support = (double) entry.getFitness();
			hypothesis.assumption = (double) entry.getBraveness();
			hypothesis.noveltyApprox = (double) entry.getInterest();
			hypothesis.length = entry.getLength();
			hypotheses.add(hypothesis);
			hypothesisToEntitiesMap.put(hypothesis, entry.getEntities());
			idToHypothesisMap.put(id, hypothesis);
		}		
	}
	
	
	
	public HypothesisSorter(Collection<Hypothesis> hypotheses) {
		this.hypotheses = hypotheses;
		idToHypothesisMap = new HashMap<>();
		for (Hypothesis hypothesis : hypotheses) {
			idToHypothesisMap.put(hypothesis.id, hypothesis);
		}
	}
	
	
	private void initHypothesisReasoner() {
		hypoReasoner = new HypothesisReasoner(hypotheses);		
	}

	private void initReasoner() {
		// create a handler		
		handler = new OntologyHandler();		
		// init a reasoner
		reasoner = null; 
		try {
			// Hermit is required because Pellet is not updated once axioms are added
			reasoner = ReasonerLoader.initReasoner(ReasonerName.HERMIT, 
					handler.getOntology());
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
					InferenceType.OBJECT_PROPERTY_HIERARCHY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public void sort() {
		initLists();			
		Collections.sort(hypothesesByLength, new HypoLengthComparator(SortingOrder.ASC));
		Collections.sort(hypothesesByFitness, new HypoFitLowComparator(SortingOrder.DESC));
		Collections.sort(hypothesesByBraveness, new HypoBravLowComparator(SortingOrder.ASC));
		Collections.sort(hypothesesByInterest, new HypoInterestLowComparator(SortingOrder.DESC));		
		
	}
	
	// ArrayList is chosen because sorted lists are not going to be updated, only read
	private void initLists() {
		hypothesesByLength = new ArrayList<>(hypotheses);
		hypothesesByFitness = new ArrayList<>(hypotheses);
		hypothesesByBraveness = new ArrayList<>(hypotheses);
		hypothesesByInterest = new ArrayList<>(hypotheses);		
	}
		
	
	
	public static List<Hypothesis> sortBySupportAssumption(Collection<Hypothesis> hypotheses, double cost) {
		List<Hypothesis> sortedHypotheses = new ArrayList<>(hypotheses);
		Collections.sort(sortedHypotheses, new HypoFitLowBravLowComparator(cost, SortingOrder.DESC));
		return sortedHypotheses;
	}
	
	
	public static List<Hypothesis> sortByFitnessBraveness(Collection<Hypothesis> hypotheses, double cost) {
		List<Hypothesis> sortedHypotheses = new ArrayList<>(hypotheses);
		Collections.sort(sortedHypotheses, new HypoFitBravComparator(cost, SortingOrder.DESC));
		return sortedHypotheses;
	}
	
	
	
	public static List<Hypothesis> sortByLength(Collection<Hypothesis> hypotheses) {
		List<Hypothesis> sortedHypotheses = new ArrayList<>(hypotheses);
		Collections.sort(sortedHypotheses, new HypoLengthComparator(SortingOrder.ASC));
		return sortedHypotheses;
	}
	
	
	public static List<Hypothesis> sortByLift(Collection<Hypothesis> hypotheses) {
		List<Hypothesis> sortedHypotheses = new ArrayList<>(hypotheses);
		Collections.sort(sortedHypotheses, new HypoLiftComparator(SortingOrder.DESC));
		return sortedHypotheses;
	}
	

	
	public void orderByStrength() {		
		initReasoner();
		mapAxiomsToHypotheses();
		buildStrengthGraph();
	}
	
	
	
	private void mapAxiomsToHypotheses() {
		// map axioms to their hypotheses
		axiomHypothesisMap = new HashMap<>();
		for (Hypothesis h : hypotheses) {
			if (h.axioms.size() == 1) {
				OWLAxiom ax = h.getFirstAxiom();
				if (ax != null) {			
					axiomHypothesisMap.put(ax, h);
				}
			}
		}
	}
	
		
	
	private void buildStrengthGraph() {
		// parents are stronger than children
		strengthGraph = new Graph<>(hypotheses);
		// find the maximal size
		int maxSize = 0;
		for (Hypothesis h : hypotheses) {
			if (h.axioms.size() > maxSize) {
				maxSize = h.axioms.size();
			}
		}
		// first order axioms, then all other hypotheses (optimisation)
		for (int size=1; size<=maxSize; size++) {
			orderHypothesesOfSize(size);
		}
	}
	
	
	
	private void orderHypothesesOfSize(int size) {
		Out.p("\nOrdering hypotheses of size = " + size);
		int count = 0;
		for (Hypothesis h1 : hypotheses) {			
			count++;
			// debug
			if (count % 100 == 0) {
				Out.p(count + " / " + hypotheses.size() + " hypotheses ordered by strength");
			}
			if (h1.axioms.size() > size) {
				continue;
			}
			double start = System.nanoTime();
			for (Hypothesis h2 : hypotheses) {
				if (h2.axioms.size() > size) {
					continue;
				}
				// already processed
				if (h1.axioms.size() < size && h2.axioms.size() < size) {
					continue;
				}
				if (!h1.equals(h2) 
						&& isComparableTo(h1, h2)
						&& !strengthGraph.hasDescendant(h1, h2)) {
					if (isStrongerThan(h1, h2)) {						
						strengthGraph.addChild(h1, h2);
						strengthGraph.removeChildFromAncestors(h1, h2);
					}
				}
			}
			double time = (System.nanoTime() - start)/1e9;
			// strength comparison time
			h1.strengthTime = (h1.strengthTime == null) ? time : h1.strengthTime + time;						
		}
	}
	
	
	
	private boolean isComparableTo(Hypothesis h1, Hypothesis h2) {
		if (!h1.hasSignatureOverlapAtLeast(h2, 2)) {
			return false;
		}
		if (!h1.containsRoleAxioms() && h2.containsRoleAxioms()) {
			return false;
		}
		return true;
	}
	
	
	
	private boolean isStrongerThan(Hypothesis h1, Hypothesis h2) {
		if (h1.axioms.containsAll(h2.axioms)) {
			return true;
		}
		if (hasStrongerOrEqualAxioms(h1, h2)) {
			return true;
		}
		return isEntailed(h1, h2);
	}
	
	
	private boolean isEntailed(Hypothesis h1, Hypothesis h2) {
		if (!handler.containsAxioms(h1.axioms)) {
			isReasonerError = false;
			handler.removeAxioms();			
			handler.addAxioms(h1.axioms);
			try {
				reasoner.flush();
			} catch (Exception e) {
				// usually irregular roles
				isReasonerError = true;
			}			
		}
		if (isReasonerError || !reasoner.isConsistent()) {
			return false;
		}
		return reasoner.isEntailed(h2.axioms);
	}
	
	
	
	private boolean hasStrongerOrEqualAxioms(Hypothesis h1, Hypothesis h2) {
		for (OWLAxiom ax2 : h2.axioms) {
			Hypothesis hax2 = axiomHypothesisMap.get(ax2);
			boolean found = false;
			for (OWLAxiom ax1 : h1.axioms) {
				if (ax1.equals(ax2)) {
					found = true;
					break;
				}
				Hypothesis hax1 = axiomHypothesisMap.get(ax1);
				if (strengthGraph.hasDescendant(hax1, hax2)) {
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
		
		
	
	public void setStrengthRanks() {
		for (Hypothesis h : strengthGraph.getLabels()) {
			h.strength = (double) strengthGraph.countAncestors(h);
		}
	}
	
	
	

	public void indexByEntities() {	
		if (hypothesisToEntitiesMap != null 
				&& !hypothesisToEntitiesMap.isEmpty()) {
			return;
		}
		hypothesisToEntitiesMap = new HashMap<>();
		for (Hypothesis hypo : hypotheses) {
			Set<String> keywords = new HashSet<>();
			for (OWLEntity ent : hypo.signature) {				
				keywords.add(getKeyword(ent));
			}
			hypothesisToEntitiesMap.put(hypo, keywords);
		}		
	}
	
	
	
	public Map<Hypothesis, Set<String>> getHypothesisToEntitiesMap() {
		return hypothesisToEntitiesMap;
	}


	
	private String getKeyword(OWLEntity entity) {
		IRI iri = entity.getIRI();
		String str = iri.toString().replaceFirst(iri.getNamespace(), "");
		str = str.replaceFirst(":", "");
		str = str.replaceFirst("#", "");
		return str;
	}
	
	
	
	public String nextTo(String id,
			Navigation navigation,
			SortingObjective objective,
			String... entities) {
		Hypothesis hypothesis = idToHypothesisMap.get(id);
		return nextTo(hypothesis, navigation, objective, entities).id;
	}
	
	
	public Hypothesis nextTo(Hypothesis hypothesis,
			Navigation navigation,
			SortingObjective objective,
			String... entities) {
		List<Hypothesis> ordering = null;
		switch (objective) {
		case GENERALITY:
			return nextByGenerality(hypothesis, navigation, entities);
		case FITNESS:
			ordering = hypothesesByFitness;
			break;						
		case BRAVENESS:
			ordering = hypothesesByBraveness;
			break;
		case INTEREST:
			ordering = hypothesesByInterest;
			break;
		case LENGTH:
			ordering = hypothesesByLength;
			break;			
		}		
		return nextByNumericObjective(hypothesis, ordering, navigation, entities);
	}
	
	

	private Hypothesis nextByNumericObjective(Hypothesis hypothesis, 
			List<Hypothesis> ordering, Navigation navigation, String... entities) {	
		if (ordering == null || ordering.isEmpty()) {
			return hypothesis;
		}
		switch (navigation) {
		case BEST:
			return getMinByNumericObjective(ordering, entities);
		case WORST:
			return getMaxByNumericObjective(ordering, entities);
		case WORSE:
			return getHigherByNumericObjective(hypothesis, ordering, entities);
		case BETTER:
			return getLowerByNumericObjective(hypothesis, ordering, entities);		
		}
		return hypothesis;		
	}
	
	
	private Hypothesis getMinByNumericObjective(List<Hypothesis> ordering, String... entities) {
		if (entities == null || entities.length == 0) {
			return ordering.get(0);
		}
		for (Hypothesis hypo : ordering) {
			Set<String> keywords = hypothesisToEntitiesMap.get(hypo);
			for (String entity : entities) {
				if (keywords.contains(entity)) {
					return hypo;
				}
			}
		}
		return ordering.get(0);
	}
	
	
	private Hypothesis getMaxByNumericObjective(List<Hypothesis> ordering, String... entities) {
		int last = ordering.size() - 1;
		if (entities == null || entities.length == 0) {
			return ordering.get(last);
		}
		for (int i=last; i>=0; i--) {
			Hypothesis hypo = ordering.get(i);
			Set<String> keywords = hypothesisToEntitiesMap.get(hypo);
			for (String entity : entities) {
				if (keywords.contains(entity)) {
					return hypo;
				}
			}
		}
		return ordering.get(last);
	}
	
	
	private Hypothesis getHigherByNumericObjective(Hypothesis hypothesis, 
			List<Hypothesis> ordering, String... entities) {
		if (hypothesis == null) {
			return getMaxByNumericObjective(ordering, entities);
		}
		int position = ordering.indexOf(hypothesis);
		int last = ordering.size() - 1;
		// the end of the list
		if (position < 0 || position == last) {
			return hypothesis;
		}
		if (entities == null || entities.length == 0) {			
			return ordering.get(position + 1);
		}
		for (int i=position+1; i<=last; i++) {
			Hypothesis hypo = ordering.get(i);
			Set<String> keywords = hypothesisToEntitiesMap.get(hypo);
			for (String entity : entities) {
				if (keywords.contains(entity)) {
					return hypo;
				}
			}
		}
		return hypothesis;
	}
	
	
	
	private Hypothesis getLowerByNumericObjective(Hypothesis hypothesis, 
			List<Hypothesis> ordering, String... entities) {
		if (hypothesis == null) {
			return getMinByNumericObjective(ordering, entities);
		}
		int position = ordering.indexOf(hypothesis);		
		// the beginning of the list
		if (position <= 0) {
			return hypothesis;
		}
		if (entities == null || entities.length == 0) {			
			return ordering.get(position - 1);
		}
		for (int i=position-1; i>=0; i--) {
			Hypothesis hypo = ordering.get(i);
			Set<String> keywords = hypothesisToEntitiesMap.get(hypo);
			for (String entity : entities) {
				if (keywords.contains(entity)) {
					return hypo;
				}
			}
		}
		return hypothesis;
	}
	
	
	
	private Hypothesis nextByGenerality(Hypothesis hypothesis, 
			Navigation navigation, String... entities) {
		if (strengthGraph == null || strengthGraph.isEmpty()) {
			return hypothesis;
		}
		switch (navigation) {
		case BEST:
			return getMinByGenerality(hypothesis, entities);
		case WORST:
			return getMaxByGenerality(hypothesis, entities);
		case WORSE:
			return getHigherByGenerality(hypothesis, entities);
		case BETTER:
			return getLowerByGenerality(hypothesis, entities);		
		}
		return hypothesis;
	}


	private Hypothesis getMinByGenerality(Hypothesis hypothesis, 
			String... entities) {		
		if (hypothesis == null) {
			return hypothesis;
		}						
		return strengthGraph.getHighestAncestor(hypothesis, hypothesisToEntitiesMap, entities);
	}


	private Hypothesis getMaxByGenerality(Hypothesis hypothesis, 
			String... entities) {
		if (hypothesis == null) {
			return hypothesis;
		}						
		return strengthGraph.getLowestDescendant(hypothesis, hypothesisToEntitiesMap, entities);
	}


	private Hypothesis getHigherByGenerality(Hypothesis hypothesis,
			String... entities) {
		if (hypothesis == null) {
			return hypothesis;
		}			
		return strengthGraph.getLowerDescendant(hypothesis, hypothesisToEntitiesMap, entities);
	}


	private Hypothesis getLowerByGenerality(Hypothesis hypothesis,
			String... entities) {
		if (hypothesis == null) {
			return hypothesis;
		}						
		return strengthGraph.getHigherAncestor(hypothesis, hypothesisToEntitiesMap, entities);
	}
	
	
	
	
	public void test() {
		Out.p("\nTesting the hypotheses browser");
		Navigation[] navigations = new Navigation[]{
				Navigation.WORSE, Navigation.BETTER, 
				Navigation.WORST, Navigation.BEST
		};
		SortingObjective[] objectives = new SortingObjective[]{
				SortingObjective.BRAVENESS, 
				SortingObjective.FITNESS,
//				SortingObjective.GENERALITY, 
				SortingObjective.INTEREST,
				SortingObjective.LENGTH
		};
		List<String> entities = new ArrayList<>();
		for (Set<String> hypoEntities : hypothesisToEntitiesMap.values()) {
			entities.addAll(hypoEntities);
		}
		Hypothesis hypothesis = hypothesesByLength.get((int)(Math.random()*hypotheses.size()));
		String id = hypothesis.id;
		int nSteps = 200;
		for (int i=0; i<nSteps; i++) {			
			Navigation navigation = navigations[(int)(Math.random()*navigations.length)];
			SortingObjective objective = objectives[(int)(Math.random()*objectives.length)];
			String entity = entities.get((int)(Math.random()*entities.size()));
			id = nextTo(id, navigation, objective, entity);
			hypothesis = idToHypothesisMap.get(id);
			Out.p(i + ": (" + navigation + ", " + objective + ", " + entity + ") = \n\t["
					+ "id=" + hypothesis.id 
					+ ", fit=" + hypothesis.support
					+ ", bra=" + hypothesis.assumption
					+ ", size=" + hypothesis.length
					+ ", inter=" + hypothesis.noveltyApprox
//					+ ", stren=" + hypothesis.strength 
					+ ", sig=" + hypothesis.signature + "]");
		}
		
		
	}



	
	
			
}
