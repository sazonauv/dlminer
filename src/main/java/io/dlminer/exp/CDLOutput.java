package io.dlminer.exp;

import io.dlminer.ont.LengthMetric;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by slava on 11/04/17.
 */
public class CDLOutput {

    public int hypoNumber;
    public int defNumber;
    public int atomDefNumer;
    public int complDefNumer;


    public double hypoTime;
    public double defTime;

    public double defEntailedNumber;
    public double defEntailedRecall;
    public double defEntailedLength;
    public double atomDefEntailedNumber;
    public double complDefEntailedNumber;

    public double defEntailedTBoxNumber;
    public double defEntailedTBoxLength;
    public double atomDefEntailedTBoxNumber;
    public double complDefEntailedTBoxNumber;


    public double subAndSuperClassDefinedNumber;
    public double onlySuperClassDefinedNumber;
    public double onlySubClassDefinedNumber;
    public double classUndefinedNumber;



    public double entHyposNumber;
    public double entHyposLength;


    public Set<OWLAxiom> entailedDefinitions;
    public Set<OWLAxiom> entailingHypotheses;
    public Set<OWLAxiom> entailedDefinitionsTBox;


    private void setDefinedClassNumbers(Set<OWLAxiom> defAxioms, Set<OWLAxiom> defEntAxioms) {
        Set<OWLClass> defCls = getDefinedClasses(defAxioms);
        Set<OWLClass> subClasses = new HashSet<>();
        Set<OWLClass> superClasses = new HashSet<>();
        for (OWLAxiom ax : defEntAxioms) {
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) ax;
                OWLClassExpression sub = axiom.getSubClass();
                if (!sub.isAnonymous()) {
                    subClasses.add(sub.asOWLClass());
                }
                OWLClassExpression sup = axiom.getSuperClass();
                if (!sup.isAnonymous()) {
                    superClasses.add(sup.asOWLClass());
                }
            }
        }
        subAndSuperClassDefinedNumber = 0;
        onlySubClassDefinedNumber = 0;
        onlySuperClassDefinedNumber = 0;
        classUndefinedNumber = 0;
        for (OWLClass cl : defCls) {
            if (subClasses.contains(cl) && superClasses.contains(cl)) {
                subAndSuperClassDefinedNumber++;
            }
            if (!subClasses.contains(cl) && superClasses.contains(cl)) {
                onlySuperClassDefinedNumber++;
            }
            if (subClasses.contains(cl) && !superClasses.contains(cl)) {
                onlySubClassDefinedNumber++;
            }
            if (!subClasses.contains(cl) && !superClasses.contains(cl)) {
                classUndefinedNumber++;
            }
        }
    }



    private static Set<OWLClass> getDefinedClasses(Set<OWLAxiom> axioms) {
        Set<OWLClass> cls = new HashSet<>();
        for (OWLAxiom ax : axioms) {
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom clAxiom = (OWLSubClassOfAxiom) ax;
                OWLClassExpression subCl = clAxiom.getSubClass();
                if (!subCl.isAnonymous()) {
                    cls.add(subCl.asOWLClass());
                }
                OWLClassExpression superCl = clAxiom.getSuperClass();
                if (!superCl.isAnonymous()) {
                    cls.add(superCl.asOWLClass());
                }
            }
        }
        return cls;
    }



    public void compareWithDefinitions(
            Set<OWLAxiom> hypoAxioms, Set<OWLAxiom> defAxioms,
            Set<OWLAxiom> tboxAxioms, ReasonerName reasonerName) {
        hypoNumber = hypoAxioms.size();
        defNumber = defAxioms.size();
        atomDefNumer = countAtomicDefinitions(defAxioms);
        complDefNumer = countComplexDefinitions(defAxioms);
        List<Set<OWLAxiom>> resList = OntologyHandler.getEntailedAxiomsUsingModules(
                hypoAxioms, defAxioms, tboxAxioms, true, reasonerName);
        entailedDefinitionsTBox = resList.get(2);
        defEntailedTBoxNumber = entailedDefinitionsTBox.size();
        defEntailedTBoxLength = LengthMetric.length(entailedDefinitionsTBox);
        atomDefEntailedTBoxNumber = countAtomicDefinitions(entailedDefinitionsTBox);
        complDefEntailedTBoxNumber = countComplexDefinitions(entailedDefinitionsTBox);
        entailedDefinitions = new HashSet<>(resList.get(1));
        entailedDefinitions.removeAll(entailedDefinitionsTBox);
        defEntailedNumber = entailedDefinitions.size();
        defEntailedLength = LengthMetric.length(entailedDefinitions);
        defEntailedRecall = (defNumber-defEntailedTBoxNumber > 0) ? defEntailedNumber / (defNumber-defEntailedTBoxNumber) : 1;
        atomDefEntailedNumber = countAtomicDefinitions(entailedDefinitions);
        complDefEntailedNumber = countComplexDefinitions(entailedDefinitions);
        setDefinedClassNumbers(defAxioms, resList.get(1));
        entailingHypotheses = resList.get(0);
        entHyposNumber = entailingHypotheses.size();
        entHyposLength = LengthMetric.length(entailingHypotheses);
    }



    private static int countComplexDefinitions(Set<OWLAxiom> axioms) {
        int count = 0;
        for (OWLAxiom ax : axioms) {
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom clAxiom = (OWLSubClassOfAxiom) ax;
                OWLClassExpression subCl = clAxiom.getSubClass();
                OWLClassExpression superCl = clAxiom.getSuperClass();
                if (subCl.isAnonymous() || superCl.isAnonymous()) {
                    count++;
                }
            }
        }
        return count;
    }



    private static int countAtomicDefinitions(Set<OWLAxiom> axioms) {
        int count = 0;
        for (OWLAxiom ax : axioms) {
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom clAxiom = (OWLSubClassOfAxiom) ax;
                OWLClassExpression subCl = clAxiom.getSubClass();
                OWLClassExpression superCl = clAxiom.getSuperClass();
                if (!subCl.isAnonymous() && !superCl.isAnonymous()) {
                    count++;
                }
            }
        }
        return count;
    }



    public static void main(String[] args) {
        File file = new File(args[0]);
        OntologyHandler handler = new OntologyHandler(file);
        handler.removeUselessAnnotations();
        File fileMin = new File(file.getParentFile(), file.getName().replaceAll(".owl", "_min.owl"));
        Out.p(fileMin);
        handler.saveOntology(fileMin);
    }

}
