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


public class Main_run_metamap {
	public static String fileIn = "D:/projects/UMLS2SCT_PostCoordination/disjointUMLS_SCT.txt";
	public static String fileOut = "D:/projects/UMLS2SCT_PostCoordination/metamap_disjointUMLS_SCT.txt";
	private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
	private static boolean isPureAscii(String v) {
		    return asciiEncoder.canEncode(v);
	}
	
	public static void main(String[] args) throws Exception {
		String terms = "food culture direct right";
		MetaMapApi api = new MetaMapApiImpl();
		//api.setOptions("-Dabgioxz -R SNOMEDCT_US");  // 
		//api.setOptions("-D -R SNOMEDCT_US");  // 
		//api.setOptions("-a -R SNOMEDCT_US");  // 
		//api.setOptions("-iga -R SNOMEDCT_US");  // 
		api.setOptions("-biza -R SNOMEDCT_US");  // 
		
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
			/*
			int i=0;
			for(String cui: mapCUIs.keySet()) {
				if(cui.equals("C4298485")) {
					System.out.println("position "+i+" out of "+mapCUIs.size());
					System.exit(0);
				}
				i++;
			}
			*/
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut, true));
			boolean stop = true;
			for(String cui: mapCUIs.keySet()) {
				if(cui.equals("C4036985")) {
					stop = false;
					//continue;
				}
				if(stop) continue;
				System.out.println("cui="+cui);
    			for(String label: mapCUIs.get(cui)) {
    				terms=label;
    				if(label.length()>100) continue;
					List<Result> resultList = api.processCitationsFromString(terms);
    				if(resultList.isEmpty()) continue;
					bw.write(cui+"\t"+terms+"\n");
					for(Result result: resultList) {
						//String machineOutput = result.getMachineOutput();
						//System.out.println(machineOutput);
						
						for (Utterance utterance: result.getUtteranceList()) {
							/*System.out.println("Utterance:");
							System.out.println(" Id: " + utterance.getId());
							System.out.println(" Utterance text: " + utterance.getString());
							System.out.println(" Position: " + utterance.getPosition());
							*/
							for (PCM pcm: utterance.getPCMList()) {
								/*System.out.println("Phrase:");
								System.out.println(" text: " + pcm.getPhrase().getPhraseText());
								System.out.println("Candidates:");
								for (Ev ev: pcm.getCandidateList()) {
						        	System.out.println(" Candidate:");
						        	System.out.println("  Score: " + ev.getScore());
						        	System.out.println("  Concept Id: " + ev.getConceptId());
						        	System.out.println("  Concept Name: " + ev.getConceptName());
						        	System.out.println("  Preferred Name: " + ev.getPreferredName());
						        	System.out.println("  Matched Words: " + ev.getMatchedWords());
						        	System.out.println("  Semantic Types: " + ev.getSemanticTypes());
						        	System.out.println("  MatchMap: " + ev.getMatchMap());
						        	System.out.println("  MatchMap alt. repr.: " + ev.getMatchMapList());
						        	System.out.println("  is Head?: " + ev.isHead());
						        	System.out.println("  is Overmatch?: " + ev.isOvermatch());
						        	System.out.println("  Sources: " + ev.getSources());
						        	System.out.println("  Positional Info: " + ev.getPositionalInfo());
								}*/
								
								//System.out.println("Mappings:");
								//int mapN = 0;
								for (Mapping map: pcm.getMappingList()) {
									//System.out.println(" Map Score: " + map.getScore());
									//System.out.println("mapN="+mapN);
									//mapN++;
									if(map.getEvList().size()<=1) continue;
									for (Ev mapEv: map.getEvList()) {
										bw.write("\t["+mapEv.getConceptId()+", "+mapEv.getConceptName()+", "+mapEv.getPositionalInfo()+"]");
										//System.out.println("   Score: " + mapEv.getScore());
										//System.out.println("   Concept Id: " + mapEv.getConceptId());
										//System.out.println("   Concept Name: " + mapEv.getConceptName());
										//System.out.println("   Preferred Name: " + mapEv.getPreferredName());
										//System.out.println("   Matched Words: " + mapEv.getMatchedWords());
										//System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
										//System.out.println("   MatchMap: " + mapEv.getMatchMap());
										//System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
										//System.out.println("   is Head?: " + mapEv.isHead());
										//System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
										//System.out.println("   Sources: " + mapEv.getSources());
										//System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
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
