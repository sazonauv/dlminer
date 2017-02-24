package io.dlminer.learn;

import io.dlminer.ont.OntologyHandler;
import io.dlminer.sort.AxiomLengthComparator;
import io.dlminer.sort.SortingOrder;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;

/**
 * Created by Slava Sazonau on 14/03/16.
 */
public abstract class ABoxCleaner {


    public static Set<OWLAxiom> findMinimalSubset(Set<OWLAxiom> axioms, OntologyHandler handler, OWLReasoner reasoner) {
        // retain only non-entailed axioms
        List<OWLAxiom> axiomList = new ArrayList<>(axioms.size());
        for (OWLAxiom ax : axioms) {
            if (!reasoner.isEntailed(ax)) {
                axiomList.add(ax);
            }
        }
        // sort axioms by length descending
        OWLAxiom[] array = new OWLAxiom[axiomList.size()];
        array = axiomList.toArray(array);
        Arrays.sort(array, new AxiomLengthComparator(SortingOrder.DESC));
        axiomList = Arrays.asList(array);
        handler.addAxioms(axiomList);
        Set<OWLAxiom> minSet = findMinimalSubset(axiomList, handler, reasoner);
        handler.removeAxioms(axiomList);
        return minSet;
    }


    private static Set<OWLAxiom> findMinimalSubset(Collection<OWLAxiom> axioms, OntologyHandler handler, OWLReasoner reasoner) {
        // find a minimal subset (preserve the order)
        Set<OWLAxiom> minSet = new LinkedHashSet<>();
        for (OWLAxiom ax : axioms) {
            handler.removeAxiom(ax);
            reasoner.flush();
            reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
            if (!reasoner.isEntailed(ax)) {
                minSet.add(ax);
                handler.addAxiom(ax);
            }
        }
        return minSet;
    }

    public static Set<OWLAxiom> removeSuperclasses(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                   OWLReasoner reasoner) {
        Set<OWLAxiom> minSet = new HashSet<>();
        for (OWLNamedIndividual ind : indAxiomMap.keySet()) {
            List<OWLAxiom> type = indAxiomMap.get(ind);
            for (int i=0; i<type.size(); i++) {
                OWLAxiom ax1 = type.get(i);
                OWLClass cl1 = OntologyHandler.getAssertionClass(ax1);
                if (!hasSubclass(i, cl1, type, reasoner)) {
                    minSet.add(ax1);
                }
            }
        }
        return minSet;
    }


    private static boolean hasSubclass(int i, OWLClass cl1,
                                       List<OWLAxiom> type,
                                       OWLReasoner reasoner) {
        boolean hasSubclass = false;
        for (int j=type.size()-1; j>=0; j--) {
            OWLAxiom ax2 = type.get(j);
            OWLClass cl2 = OntologyHandler.getAssertionClass(ax2);
            if (!cl1.equals(cl2) && isSubclassOf(cl2, cl1, reasoner)) {
                // check if cl1 = cl2
                if (!isSubclassOf(cl1, cl2, reasoner)) {
                    hasSubclass = true;
                } else {
                    // check if cl1 is the last one
                    if (i < j) {
                        hasSubclass = true;
                    }
                }
                break;
            }
        }
        return hasSubclass;
    }

    
    

    public static Set<OWLAxiom> findClassShrinkage(
            Map<OWLNamedIndividual, Map<OWLClass, OWLAxiom>> indAxiomMap,
            Map<OWLClass, Set<OWLNamedIndividual>> classIndMap,
            OWLReasoner tboxReasoner) {
        Set<OWLAxiom> minSet = new HashSet<>();
        Set<OWLClass> cls1 = classIndMap.keySet();
        // process equivalent classes
        Set<OWLClass> cls2 = removeEquivalentClasses(tboxReasoner, cls1);
        // add assertions of the removed equivalent classes
        for (OWLClass cl : cls1) {
            if (!cls2.contains(cl)) {
                Set<OWLNamedIndividual> insts = classIndMap.get(cl);
                for (OWLNamedIndividual inst : insts) {
                    minSet.add(indAxiomMap.get(inst).get(cl));
                }
            }
        }
        // add assertions of classes that have subclasses
        for (OWLClass cl : cls2) {
            Set<OWLNamedIndividual> clInsts = classIndMap.get(cl);
            Set<OWLClass> subs = tboxReasoner.getSubClasses(cl, false).getFlattened();
            for (OWLClass sub : subs) {
                if (!sub.isOWLNothing() && cls2.contains(sub)) {
                    Set<OWLNamedIndividual> subInsts = classIndMap.get(sub);
                    for (OWLNamedIndividual inst : clInsts) {
                        if (subInsts.contains(inst)) {
                            minSet.add(indAxiomMap.get(inst).get(cl));
                        }
                    }
                }
            }
        }
        return minSet;
    }


