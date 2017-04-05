package io.dlminer.ont;

import java.util.Set;

import org.semanticweb.owlapi.model.*;

public class AxiomMetric {


    public static boolean containsDataRestrictions(Set<OWLAxiom> axioms) {
        return countDataRestrictions(axioms) > 0;
    }

    public static boolean containsDataRestrictions(OWLAxiom axiom) {
        return countDataRestrictions(axiom) > 0;
    }


    public static int countDataRestrictions(Set<OWLAxiom> axioms) {
        int count = 0;
        for (OWLAxiom ax : axioms) {
            count += countDataRestrictions(ax);
        }
        return count;
    }

    public static int countDataRestrictions(OWLAxiom axiom) {
        int count = 0;
        for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
            if (expr instanceof OWLDataSomeValuesFrom) {
                count++;
            }
        }
        return count;
    }


    public static boolean containsConjunctions(Set<OWLAxiom> axioms) {
        return countConjunctions(axioms) > 0;
    }

	
	public static int countConjunctions(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countConjunctions(ax);
		}				
		return count;
	}
	
	public static int countConjunctions(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectIntersectionOf) {
				count++;
			}
		}		
		return count;
	}



    public static boolean containsDisjunctions(Set<OWLAxiom> axioms) {
        return countDisjunctions(axioms) > 0;
    }

    public static boolean containsDisjunctions(OWLAxiom axiom) {
        return countDisjunctions(axiom) > 0;
    }


	public static int countDisjunctions(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countDisjunctions(ax);
		}				
		return count;
	}
	
	
	public static int countDisjunctions(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectUnionOf) {
				count++;
			}
		}		
		return count;
	}



    public static boolean containsNegations(Set<OWLAxiom> axioms) {
        return countNegations(axioms) > 0;
    }

    public static boolean containsNegations(OWLAxiom axiom) {
        return countNegations(axiom) > 0;
    }
	
	public static int countNegations(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countNegations(ax);
		}				
		return count;
	}
	
	
	public static int countNegations(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectComplementOf) {
				count++;
			}
		}		
		return count;
	}
	
	
	public static int countNegations(OWLClassExpression expr) {
		int count = 0;
		for (OWLClassExpression subExpr : expr.getNestedClassExpressions()) {
			if (subExpr instanceof OWLObjectComplementOf) {
				count++;
			}
		}		
		return count;
	}



    public static boolean containsExistentials(Set<OWLAxiom> axioms) {
        return countExistentials(axioms) > 0;
    }

	
	public static int countExistentials(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countExistentials(ax);
		}				
		return count;
	}
	
	
	public static int countExistentials(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectSomeValuesFrom) {
				count++;
			}
		}		
		return count;
	}




    public static boolean containsUniversals(Set<OWLAxiom> axioms) {
        return countUniversals(axioms) > 0;
    }


    public static boolean containsUniversals(OWLAxiom axiom) {
        return countUniversals(axiom) > 0;
    }


	public static int countUniversals(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countUniversals(ax);
		}				
		return count;
	}
	
	
	
	public static int countUniversals(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectAllValuesFrom) {
				count++;
			}
		}		
		return count;
	}



    public static boolean containsMaxRestrictions(Set<OWLAxiom> axioms) {
        return countMaxRestrictions(axioms) > 0;
    }


    public static boolean containsMaxRestrictions(OWLAxiom axiom) {
        return countMaxRestrictions(axiom) > 0;
    }


    public static int countMaxRestrictions(Set<OWLAxiom> axioms) {
        int count = 0;
        for (OWLAxiom ax : axioms) {
            count += countMaxRestrictions(ax);
        }
        return count;
    }



    public static int countMaxRestrictions(OWLAxiom axiom) {
        int count = 0;
        for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
            if (expr instanceof OWLObjectMaxCardinality) {
                count++;
            }
        }
        return count;
    }
	
	
	
	public static boolean isEL(OWLAxiom axiom) {
		if (countDisjunctions(axiom) == 0
				&& countNegations(axiom) == 0
				&& countUniversals(axiom) == 0) {
			return true;
		}
		return false;
	}
	
	
	public static boolean isEL(Set<OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (!isEL(axiom)) {
				return false;
			}
		}
		return true;
	}




}
