package io.dlminer.learn;

import io.dlminer.main.DLMinerMode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

/**
 * Created by slava on 07/09/17.
 */
public class AxiomConfig {

    /**
     * The minimal precision that a hypothesis must have,
     * e.g. 0.8 means that a hypothesis must be at least 80% accurate.
     * It weighs the support of a hypothesis against its
     * assumption. The higher the minimal precision is,
     * the more credible hypotheses DL-Miner tend to return,
     * but fewer of them. Some recommended values are 0.8, 0.9.
     */
    public Double minPrecision;


    /**
     * The positive class for predictions
     */
    public OWLClass positiveClass;


    /**
     * The negative class for predictions
     */
    public OWLClass negativeClass;


    /**
     * The mode the algorithm operates in
     */
    public DLMinerMode dlminerMode;



    /**
     * The set of seed entities for learning
     */
    public Set<OWLEntity> seedEntities;


    public String seedClassName;


    public String[] ignoredStrings;

    public AxiomPattern axiomPattern;



    /**
     * The flag indicating whether the minimal support
     * is used to filter hypotheses (normally is set to TRUE)
     */
    public Boolean useMinSupport;



    /**
     * The flag indicating whether the minimal precision is used to filter hypotheses
     */
    public Boolean useMinPrecision;



    /**
     * The flag indicating whether consistency is used to filter hypotheses
     */
    public Boolean useConsistency;



    /**
     * The flag indicating whether hypotheses are cleaned
     */
    public Boolean useCleaning;



}