    public static Set<OWLClass> removeEquivalentClasses(OWLReasoner reasoner, Set<OWLClass> classes) {
        Set<OWLClass> cls = new HashSet<>();
        Set<OWLClass> eqs = new HashSet<>();
        for (OWLClass cl : classes) {
            Set<OWLClass> clEqs = reasoner.getEquivalentClasses(cl).getEntities();
            if (clEqs.size() > 1) {
                // check if already included
                boolean included = false;
                for (OWLClass eq : clEqs) {
                    if (eqs.contains(eq)) {
                        included = true;
                        break;
                    }
                }
                if (!included) {
                    eqs.add(cl);
                }
            } else {
                cls.add(cl);
            }
        }
        cls.addAll(eqs);
        return cls;
    }
    
    

    public static Set<OWLAxiom> findRoleShrinkage(
            Map<List<OWLNamedIndividual>, Map<OWLObjectProperty, OWLAxiom>> indAxiomMapRoleProj,
            Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleIndMapProj,
            OWLReasoner tboxReasoner) {
        Set<OWLAxiom> minSet = new HashSet<>();
        Set<OWLObjectProperty> roles1 = roleIndMapProj.keySet();
        // process equivalent roles
        Set<OWLObjectProperty> roles2 = removeEquivalentRoles(tboxReasoner, roles1);
        // add assertions of the removed equivalent roles
        for (OWLObjectProperty r : roles1) {
            if (!roles2.contains(r)) {
                Set<List<OWLNamedIndividual>> insts = roleIndMapProj.get(r);
                for (List<OWLNamedIndividual> inst : insts) {
                    minSet.add(indAxiomMapRoleProj.get(inst).get(r));
                }
            }
        }
        // add assertions of roles that have subroles
        for (OWLObjectProperty r : roles2) {
            Set<List<OWLNamedIndividual>> rInsts = roleIndMapProj.get(r);
            Set<OWLObjectPropertyExpression> subs =
                    tboxReasoner.getSubObjectProperties(r, false).getFlattened();
            for (OWLObjectPropertyExpression sub : subs) {
                if (!sub.isOWLBottomObjectProperty() && roles2.contains(sub)) {
                    Set<List<OWLNamedIndividual>> subInsts = roleIndMapProj.get(sub);
                    for (List<OWLNamedIndividual> inst : rInsts) {
                        if (subInsts.contains(inst)) {
                            minSet.add(indAxiomMapRoleProj.get(inst).get(r));
                        }
                    }
                }
            }
        }
        return minSet;
    }


    public static Set<OWLObjectProperty> removeEquivalentRoles(
            OWLReasoner reasoner, Set<OWLObjectProperty> roles) {
        Set<OWLObjectProperty> rs = new HashSet<>();
        Set<OWLObjectProperty> eqs = new HashSet<>();
        for (OWLObjectProperty r : roles) {
            Set<OWLObjectPropertyExpression> rEqs =
                    reasoner.getEquivalentObjectProperties(r).getEntities();
            if (rEqs.size() > 1) {
                // check if already included
                boolean included = false;
                for (OWLObjectPropertyExpression eq : rEqs) {
                    if (eqs.contains(eq)) {
                        included = true;
                        break;
                    }
                }
                if (!included) {
                    eqs.add(r);
                }
            } else {
                rs.add(r);
            }
        }
        rs.addAll(eqs);
        return rs;
    }


