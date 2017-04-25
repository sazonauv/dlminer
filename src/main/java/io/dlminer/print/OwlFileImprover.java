package io.dlminer.print;
//package org.ravensoft.dlminer.application;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by zenkevich on 3/28/16.
 */
public class OwlFileImprover {


    public static String eraseDtdHeader(String s) {
        String result = s;

        for (int i = 1; i <= 5; i++) {
            result = result.substring(result.indexOf("\n") + 1);
        }

        return result;
    }

    public static String eraseUrls(String s) {
        Pattern pattern = Pattern.compile("http://([a-zA-z\\./]+)#([a-zA-z\\.]+)");
        Matcher matcher = pattern.matcher(s);

        return matcher.replaceAll("$2");
    }


    public static String eraseOntologyIRI(String s) {
        int ontStart = s.indexOf("Ontology");
        int ontEnd = s.indexOf("\n", ontStart);
        int lastBracketIndex = s.lastIndexOf(")");
        return s.substring(ontEnd+1, lastBracketIndex);
    }


    public static String indentContents(String s) {
        String str = s.trim();
        String tab = "";
        String result = "";
        char[] chars = str.toCharArray();
        for (int i=0; i<chars.length; i++) {
            result += chars[i];
            if (chars[i] == '(') {
                tab += "\t";
                result += "\n" + tab;
            }
            if (chars[i] == ')' && tab.length() > 0) {
                tab = tab.substring(0, tab.length()-1);
            }
            if (chars[i] == ' ') {
                result += "\n" + tab;
            }
        }
        return result;
    }
    
    
    public static String convertToManchesterOWLSyntax(InputStream inputStream) {
        //loading the ontology
        OWLOntology ontology = null;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            ontology = manager.loadOntologyFromOntologyDocument(inputStream);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        if (ontology == null || ontology.getLogicalAxioms() == null) {
            return "";
        }
        String result = "";
        OWLObjectRenderer rendering =
                new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            result += rendering.render(axiom).trim() + "\n";
        }
        return result;
    }

}
