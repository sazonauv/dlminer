package io.dlminer.exp;

import io.dlminer.ont.OntologyHandler;
import io.dlminer.print.Out;

import java.io.File;

/**
 * Created by slava on 03/07/17.
 */
public class AnnotationCleaner {

    public static void main(String[] args) {
        File inFile = new File(args[0]);
        File outFile = new File(args[1]);
        OntologyHandler handler = new OntologyHandler(inFile);
        handler.removeUselessAnnotations();
        handler.saveOntology(outFile);
        Out.p("Done");
    }

}