    public static Set<OWLAxiom> findShrinkageApprox(
            Map<OWLNamedIndividual, Map<OWLClass, OWLAxiom>> indAxiomMap,
            Map<OWLClass, Set<OWLNamedIndividual>> classIndMap,
            OWLReasoner tboxReasoner, OWLReasoner hypoReasoner) {
        Set<OWLAxiom> minSet = new HashSet<>();
        Set<OWLClass> cls1 = classIndMap.keySet();
        // process equivalent classes
        Set<OWLClass> cls2 = removeEquivalentClassesApprox(tboxReasoner, hypoReasoner, cls1);
        // add assertions of the removed equivalent classes
        for (OWLClass cl : cls1) {
            if (!cls2.contains(cl)) {
                Set<OWLNamedIndividual> insts = classIndMap.get(cl);
                for (OWLNamedIndividual inst : insts) {
                    minSet.add(indAxiomMap.get(inst).get(cl));
                }
            }
        }
        // count the ones with direct subclasses
        for (OWLClass cl : cls2) {
            Set<OWLNamedIndividual> clInsts = classIndMap.get(cl);
            Set<OWLClass> subs1 = tboxReasoner.getSubClasses(cl, true).getFlattened();
            Set<OWLClass> subs2 = hypoReasoner.getSubClasses(cl, true).getFlattened();
            // check tbox
            for (OWLClass sub : subs1) {
                if (!sub.isOWLNothing() && cls2.contains(sub)) {
                    Set<OWLNamedIndividual> subInsts = classIndMap.get(sub);
                    for (OWLNamedIndividual inst : clInsts) {
                        if (subInsts.contains(inst)) {
                            minSet.add(indAxiomMap.get(inst).get(cl));
                        }
                    }
                }
            }
            // check hypothesis
            for (OWLClass sub : subs2) {
                if (!sub.isOWLNothing() && cls2.contains(sub)) {
                    Set<OWLNamedIndividual> subInsts = classIndMap.get(sub);
                    for (OWLNamedIndividual inst : clInsts) {
                        if (subInsts.contains(inst)) {
                            minSet.add(indAxiomMap.get(inst).get(cl));
                        }
                    }
                }
            }
        }
        return minSet;
    }


    private static Set<OWLClass> removeEquivalentClassesApprox(
            OWLReasoner tboxReasoner, OWLReasoner hypoReasoner,
            Set<OWLClass> classes) {
        Set<OWLClass> cls = new HashSet<>();
        Set<OWLClass> eqs = new HashSet<>();
        for (OWLClass cl : classes) {
            Set<OWLClass> clEqs1 = tboxReasoner.getEquivalentClasses(cl).getEntities();
            Set<OWLClass> clEqs2 = hypoReasoner.getEquivalentClasses(cl).getEntities();
            // check tbox
            if (clEqs1.size() > 1) {
                // check if already included
                boolean included = false;
                for (OWLClass eq : clEqs1) {
                    if (eqs.contains(eq)) {
                        included = true;
                        break;
                    }
                }
                if (!included) {
                    eqs.add(cl);
                }
            } else
                // check hypothesis
                if (clEqs2.size() > 1) {
                    // check if already included
                    boolean included = false;
                    for (OWLClass eq : clEqs2) {
                        if (eqs.contains(eq)) {
                            included = true;
                            break;
                        }
                    }
                    if (!included) {
                        eqs.add(cl);
                    }
                } else {
                    cls.add(cl);
                }
        }
        cls.addAll(eqs);
        return cls;
    }



    public static Set<OWLAxiom> findMinimalABox(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                OWLReasoner reasoner) {
        // sort axioms by length descending
        Set<OWLAxiom> minSet = removeSuperclasses(indAxiomMap, reasoner);
        return minSet;
    }


    private static boolean isSubclassOf(OWLClass cl1, OWLClass cl2,
                                        OWLReasoner reasoner) {
        Set<OWLClass> subs2 = reasoner.getSubClasses(cl2, false).getFlattened();
        return subs2.contains(cl1);
    }


    public static Set<OWLAxiom> removeSuperclassesLower(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                        OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLAxiom> minSet = new HashSet<>();
        for (OWLNamedIndividual ind : indAxiomMap.keySet()) {
            List<OWLAxiom> type = indAxiomMap.get(ind);
            for (int i=0; i<type.size(); i++) {
                OWLAxiom ax1 = type.get(i);
                OWLClass cl1 = OntologyHandler.getAssertionClass(ax1);
                if (!hasSubclassLower(i, cl1, type, reasoner1, reasoner2)) {
                    minSet.add(ax1);
                }
            }
        }
        return minSet;
    }


    private static boolean hasSubclassLower(int i, OWLClass cl1,
                                            List<OWLAxiom> type, OWLReasoner reasoner1, OWLReasoner reasoner2) {
        boolean hasSubclass = false;
        for (int j=type.size()-1; j>=0; j--) {
            OWLAxiom ax2 = type.get(j);
            OWLClass cl2 = OntologyHandler.getAssertionClass(ax2);
            if (!cl1.equals(cl2) && isSubclassOfLower(cl2, cl1, reasoner1, reasoner2)) {
                // check if cl1 = cl2
                if (!isSubclassOfLower(cl1, cl2, reasoner1, reasoner2)) {
                    hasSubclass = true;
                } else {
                    // check if cl1 is the last one
                    if (i < j) {
                        hasSubclass = true;
                    }
                }
                break;
            }
        }
        return hasSubclass;
    }

