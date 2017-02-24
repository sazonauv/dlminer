package io.dlminer.ont;

import io.dlminer.print.Out;

import java.util.ConcurrentModificationException;
import java.util.Timer;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class RunTimer {
	

	private OntologyHandler handler;
	
	private ReasonerName reasonerName;
		
	private boolean consistent;
	

	public RunTimer(OntologyHandler handler, ReasonerName reasonerName) {
		super();		
		this.handler = handler;
		this.reasonerName = reasonerName;		
	}

		
	private long measureTime(OWLReasoner reasoner) {				
		long start = System.currentTimeMillis();		
		try {
			// check consistency
			if (reasoner.isConsistent()) {
				consistent = true;
				Out.p("Consistent");
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
						InferenceType.OBJECT_PROPERTY_HIERARCHY,
						InferenceType.CLASS_ASSERTIONS, 
						InferenceType.OBJECT_PROPERTY_ASSERTIONS);
			} else {
				consistent = false;
				Out.p("Inconsistent");
			}		
		} catch (Exception e) {
			Out.p(e);
			return finishWork(reasoner, start);
		}
		return finishWork(reasoner, start);
	}

	private long finishWork(OWLReasoner reasoner, long start) {
		long end = System.currentTimeMillis();
		long time = (end-start);
		if (reasoner != null) {
			try {
				reasoner.interrupt();				
			} catch (Exception e) {
				Out.p(e);				
			} finally {
				reasoner.dispose();
			}
		}
		return time;		
	}

	
	/**
	 * @param timeout in milliseconds
	 * @return execution time
	 * @throws ConcurrentModificationException
	 */
	public long measureTime(long timeout) {
		// load a reasoner
		OWLReasoner reasoner = null;
		try {
			reasoner = ReasonerLoader.initReasoner(reasonerName, handler.getOntology());		
		} catch (Exception e) {
			Out.p(e);			
		}		
		// set a timeout
		Timer timer = new Timer(true);
		timer.schedule(new InterruptReasonerTask(reasoner), timeout);
		return measureTime(reasoner);
	}


	public boolean isConsistent() {
		return consistent;
	}


}
