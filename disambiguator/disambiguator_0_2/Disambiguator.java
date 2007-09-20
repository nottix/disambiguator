package disambiguator;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
//import chaos.alternatives.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

public class Disambiguator extends DependencyProcessor {
	
	private ArrayList constList = new ArrayList();
	private ArrayList completeList = new ArrayList();
	private ArrayList icdList = new ArrayList();
	private Connection connection;
	
	public void initialize() throws ProcessorException {
		connection = DBUtil.startTransaction();
	}
	
	public void initialize(URL LinguisticResource) throws ProcessorException {
		
	}
	
	public void finalize() throws ProcessorException {
		
	}
	
	//Prende l'XDG di input e cerca di disambiguarne il contenuto
	public XDG run(XDG inputXdg) throws ProcessorException {
		//XDG newXDG = null;
		IcdList icds = inputXdg.getSetOfIcds();
		IcdList retIcds = new IcdList();
		IcdList icdList2 = new IcdList();
		ArrayList ret = null;
		//IcdList icdPlausList;
		Icd icd;
		completeList.clear();
		icdList.clear();
		try {
			for(int i=0; i< icds.size(); i++) {
				constList.clear();
				icd = icds.getIcd(i);
				System.out.println("cicle: "+i+", icd: "+icd.getPlausibilityAsString());
				if( (icd.getPlausibility() < 1) && (!icdList.contains(icd)) )
				{
					icdList.add(icd);
					System.out.println("completeList: "+icd.getFrom().getSurface()+", "+icd.getTo().getSurface());
				}
			}
			for(int h=0; h<icdList.size(); h++) {
				String surface = ((Icd)icdList.get(h)).getFrom().getSurface();
				String surfaceTo = ((Icd)icdList.get(h)).getTo().getSurface();
				if( !retIcds.isIn((Icd)icdList.get(h)) ) {
					constList.add(0, surface);
					constList.add(1, surfaceTo);
					constList.add(2, (Icd)icdList.get(h));
					constList.add(3, ((Icd)icdList.get(h)).getFrom().getType());
					constList.add(4, ((Icd)icdList.get(h)).getTo().getType());
					completeList.add(constList.clone());
					retIcds.add((Icd)icdList.get(h));
				}
				for(int l=0; l<icdList.size(); l++) {
					if(((Icd)icdList.get(l)).getTo().getSurface().equals(surfaceTo) &&
							!retIcds.isIn((Icd)icdList.get(l)) ) {
						constList.clear();
						constList.add(0, ((Icd)icdList.get(l)).getFrom().getSurface());
						constList.add(1, ((Icd)icdList.get(l)).getTo().getSurface());
						constList.add(2, (Icd)icdList.get(l));
						constList.add(3, ((Icd)icdList.get(h)).getFrom().getType());
						constList.add(4, ((Icd)icdList.get(h)).getTo().getType());
						retIcds.add((Icd)icdList.get(l));
						//System.out.println("BEFORE: "+((Icd)icdList.get(l)).getFrom().getSurface()+((Icd)icdList.get(l)).getTo().getSurface());
						completeList.add(constList.clone());
					}
					
				}
				
				/*for(int l=0; l<icdList.size(); l++) {
					if(((Icd)icdList.get(l)).getFrom().getSurface().equals(surface) ||
							((Icd)icdList.get(l)).getTo().getSurface().equals(surface)) {
						constList.add(0, ((Icd)icdList.get(l)).getFrom().getSurface());
						constList.add(1, ((Icd)icdList.get(l)).getTo().getSurface());
						completeList.add(constList);
						icdList.remove(l);
					}
				}*/
				
				for(int i=0; i<completeList.size(); i++) {
					System.out.println("COMPLETELIST: "+(String)((ArrayList)completeList.get(i)).get(0)+
							", "+(String)((ArrayList)completeList.get(i)).get(1)+
							", "+((Icd)((ArrayList)completeList.get(i)).get(2)).getToId() );
				}
				
				Icd icdPlaus;
				ret = getMoreFrequent(completeList); //Primo caso
				//ret = new ArrayList();
				if(ret!=null) {
					icdPlaus = (Icd)ret.get(2);
					icdPlaus.setPlausibility(1);
					icdList2.add(icdPlaus);
					//System.out.println("WINNER: "+(String)ret.get(0)+", "+(String)ret.get(1));
				}
				else {
					//System.out.println("Vuoto");
					ret = getFrequentRelDis(completeList); //Secondo caso
					if(ret!=null) {
						icdPlaus = (Icd)ret.get(2);
						icdPlaus.setPlausibility(1);
						icdList2.add(icdPlaus);
					}
					else {
						for(int i=0; i<completeList.size(); i++) {
							icdList2.add( (Icd)((ArrayList)completeList.get(i)).get(2) );
						}
					}
				}
				completeList.clear();
			}
			System.out.println("FINE, completeList.size(): "+completeList.size());
			
						
			/*for(int k=0; k<completeList.size(); k++) {
				System.out.println("List "+k+": "+((Constituent)((ArrayList)completeList.get(k)).get(0)).getSurface()+", "+((Constituent)((ArrayList)completeList.get(k)).get(1)).getSurface());
			}*/
			retIcds = retIcds.subtract(icdList2);
			icds = icds.subtract(retIcds);
			icds.addAll(icdList2);
			inputXdg.setSetOfIcds(icds);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return inputXdg;
	}
	
	/**
	 * @param data: Lista di coppie di costituenti da ricercare nel Corpus
	 * @return Lista contenente la coppia di costituenti piï¿½ frequente nel Corpus
	 */
	public ArrayList getMoreFrequent(ArrayList data) {
		//System.out.println("DATA: "+data.size());
		ArrayList retList = new ArrayList();
		ArrayList constList;
		int[] freq = new int[100];
		String query;
		int max = 0;
		int indexMax = 0;
		try {
			for(int i=0; i<data.size(); i++) {
				constList = (ArrayList)data.get(i);
				query = "SELECT COUNT(i.idfrase) FROM icd i WHERE ((i.fromcs = ?) OR" +
					" (i.tocs = ?)) AND" +
					"((i.fromcs = ?) OR (i.tocs = ?)) AND ((i.fromct = ?) OR (i.toct = ?))";
				//System.out.println(query);
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setString(1, (String)constList.get(0));
				ps.setString(2, (String)constList.get(0));
				ps.setString(3, (String)constList.get(1));
				ps.setString(4, (String)constList.get(1));
				ps.setString(5, (String)constList.get(3));
				ps.setString(6, (String)constList.get(3));
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					freq[i] = rs.getInt(1);
					if(rs.getInt(1)>max) {
						retList.clear();
						max = rs.getInt(1);
						indexMax = i;
					}
					//System.out.println("Rs: "+rs.getInt(1));
				}
				rs.close();
				ps.close();
			}
			int found=0;
			for(int i=0; i<freq.length; i++) {
				if(freq[i]==indexMax) {
					found++;
				}
			}
			if(found>1 || found==0)
				retList = null;
			else {
				System.out.println("found: "+indexMax);
				retList.add(0, (String)((ArrayList)data.get(indexMax)).get(0));
				retList.add(1, (String)((ArrayList)data.get(indexMax)).get(1));
				retList.add(2, ((ArrayList)data.get(indexMax)).get(2));
				retList.add(3, (String)((ArrayList)data.get(indexMax)).get(3));
				retList.add(4, (String)((ArrayList)data.get(indexMax)).get(4));
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return retList;
	}
	
	public ArrayList getFrequentSurRelDis(ArrayList data) {
		ArrayList input;
		ArrayList out = new ArrayList();
		String fromSur;
		String fromType;
		String toSur;
		String toType;
		String query;
		int res1=0, res2=0;
		Integer avg;
		try {
			for(int i=0; i<data.size(); i++) {
				//from(surface, type) to(type)
				input = (ArrayList)data.get(i);
				fromSur = (String)input.get(0); //costituente from
				fromType = (String)input.get(3);
				toSur = (String)input.get(1);
				toType = (String)input.get(4);
				
				query = "SELECT COUNT(i.idfrase) FROM icd i WHERE " +
						"i.fromcs = ? AND i.fromct = ? AND " +
						"i.toct = ?";
				PreparedStatement ps1 = connection.prepareStatement(query);
				ps1.setString(1, fromSur);
				ps1.setString(2, fromType);
				ps1.setString(3, toType);
				ResultSet rs1 = ps1.executeQuery();
				if(rs1.next()) {
					res1 = rs1.getInt(1);
					System.out.println("RES1: "+res1);
				}
				rs1.close();
				ps1.close();
				
				//to(surface, type) from(type)
				query = "SELECT COUNT(i.idfrase) FROM icd i WHERE " +
				"i.tocs = ? AND i.toct = ? AND " +
				"i.fromct = ?";
				PreparedStatement ps2 = connection.prepareStatement(query);
				ps2.setString(1, toSur);
				ps2.setString(2, toType);
				ps2.setString(3, fromType);
				ResultSet rs2 = ps2.executeQuery();
				if(rs2.next()) {
					res2 = rs2.getInt(1);
				}
				rs2.close();
				ps2.close();
				
				//media tra res1 e res2
				avg = (res1+res2)/2;
				out.add(avg);
				System.out.println("AVG: "+avg);
			}
			
			//scelta avg più frequente
			int index=-1;
			for(int j=0; j<out.size(); j++) {
				for(int x=0; x<out.size(); x++) {
					if(((Integer)out.get(j)).compareTo((Integer)out.get(x)) > 0 ) {
						if(x==(out.size()-1)) {
							index = j;
							j = x = out.size();
						}
					}
					else
						x = out.size();
				}
			}
			
			if(index>=0) {
				out.clear();
				out.add(data.get(index));
				System.out.println((String)data.get(index));
			}
			else
				out = null;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}
	public ArrayList getFrequentRelDis(ArrayList data){
		ArrayList out= new ArrayList();
		String fromConstType;
		String toConstType;
		String query;
	
		try{
			for(int i=0; i<data.size();i++){
			
				fromConstType=((ArrayList)data.get(i)).get(3).toString();
				toConstType=((ArrayList)data.get(i)).get(4).toString();
				//query = "SELECT MIN(ABS(i.fromct-i.toct)) FROM icd i WHERE i.toct = ? AND i.fromct = ?";
				query = "SELECT MIN(ABS(i.fromct-i.toct)) FROM icd i WHERE i.toct = ? AND i.fromct = ?";
			
				PreparedStatement ps1 = connection.prepareStatement(query);
				ps1.setString(1, fromConstType);
				ps1.setString(2,toConstType);
				ResultSet rs= ps1.executeQuery();
				
				if(rs.next()) {
					out.add(rs.getInt(1));
				}
				rs.close();
				ps1.close();
			}
			int index=-1;
			for(int j=0; j<out.size(); j++) {
				for(int x=0; x<out.size(); x++) {
					if(((Integer)out.get(j)).compareTo((Integer)out.get(x)) < 0 ) {
						if(x==(out.size()-1)) {
							index = j;
							j = x = out.size();
						}
					}
					else
						x = out.size();
				}
			}
			
			if(index>=0) {
				out.clear();
				out.add(data.get(index));
				System.out.println((String)data.get(index));
			}
			else
				out = null;
		}
		catch(Exception e){
				e.printStackTrace();		
		}
		
		return out;
	}
	
	public double calcDepProb(Object arr[],Object arr2[],String type) {
		double prob = 0.0;
		double temp = 0;
		double temp2 = 0;
		for(int k=0; k<arr.length;k++) {
			for(int l=0; l<arr2.length; l++) {
				if(k!=l /*&&*/ )
					temp++;
				else if(k!=l /*&&*/ )
					temp2++;
			}
		}		
		prob = temp / temp2;
		return prob;
	}
	
	public static void main(String[] args) {
		try {
			Disambiguator disamb = new Disambiguator();
			disamb.initialize();
			String chaos_home = System.getenv("CHAOS_HOME");
			//File file = new File(chaos_home+"/")
			File file = new File(chaos_home+"//chaos");
			File[] list = file.listFiles();
			System.out.println(list[0].getAbsolutePath());
			for(int ii=0; ii<list.length; ii++) {
				if(list[ii].isDirectory())
					continue;
			Text t = dbClass.load_new(list[ii]);
			System.out.println("File aperto: "+list[ii].getName());
			//t.save(new File("C:\\ChaosParser\\Chaos2\\treebank2000gold-CONLL_ORG_UTF8_0.coln_test.xml"), AvailableOutputFormat.valueOf("xml"), true);
			/*FileOutputStream out = new FileOutputStream(new File("C:\\ChaosParser\\Chaos2\\treebank2000gold-CONLL_ORG_UTF8_0_test.coln.xml"));
			out.write(t.toXML().getBytes());
			out.close();*/
			Vector p = t.getParagraphs();
			for(int i=0; i<p.size(); i++) {
				//System.out.println("size p: "+p.size()+", i: "+i);
				Paragraph vet = (Paragraph)p.get(i);
				Vector xdgs = vet.getXdgs();
				for(int tt=0; tt<xdgs.size(); tt++) {
					//System.out.println("size: "+xdgs.size()+", xdg: "+tt);
					XDG xdg = new XDG(); 
					xdg.setSetOfConstituents(((XDG)xdgs.elementAt(tt)).getSetOfConstituents());
					xdg.setSetOfIcds(((XDG)xdgs.elementAt(tt)).getSetOfIcds());
					System.out.println("PRIMA--->>>> "+xdg.getSetOfIcds().size());
					xdg = disamb.run(xdg);
					System.out.println("DOPO--->>>> "+xdg.getSetOfIcds().size());
					xdgs.setElementAt(xdg,tt);
				}
			}
			//BufferedWriter out = new BufferedWriter(new FileWriter(new File("C:\\ChaosParser\\Chaos2\\treebank2000gold-CONLL_ORG_UTF8_0.coln.xml")));
			FileOutputStream out2 = new FileOutputStream(new File(chaos_home+"//chaos2//"+list[ii].getName()));
			System.out.println("File scritto: "+chaos_home+"//chaos2//"+list[ii].getName());
			out2.write(t.toXML().getBytes());
			out2.close();
			}
			//t.save(new File("C:\\ChaosParser\\Chaos2\\treebank2000gold-CONLL_ORG_UTF8_0.coln.xml"), AvailableOutputFormat.valueOf("xml"), true);
			System.out.println("out");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}