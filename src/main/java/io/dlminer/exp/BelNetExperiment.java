package io.dlminer.exp;

import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.AxiomConfig;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.HypothesisWriter;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by slava on 11/04/17.
 */
public class BelNetExperiment {


    public static void main(String[] args) throws Exception {

        // check parameters number
        if (args.length != 12) {
            throw new RuntimeException(
                    "You need exactly 12 parameters: ontology path, hypotheses path, csv path, " +
                            "\nmax role depth, max hypotheses number, reasoner timeout, " +
                            "\nmin precision, min support, beam size, max concept length, " +
                            "\nlogic, reasoner name.");
        }


        // process parameters
        File ontFile = new File(args[0]);
        if (ontFile.getName().contains("complete")) {
            return;
        }
        File owlPath = new File(args[1]);
        File csvPath = new File(args[2]);
        Integer roleDepth = Integer.parseInt(args[3]);
        Integer hypothesesNumber = Integer.parseInt(args[4]);
        Integer reasonerTimeout = Integer.parseInt(args[5]);
        Double minPrecision = Double.parseDouble(args[6]);
        Integer minSupport = Integer.parseInt(args[7]);
        Integer beamSize = Integer.parseInt(args[8]);
        Integer maxConceptLength = Integer.parseInt(args[9]);
        Logic logic = null;
        for (Logic l : Logic.values()) {
            if (l.toString().equalsIgnoreCase(args[10])) {
                logic = l;
            }
        }
        ReasonerName reasonerName = null;
        for (ReasonerName rname : ReasonerName.values()) {
            if (rname.toString().equalsIgnoreCase(args[11])) {
                reasonerName = rname;
            }
        }

        // set logging
//        Out.setLog(hypothesesPath);


        // set parameters
        DLMinerInput input = new DLMinerInput(ontFile);
        input.setReasonerName(reasonerName);
        input.setMaxHypothesesNumber(hypothesesNumber);
        input.setReasonerTimeout(reasonerTimeout);
        input.setUseClosedWorldAssumption(true);

        // language bias
        OperatorConfig operatorConfig = input.getOperatorConfig();
        operatorConfig.maxDepth = roleDepth;
        operatorConfig.maxLength = maxConceptLength;
        operatorConfig.minSupport = minSupport;
        operatorConfig.useDataProperties = false;
        operatorConfig.useNegation = true;
        operatorConfig.useDisjunction = true;
        operatorConfig.useUniversalRestriction = true;
        operatorConfig.logic = logic;
        if (logic.equals(Logic.EL)) {
            operatorConfig.useNegation = false;
            operatorConfig.useDisjunction = false;
            operatorConfig.useUniversalRestriction = false;
        }

        AxiomConfig axiomConfig = input.getAxiomConfig();
        axiomConfig.minPrecision = minPrecision;
        axiomConfig.useMinPrecision = true;
        axiomConfig.useCleaning = false;

        // optimisations
        operatorConfig.checkDisjointness = true;
        operatorConfig.useReasonerForAtomicClassInstances = true;


        // run DL-Miner
        DLMiner miner = new DLMiner(input);
        try {
            miner.run();
        } catch (Throwable e) {
            Out.p("\nThe reasoner " + reasonerName + " has failed to process the ontology");
            if (!reasonerName.equals(ReasonerLoader.DEF_REASONER)) {
                Out.p("\tRerun with the default reasoner " + ReasonerLoader.DEF_REASONER);
                input.setOntologyFile(new File(args[0]));
                input.setReasonerName(ReasonerLoader.DEF_REASONER);
                miner.run();
            }
        }

        Out.p("\nStatistics:");
        Out.p(miner.getStats());

        // set numeric ids
        Collection<Hypothesis> hypotheses = miner.getOutput().getHypotheses();
        HypothesisWriter.setNumericIDs(hypotheses);


        // save results
        Out.p("\nSaving hypotheses");
        IRI iri = miner.getOutput().getOntology().getOntologyID().getOntologyIRI().or(IRI.create("unkown"));
        if (!owlPath.exists()) {
            owlPath.mkdirs();
        }


        // save all hypotheses
        if (!csvPath.exists()) {
            csvPath.mkdirs();
        }
//        File csvFile = new File(csvPath, "hypotheses.csv");
//        CSVWriter.saveHypothesesToCSV(hypotheses, csvFile, ontFile.getName());

        // save statistics
        File csvStatsFile = new File(csvPath, "run_stats.csv");
        CSVWriter.saveStatsToCSV(csvStatsFile, ontFile.getName(),
                miner.getStats(), miner.getOutput().getOntology());


        // save hypotheses
        String name = ontFile.getName().replaceAll(".owl", "");
//        File owlFile = new File(owlPath, name + "_h.owl");
//        miner.saveHypotheses(hypotheses, owlFile, iri);

        // compare to the gold standard
        GSOutput gsOutput = compareToGoldStandard(hypotheses, ontFile, reasonerName);

        Out.p("\nMissed " + gsOutput.gsAxiomMissed.size() + " axioms:");
        Out.printAxiomsMS(gsOutput.gsAxiomMissed);

        OntologyHandler misHandler = new OntologyHandler(gsOutput.gsAxiomMissed);
        File misFile = new File(owlPath, name + "_mis.owl");
        misHandler.saveOntology(misFile);

        // save statistics
        File csvGSFile = new File(csvPath, "gs_stats.csv");
        saveGSOutputToCSV(csvGSFile, ontFile.getName(), gsOutput);


        // dispose reasoners
        miner.disposeReasoners();

        Out.p("\nAll is done.\n");
    }