    public static Set<OWLAxiom> findMinimalABoxLower(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                     OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLAxiom> minSet = removeSuperclassesLower(indAxiomMap, reasoner1, reasoner2);
        return minSet;
    }


    public static Set<OWLAxiom> removeSuperclassesUpper(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                        OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLAxiom> minSet = new HashSet<>();
        for (OWLNamedIndividual ind : indAxiomMap.keySet()) {
            List<OWLAxiom> type = indAxiomMap.get(ind);
            for (int i=0; i<type.size(); i++) {
                OWLAxiom ax1 = type.get(i);
                OWLClass cl1 = OntologyHandler.getAssertionClass(ax1);
                if (!hasSubclassUpper(i, cl1, type, reasoner1, reasoner2)) {
                    minSet.add(ax1);
                }
            }
        }
        return minSet;
    }


    private static boolean hasSubclassUpper(int i, OWLClass cl1,
                                            List<OWLAxiom> type, OWLReasoner reasoner1, OWLReasoner reasoner2) {
        boolean hasSubclass = false;
        for (int j=type.size()-1; j>=0; j--) {
            OWLAxiom ax2 = type.get(j);
            OWLClass cl2 = OntologyHandler.getAssertionClass(ax2);
            if (!cl1.equals(cl2) && isSubclassOfUpper(cl2, cl1, reasoner1, reasoner2)) {
                // check if cl1 = cl2
                if (!isSubclassOfUpper(cl1, cl2, reasoner1, reasoner2)) {
                    hasSubclass = true;
                } else {
                    // check if cl1 is the last one
                    if (i < j) {
                        hasSubclass = true;
                    }
                }
                break;
            }
        }
        return hasSubclass;
    }

    public static Set<OWLAxiom> findMinimalABoxUpper(Map<OWLNamedIndividual, List<OWLAxiom>> indAxiomMap,
                                                     OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLAxiom> minSet = removeSuperclassesUpper(indAxiomMap, reasoner1, reasoner2);
        return minSet;
    }


    private static boolean isSubclassOfUpper(OWLClass cl1, OWLClass cl2,
                                             OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLClass> subs1 = reasoner1.getSubClasses(cl2, false).getFlattened();
        Set<OWLClass> subs2 = reasoner2.getSubClasses(cl2, false).getFlattened();
        if (subs1.contains(cl1) || subs2.contains(cl1)) {
            return true;
        }
        for (OWLClass sub1 : subs1) {
            Set<OWLClass> subs12 = reasoner2.getSubClasses(sub1, false).getFlattened();
            if (subs12.size() > 1) {
                for (OWLClass sub12 : subs12) {
                    Set<OWLClass> subs121 = reasoner1.getSubClasses(sub12, false).getFlattened();
                    if (subs121.size() > 1) {
                        return true;
                    }
                }
            }
        }
        for (OWLClass sub2 : subs2) {
            Set<OWLClass> subs21 = reasoner1.getSubClasses(sub2, false).getFlattened();
            if (subs21.size() > 1) {
                for (OWLClass sub21 : subs21) {
                    Set<OWLClass> subs212 = reasoner2.getSubClasses(sub21, false).getFlattened();
                    if (subs212.size() > 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSubclassOfLower(OWLClass cl1, OWLClass cl2,
                                             OWLReasoner reasoner1, OWLReasoner reasoner2) {
        Set<OWLClass> subs1 = reasoner1.getSubClasses(cl2, false).getFlattened();
        Set<OWLClass> subs2 = reasoner2.getSubClasses(cl2, false).getFlattened();
        if (subs1.contains(cl1) || subs2.contains(cl1)) {
            return true;
        }
        for (OWLClass sub1 : subs1) {
            Set<OWLClass> subs12 = reasoner2.getSubClasses(sub1, false).getFlattened();
            if (subs12.contains(cl1)) {
                return true;
            } else {
                for (OWLClass sub12 : subs12) {
                    Set<OWLClass> subs121 = reasoner1.getSubClasses(sub12, false).getFlattened();
                    if (subs121.contains(cl1)) {
                        return true;
                    }
                }
            }
        }
        for (OWLClass sub2 : subs2) {
            Set<OWLClass> subs21 = reasoner1.getSubClasses(sub2, false).getFlattened();
            if (subs21.contains(cl1)) {
                return true;
            } else {
                for (OWLClass sub21 : subs21) {
                    Set<OWLClass> subs212 = reasoner2.getSubClasses(sub21, false).getFlattened();
                    if (subs212.contains(cl1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


	



}
