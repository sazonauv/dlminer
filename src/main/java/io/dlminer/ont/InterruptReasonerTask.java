package io.dlminer.ont;

import java.util.TimerTask;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class InterruptReasonerTask extends TimerTask {
	
	private OWLReasoner reasoner;
		
	public InterruptReasonerTask(OWLReasoner reasoner) {
		super();
		this.reasoner = reasoner;
	}

	@Override
	public void run() {
		if (reasoner != null) {
			try {
				System.out.println("Timeout is exceeded");
				reasoner.interrupt();				
			} catch (Exception e) {
				System.out.println("Exception while reasoner interruption");
			} finally {
				reasoner.dispose();
			}
		}
	}

}
