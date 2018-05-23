package imi.coreNLPExample;

//import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;

public class Main_run_corenlp {
	/*public static String text = "Joe Smith was born in California. " +
		      "In 2017, he went to Paris, France in the summer. " +
		      "His flight left at 3:00pm on July 10th, 2017. " +
		      "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
		      "He sent a postcard to his sister Jane Smith. " +
		      "After hearing about Joe's trip, Jane decided she might go to France one day.";
	*/
	public static String fileIn = "D:/projects/UMLS2SCT_PostCoordination/disjointUMLS_SCT.txt";
	public static String fileOut = "D:/projects/UMLS2SCT_PostCoordination/coreNLP_disjointUMLS_SCT.txt";
	private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
	private static boolean isPureAscii(String v) {
		    return asciiEncoder.canEncode(v);
	}

	public static void main(String[] args) {
		
		// set up pipeline properties
	    Properties props = new Properties();
	    // set the list of annotators to run
	    //props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse");
	    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
	    props.setProperty("coref.algorithm", "neural");
	    // build pipeline
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    String text = "";
	    Map<String,Set<String>> mapCUIs = new HashMap<String,Set<String>>();
    	try {
	    	BufferedReader br = new BufferedReader(new FileReader(fileIn));
	    	String line = "";
	    	while((line=br.readLine())!=null) {
	    		if(!line.contains("\t")) {
	    			continue;
	    		}
	    		String cui = line.substring(0, line.indexOf("\t"));
	    		String label = line.substring(line.indexOf("\t")+1);
	    		if(!isPureAscii(label)) continue;
	    		if(mapCUIs.containsKey(cui)) {
	    			mapCUIs.get(cui).add(label);
	    		}else {
	    			Set<String> listLabels = new HashSet<String>();
	    			listLabels.add(label);
	    			mapCUIs.put(cui,listLabels);
	    		}
	    	}
	    	br.close();
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
    	try {
    		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
    		for(String cui: mapCUIs.keySet()) {
    			
    			for(String label: mapCUIs.get(cui)) {
    				text=label;
    				if(label.length()>100) continue;
    				// create a document object
    				CoreDocument document = new CoreDocument(text);
    				// annnotate the document
    				pipeline.annotate(document);
    				
    				bw.write(cui+"\t"+text+"\t");
        				
    				for(CoreSentence sentence: document.sentences()) {
    					String sentenceText = sentence.text();
    					bw.write(sentenceText+"\t");
    					
    					Tree constituencyParse = sentence.constituencyParse();
						for(Tree subTree: constituencyParse.children()) {
							bw.write("(ROOT");
							printSubTree(subTree, sentence,0,bw);
							bw.write(")\t");
						}
					}
    				bw.write("\n");
    			}
    		}
    		bw.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	private static int printSubTree(Tree root, CoreSentence sentence, int index, BufferedWriter bw) {
		try {
			if(!root.isLeaf()) {
				bw.write(" ("+root.value());
				for(Tree subTree: root.children()) {
					index=printSubTree(subTree,sentence,index,bw);
				}
				bw.write(")");
			}else {
				CoreLabel token = sentence.tokens().get(index);
				bw.write(" "+root.value()+" -> {POS: "+sentence.posTags().get(token.index()-1)+"; Lemma: "+token.lemma()+"; Index: "+token.index()+"; positions: ["+token.beginPosition()+", "+token.endPosition()+"]}");
				index++;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return index;
	}
}
