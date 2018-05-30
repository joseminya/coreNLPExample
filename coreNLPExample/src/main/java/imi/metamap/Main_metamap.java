package imi.metamap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
public class Main_metamap {
	private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
	private static boolean isPureAscii(String v) {
		    return asciiEncoder.canEncode(v);
	}
	public static void main(String[] args) {
		String fileIn	= "D:/projects/UMLS2SCT_PostCoordination/disjointUMLS_SCT.txt";
		String fileOut	= "D:/projects/UMLS2SCT_PostCoordination/metamap_disjointUMLS_SCT.txt";
		fileIn	= "D:/projects/UMLS2SCT_PostCoordination/sct_conceptTerms.txt";
		fileOut	= "D:/projects/UMLS2SCT_PostCoordination/metamap_sctTerms.txt";
		
		if(args.length==3) {
			fileIn = args[1];
			fileOut = args[2];
		}
		
		MetaMapApi api = new MetaMapApiImpl();
		//api.setOptions("-biza -R SNOMEDCT_US");
		api.setOptions("-R SNOMEDCT_US");
		String terms = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileIn));
			String line ="";
			Map<String,Set<String>> mapCUIs = new HashMap<String,Set<String>>();
	    	
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
			
			boolean stop = false;
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut, true));
			for(String cui: mapCUIs.keySet()) {
				//System.out.println("cui="+cui);
				if(cui.equals("360859001")) stop = true;
				if(!stop) continue;
    			for(String label: mapCUIs.get(cui)) {
    				terms=label;
    				if(label.length()>100) continue;
					List<Result> resultList = api.processCitationsFromString(terms);
    				if(resultList.isEmpty()) continue;
					bw.write(cui+"\t"+terms+"\n");
					for(Result result: resultList) {
						for (Utterance utterance: result.getUtteranceList()) {
							for (PCM pcm: utterance.getPCMList()) {
								for (Mapping map: pcm.getMappingList()) {
									if(map.getEvList().size()<=1) continue;
									for (Ev mapEv: map.getEvList()) {
										bw.write("\t["+mapEv.getConceptId()+", "+mapEv.getConceptName()+", "+mapEv.getPositionalInfo()+"]");
									}
									bw.write("\n");
								}
							}
						}
					}
    			}
			}
			bw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
