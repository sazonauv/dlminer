package io.dlminer.exp;

import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEvaluator;
import io.dlminer.main.*;
import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.HypothesisWriter;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by slava on 11/04/17.
 */
public class CDLExperiment {

    public static void main(String[] args) throws Exception {

        // check parameters number
        if (args.length != 18) {
            throw new RuntimeException(
                    "You need exactly 18 parameters: ontology path, hypotheses path, csv path, " +
                            "\nmax role depth, max hypotheses number, reasoner timeout, " +
                            "\nmin precision, min support, beam size, max concept length, " +
                            "\nlogic, reasoner name, " +
                            "\nevaluation sample size, max definitions number," +
                            "\ndisjunction flag, precision flag, cleaning flag, disjointness flag.");
        }


        // process parameters
        File ontFile = new File(args[0]);
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
        Integer sampleSize = Integer.parseInt(args[12]);
        Integer defNumber = Integer.parseInt(args[13]);
        Integer disjunctionFlag = Integer.parseInt(args[14]);
        boolean useDisjunction = (disjunctionFlag == 0) ? false : true;
        Integer precFlag = Integer.parseInt(args[15]);
        boolean usePrecision = (precFlag == 0) ? false : true;
        Integer cleanFlag = Integer.parseInt(args[16]);
        boolean useCleaning = (cleanFlag == 0) ? false : true;
        Integer disjointFlag = Integer.parseInt(args[17]);
        boolean useDisjointness = (disjointFlag == 0) ? false : true;

        // set logging
//        Out.setLog(hypothesesPath);



        // set parameters
        DLMinerInput input = new DLMinerInput(ontFile);
        input.setLogic(logic);
        input.setReasonerName(reasonerName);
        input.setMaxHypothesesNumber(hypothesesNumber);
        input.setReasonerTimeout(reasonerTimeout);
        input.setMinPrecision(minPrecision);
        input.setUseMinPrecision(usePrecision);
        input.setUseCleaning(useCleaning);
        input.setDlminerMode(DLMinerMode.CDL);

        // language bias
        OperatorConfig config = input.getConfig();
        config.maxDepth = roleDepth;
        config.maxLength = maxConceptLength;
        config.minSupport = minSupport;
        config.useDataProperties = false;
        config.useNegation = true;
        config.useDisjunction = true;
        config.useUniversalRestriction = true;
        if (logic.equals(Logic.EL)) {
            config.useNegation = false;
            config.useDisjunction = false;
            config.useUniversalRestriction = false;
        }

        // optimisations
        config.checkDisjointness = true;
        config.checkClassHierarchy = true;
        config.checkPropertyHierarchy = true;
        config.checkPropertyDomainsAndRanges = true;
        config.checkClassInstances = true;



        // run DL-Miner
        DLMiner miner = new DLMiner(input);
        try {
            miner.run();
        } catch (Throwable e) {
            e.printStackTrace();
            Out.p("\nThe reasoner " + reasonerName + " has failed to process the ontology");
            if (!reasonerName.equals(ReasonerLoader.DEF_REASONER)) {
                Out.p("\tRerun with the default reasoner " + ReasonerLoader.DEF_REASONER);
                input.setOntologyFile(new File(args[0]));
                input.setReasonerName(ReasonerLoader.DEF_REASONER);
                miner.run();
            }
        }

        Out.p("\nStatistics:");
        DLMinerStats stats = miner.getStats();
        DLMinerOutput output = miner.getOutput();
        Out.p(stats);

        Collection<Hypothesis> hypotheses = output.getHypotheses();

        // sample random hypotheses
//        Collection<Hypothesis> sampleHypotheses = HypothesisEvaluator.sampleHypotheses(hypotheses, sampleSize);
        // evaluate hypotheses
//        HypothesisEvaluator evaluator = output.getEvaluator();
//		evaluator.evaluateHypothesesContrapositive(sampleHypotheses, stats);
//		evaluator.evaluateHypotheses(sampleHypotheses, stats);
        // measure strength
//        miner.evaluateStrength(sampleHypotheses);
        // rank by dominance
//        double start = System.currentTimeMillis();
//        HypothesisEvaluator.rank(hypotheses);
//        double time = (System.currentTimeMillis() - start)/1e3;
//        stats.setDominanceTime(time);


        double hypoTime = stats.getOntologyParsingTime()
                + stats.getOntologyReasoningTime()
                + stats.getConceptBuildingTime()
                + stats.getHypothesesBuildingTime();



        // run DL-Learner
        input.setOntologyFile(ontFile);
        double start = System.currentTimeMillis();
        Set<OWLAxiom> defAxioms = learnDefinitions(defNumber, input, output);
        double defTime = (System.currentTimeMillis() - start)/1e3;
        Out.p("\nDefinitions are built: " + defAxioms.size());

        // add axioms from hypotheses
        Set<OWLAxiom> hypoAxioms = new HashSet<>();
        for (Hypothesis h : hypotheses) {
            for (OWLAxiom ax : h.axioms) {
                // only class axioms
                if (ax instanceof OWLSubClassOfAxiom) {
                    hypoAxioms.add(ax);
                }
            }
        }

        Out.p("\nEvaluating descriptions");
        // evaluate axioms
        Set<Hypothesis> defHypos = miner.evaluateAxioms(defAxioms);
        // filter axioms
        Set<Hypothesis> filtDefHypos = new HashSet<>();
        for (Hypothesis h : defHypos) {
            if (h.support >= input.getConfig().minSupport) {
                if (input.isUseMinPrecision() && h.precision < input.getMinPrecision()) {
                    continue;
                }
                filtDefHypos.add(h);
            }
        }
        defHypos = filtDefHypos;
        defAxioms = new HashSet<>();
        for (Hypothesis h : defHypos) {
            defAxioms.addAll(h.axioms);
        }
        // evaluate strength
        miner.evaluateStrength(defHypos);
        start = System.currentTimeMillis();
        if (!defHypos.isEmpty()) {
            HypothesisEvaluator.rank(defHypos);
        }
        double time = (System.currentTimeMillis() - start)/1e3;
        stats.setDominanceTime(time);


        // compare axioms
        Out.p("\nCompare axioms");
        CDLOutput cdlOutput = new CDLOutput();
        // add TBox
        OntologyHandler handler = new OntologyHandler(output.getOntology());
        Set<OWLAxiom> tboxAxioms = new HashSet<>(handler.getTBoxAxioms());
        tboxAxioms.addAll(handler.getRBoxAxioms());
        cdlOutput.compareWithDefinitions(hypoAxioms, defAxioms, tboxAxioms, reasonerName);
        cdlOutput.hypoTime = hypoTime;
        cdlOutput.defTime = defTime;


        // which definitions are entailed due to hypotheses with TBox
        // but not by TBox alone
        Set<Hypothesis> entDefHypos = new HashSet<>();
        Set<Hypothesis> misDefHypos = new HashSet<>();
        for (Hypothesis h : defHypos) {
            for (OWLAxiom ax : h.axioms) {
                if (cdlOutput.entailedDefinitionsTBox.contains(ax)) {
                    continue;
                }
                if (cdlOutput.entailedDefinitions.contains(ax)) {
                    entDefHypos.add(h);
                } else {
                    misDefHypos.add(h);
                }
            }
        }
        // debug
        Out.p("\nMissed definitions:");
        Out.printHypothesesMS(misDefHypos);


        // which hypotheses entail definition
        Set<Hypothesis> entHypos = new HashSet<>();
        for (Hypothesis h : hypotheses) {
            for (OWLAxiom ax : h.axioms) {
                if (cdlOutput.entailingHypotheses.contains(ax)) {
                    entHypos.add(h);
                }
            }
        }

        // save results
        Out.p("\nSaving hypotheses");
        // set numeric ids
        HypothesisWriter.setNumericIDs(hypotheses);
        DLMiner.addQualityValues(hypotheses);
        IRI iri = output.getOntology().getOntologyID().getOntologyIRI();
        if (!owlPath.exists()) {
            owlPath.mkdirs();
        }

        // save all hypotheses
        if (!csvPath.exists()) {
            csvPath.mkdirs();
        }
        File csvFile = new File(csvPath, "hypotheses.csv");
        CSVWriter.saveHypothesesToCSV(hypotheses, csvFile, ontFile.getName());

        // save statistics
        File csvStatsFile = new File(csvPath, "run_stats.csv");
        CSVWriter.saveStatsToCSV(csvStatsFile, ontFile.getName(),
                stats, output.getOntology());


        // save hypotheses
        String name = ontFile.getName().replaceAll(".owl", "");
        File owlFile = new File(owlPath, name + "_h.owl");
        miner.saveHypotheses(hypotheses, owlFile, iri);


        File csvEntFile = new File(csvPath, "hypotheses_ent.csv");
        CSVWriter.saveHypothesesToCSV(entHypos, csvEntFile, ontFile.getName());
        File owlEntFile = new File(owlPath, name + "_h_ent.owl");
        miner.saveHypotheses(entHypos, owlEntFile, iri);



        // save statistics
        File csvCDLFile = new File(csvPath, "cdl_stats.csv");
        saveCDLOutputToCSV(csvCDLFile, ontFile.getName(), cdlOutput);


        // save definitions
        HypothesisWriter.setNumericIDs(defHypos);
        DLMiner.addQualityValues(defHypos);
        File owlDefFile = new File(owlPath, name + "_def.owl");
        miner.saveHypotheses(defHypos, owlDefFile, iri);



        // save entailed definitions
        File owlEntDefFile = new File(owlPath, name + "_def_ent.owl");
        miner.saveHypotheses(entDefHypos, owlEntDefFile, iri);

        // save missed definitions
        File owlMisDefFile = new File(owlPath, name + "_def_mis.owl");
        miner.saveHypotheses(misDefHypos, owlMisDefFile, iri);

        // save qualities of definitions
        File csvDefHyposFile = new File(csvPath, "hypotheses_def.csv");
        CSVWriter.saveHypothesesToCSV(defHypos, csvDefHyposFile, ontFile.getName());
        File csvEntDefHyposFile = new File(csvPath, "hypotheses_def_ent.csv");
        CSVWriter.saveHypothesesToCSV(entDefHypos, csvEntDefHyposFile, ontFile.getName());
        File csvMisDefHyposFile = new File(csvPath, "hypotheses_def_mis.csv");
        CSVWriter.saveHypothesesToCSV(misDefHypos, csvMisDefHyposFile, ontFile.getName());


        // dispose reasoners
        miner.disposeReasoners();

        Out.p("\nAll is done.\n");
    }













