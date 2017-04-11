package io.dlminer.exp;

import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerName;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by slava on 11/04/17.
 */
public class GSOutput {

    public int hypoNum;
    public int gsAxiomNum;
    public int gsAxiomEntNum;
    public int gsAxiomEntTBoxNum;

    public double recall;

    public Set<OWLAxiom> gsAxiomMissed;


    public void compareToGoldStandard(Set<OWLAxiom> hypoAxioms,
                                      Set<OWLAxiom> gsAxioms, Set<OWLAxiom> tboxAxioms,
                                      ReasonerName reasonerName) {
        hypoNum = hypoAxioms.size();
        gsAxiomNum = gsAxioms.size();
        List<Set<OWLAxiom>> resList = OntologyHandler.getEntailedAxiomsUsingModules(
                hypoAxioms, gsAxioms, tboxAxioms, true, reasonerName);
        Set<OWLAxiom> gsAxiomsEntailedTBox = resList.get(2);
        gsAxiomEntTBoxNum = gsAxiomsEntailedTBox.size();
        Set<OWLAxiom> gsAxiomsEntailed = new HashSet<>(resList.get(1));
        gsAxiomMissed = new HashSet<>(gsAxioms);
        gsAxiomMissed.removeAll(gsAxiomsEntailed);
        recall = (double)gsAxiomsEntailed.size() / gsAxioms.size();
        gsAxiomsEntailed.removeAll(gsAxiomsEntailedTBox);
        gsAxiomEntNum = gsAxiomsEntailed.size();
    }



    public static void main(String[] args) {
        OntologyHandler handler1 = new OntologyHandler(args[0]);
        OntologyHandler handler2 = new OntologyHandler(args[1]);
        handler1.removeAxioms(handler1.getABoxAxioms());
        handler1.addAxioms(handler2.getABoxAxioms());
        handler1.saveOntology(new File(args[0]));
    }

}