    private static GSOutput compareToGoldStandard(
            Collection<Hypothesis> hypotheses, File ontFile, ReasonerName reasonerName) throws Exception {
        File gsFile = new File(ontFile.getPath().replaceAll(".owl", "_complete.owl"));
        if (!gsFile.exists()) {
            throw new IllegalArgumentException(gsFile + " does not exist!");
        }
        Out.p("\nLoading ontologies");
        // gold standard
        OntologyHandler gsHandler = new OntologyHandler(gsFile);
//		gsHandler.removeAxioms(gsHandler.getDisjointnessAxioms());
        Set<OWLAxiom> gsAxioms = new HashSet<>();
        OWLDataFactory df = gsHandler.getDataFactory();
        for (OWLAxiom ax : gsHandler.getTBoxAxioms()) {
            if (ax instanceof OWLDataPropertyAxiom) {
                continue;
            }
            if (ax instanceof OWLEquivalentClassesAxiom) {
                OWLEquivalentClassesAxiom axiom = (OWLEquivalentClassesAxiom) ax;
                List<OWLClassExpression> exprs = axiom.getClassExpressionsAsList();
                gsAxioms.add(df.getOWLSubClassOfAxiom(exprs.get(0), exprs.get(1)));
                gsAxioms.add(df.getOWLSubClassOfAxiom(exprs.get(1), exprs.get(0)));
            } else {
                gsAxioms.add(ax);
            }
        }
        // hypotheses
        OntologyHandler tboxHandler = new OntologyHandler(ontFile);
//		tboxHandler.removeAxioms(tboxHandler.getDisjointnessAxioms());
        Set<OWLAxiom> tboxAxioms = tboxHandler.getTBoxAxioms();
        Set<OWLAxiom> hypoAxioms = new HashSet<>();
        for (Hypothesis h : hypotheses) {
            for (OWLAxiom ax : h.axioms) {
                if (ax instanceof OWLSubClassOfAxiom) {
                    hypoAxioms.add(ax);
                    // check if disjointness
                    OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
                    if (!axiom.getSubClass().isAnonymous()
                            && axiom.getSuperClass() instanceof OWLObjectComplementOf) {
                        OWLObjectComplementOf rhs = (OWLObjectComplementOf) axiom.getSuperClass();
                        if (!rhs.getOperand().isAnonymous()) {
                            OWLAxiom disjAxiom = df.getOWLDisjointClassesAxiom(
                                    axiom.getSubClass(), rhs.getOperand());
                            hypoAxioms.add(disjAxiom);
                        }
                    }
                }
            }
        }
        Out.p("\nComputing comparison measures");
        GSOutput output = new GSOutput();
        output.compareToGoldStandard(hypoAxioms, gsAxioms, tboxAxioms, reasonerName);
        return output;
    }







    private static void saveGSOutputToCSV(File csvGSFile,
                                          String ontName, GSOutput gsOutput) {
        CSVWriter csvWriter = null;
        if (!csvGSFile.exists()) {
            try {
                // create a new file
                csvGSFile.createNewFile();
                csvWriter = new CSVWriter(csvGSFile, false);
                csvWriter.createGSStatsHeader();
                csvWriter.saveGSStatsToCSV(ontName, gsOutput);
                csvWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // add lines without erasing contents
            csvWriter = new CSVWriter(csvGSFile, true);
            csvWriter.saveGSStatsToCSV(ontName, gsOutput);
            csvWriter.close();
        }
    }


}