    private static Set<OWLAxiom> learnDefinitions(int defsPerClass,
                                                  DLMinerInput input, DLMinerOutput output) throws Exception {
        // load the ontology
        OntologyHandler handler = new OntologyHandler(output.getOntology());
        OWLReasoner reasoner = ReasonerLoader.initReasoner(
                input.getReasonerName(),
                handler.getOntology(),
                input.getReasonerTimeout());
        // check if the ontology is consistent
        if (!reasoner.isConsistent()) {
            Out.p("\nThe ontology is inconsistent!");
            handler.removeInconsistency(reasoner);
            reasoner.flush();
        }
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
                InferenceType.OBJECT_PROPERTY_HIERARCHY);
        OWLDataFactory df = handler.getDataFactory();
        // learn concepts
        Out.p("\nLearning concept descriptions");
        OWLAPIReasoner reasoningService = new OWLAPIReasoner(reasoner);
        reasoningService.init();
        Set<OWLClass> cls = new HashSet<>(handler.getClassesInSignature());
        cls.remove(handler.getDataFactory().getOWLThing());
        Set<OWLAxiom> definitions = new HashSet<>();
        int count = 0;
        for (OWLClass cl : cls) {
            try {
                ClassLearningProblem learningProblem = new ClassLearningProblem(reasoningService);
                learningProblem.setClassToDescribe(cl);
                learningProblem.init();
                CELOE celoe = new CELOE(learningProblem, reasoningService);
                celoe.init();
                celoe.start();
                List<OWLClassExpression> descriptions = celoe.getCurrentlyBestDescriptions(defsPerClass);
//				List<OWLClassExpression> descriptions = new ArrayList<>();
//				List<? extends EvaluatedDescription<? extends Score>> descrScores =
//						celoe.getCurrentlyBestEvaluatedDescriptions(defsPerClass);
//				for (EvaluatedDescription<? extends Score> descrScore : descrScores) {
//					OWLClassExpression descr = descrScore.getDescription();
//					if (!descr.equals(cl) && descrScore.getAccuracy() >= input.getMinPrecision()) {
//						descriptions.add(descr);
//					}
//				}
                for (OWLClassExpression descr : descriptions) {
                    OWLAxiom subClassAxiom = df.getOWLSubClassOfAxiom(cl, descr);
                    OWLAxiom superClassAxiom = df.getOWLSubClassOfAxiom(descr, cl);
                    definitions.add(superClassAxiom);
                    definitions.add(subClassAxiom);
                }
                Out.p(cl + ": " + (++count) + " / " + cls.size() + " is defined (" + descriptions.size() + " definitions)");
            } catch (Exception e) {
                Out.p(e + DLMinerOutputI.CONCEPT_BUILDING_ERROR);
            }
        }
        reasoner.dispose();
        return definitions;
    }




    private static void saveCDLOutputToCSV(File csvCDLFile,
                                           String ontName, CDLOutput cdlOutput) {
        CSVWriter csvWriter = null;
        if (!csvCDLFile.exists()) {
            try {
                // create a new file
                csvCDLFile.createNewFile();
                csvWriter = new CSVWriter(csvCDLFile, false);
                csvWriter.createCDLStatsHeader();
                csvWriter.saveCDLStatsToCSV(ontName, cdlOutput);
                csvWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // add lines without erasing contents
            csvWriter = new CSVWriter(csvCDLFile, true);
            csvWriter.saveCDLStatsToCSV(ontName, cdlOutput);
            csvWriter.close();
        }
    }


	/*private static Set<OWLAxiom> learnRules(
			DLMinerInput input) throws Exception {
		Set<OWLAxiom> rules = new HashSet<>();
		GoldMiner miner = new GoldMiner();
		miner.createTransactionTables();
		miner.mineAssociationRules();
		HashMap<OWLAxiom, SupportConfidenceTuple> ruleQualityMap = miner.parseAssociationRules();
		for (OWLAxiom rule : ruleQualityMap.keySet()) {
			SupportConfidenceTuple quality = ruleQualityMap.get(rule);
			if (quality.getSupport() >= input.getMinConceptSupport()
					&& quality.getConfidence() >= input.getMinPrecision()) {
				rules.add(rule);
			}
		}
		return rules;
	}*/

}
