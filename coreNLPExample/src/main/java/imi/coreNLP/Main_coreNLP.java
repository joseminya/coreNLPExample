package imi.coreNLP;

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
public class Main_coreNLP {
	private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
	private static boolean isPureAscii(String v) {
	    return asciiEncoder.canEncode(v);
	}
	
	public static void main(String[] args) {
		String fileIn = "D:/projects/UMLS2SCT_PostCoordination/disjointUMLS_SCT.txt";
		String fileOut = "D:/projects/UMLS2SCT_PostCoordination/coreNLP_disjointUMLS_SCT.txt";
		fileIn	= "D:/projects/UMLS2SCT_PostCoordination/sct_conceptTerms.txt";
		fileOut	= "D:/projects/UMLS2SCT_PostCoordination/coreNLP_SCTTerms.txt";
		if(args.length==3) {
			fileIn = args[1];
			fileOut = args[2];
		}
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse");
	    props.setProperty("coref.algorithm", "neural");
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
    		boolean stop = false;
    		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut,true));
    		for(String cui: mapCUIs.keySet()) {
    			if(cui.equals("")) stop=true;
    			if(!stop) continue;
    			for(String label: mapCUIs.get(cui)) {
    				text=label;
    				if(label.length()>100) continue;
    				CoreDocument document = new CoreDocument(text);
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
