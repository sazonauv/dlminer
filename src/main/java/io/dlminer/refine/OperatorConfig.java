package io.dlminer.refine;

/**
 * Created by slava on 17/03/17.
 */
public class OperatorConfig {


    /**
     * The maximal length of a concept that DL-Miner should inspect,
     * e.g. "hasParent some Person" has concept length = 2 and
     * "hasParent some (Person and hasParent some Person)" has concept length = 5.
     */
    public int maxLength;


    /**
     * The maximal role depth in a concept that DL-Miner should inspect,
     * e.g. "hasParent some Person" has role depth = 1 and
     * "hasParent some (Person and hasParent some Person)" has role depth = 2.
     */
    public int maxDepth;


    /**
     * The minimal support in data that a concept is required to have
     * in order to be inspected by DL-Miner. This is measured in number of
     * individuals and ontology specific. The higher the minimal support is,
     * the faster DL-Miner terminates, but returns fewer hypotheses.
     * It is recommended to be set between 1% and 5% of all individuals for big ontologies,
     * e.g. 100 - 500 for an ontology with 10,000 individuals.
     */
    public int minSupport;


    /**
     * The beam size in the beam search routine. This parameter is important
     * to maintain tractability for ontologies with a lot of data.
     */
    public int beamSize;

    // language bias
    /**
     * The flag indicating whether negation is used for constructing classes
     */
    public boolean useNegation;

    /**
     * The flag indicating whether universal restriction is used for constructing classes
     */
    public boolean useUniversalRestriction;

    /**
     * The flag indicating whether disjunction is used for constructing classes
     */
    public boolean useDisjunction;

    /**
     * The flag indicating whether data properties are used to generate concept expressions
     */
    public boolean useDataProperties;

    // optimisations
    /**
     * The flag indicating whether disjoint classes are checked
     */
    public boolean checkDisjointness;
    public boolean useReasonerForAtomicClassInstances;

    public boolean checkClassHierarchy;
    public boolean checkSyntacticRedundancy;



    /**
     * The number of thresholds for expressions with data properties
     */
    public int dataThresholdsNumber;


    public boolean useReasonerForClassInstances;



}
