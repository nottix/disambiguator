package disambiguator_0_2;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
//import chaos.alternatives.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

public class Disambiguator extends DependencyProcessor {
	
	private IcdList completeList = new IcdList();
	private IcdList icdList = new IcdList();
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
		IcdList icds = inputXdg.getSetOfIcds();
		//printAllIcds(icds);
		IcdList retIcds = new IcdList();
		IcdList icdDisambigued = new IcdList();
		IcdList ret = null;
		Icd icd;
		completeList.clear();
		icdList.clear();
		try {
			for(int i=0; i< icds.size(); i++) {
				icd = icds.getIcd(i);
				//System.out.println("cicle: "+i+", icd: "+icd.getPlausibilityAsString());
				if( (icd.getPlausibility() < 1) && (!icdList.contains(icd)) )
				{
					icdList.addElement(icd); //Contiene tutti gli icd minori di 1 che sono nella frase.
					//System.out.println("completeList: "+icd.getFrom().getSurface()+":"+icd.getFromId()+", "+icd.getTo().getSurface()+":"+icd.getToId());
				}
			}
			//System.out.println("Fine cicli");
			//printAllIcds(icdList);
			for(int h=0; h<icdList.size(); h++) {
				//System.out.println("h: "+h);
				String surface = icdList.getIcd(h).getFrom().getSurface();
				String surfaceTo = icdList.getIcd(h).getTo().getSurface();
				if( !retIcds.isIn(icdList.getIcd(h)) ) {
					completeList.addElement(icdList.getIcd(h));
					retIcds.addElement(icdList.getIcd(h)); //Archi già fatti
				}
				for(int l=0; l<icdList.size(); l++) {
					if(icdList.getIcd(l).getTo().getSurface().equals(surfaceTo) &&
							!retIcds.isIn(icdList.getIcd(l)) ) {
						retIcds.addElement(icdList.getIcd(l));
						//System.out.println("BEFORE: "+((Icd)icdList.get(l)).getFrom().getSurface()+((Icd)icdList.get(l)).getTo().getSurface());
						completeList.addElement(icdList.get(l));
					}
					
				}
				//printAllIcds(completeList);
				
				ret = getMoreFrequent(completeList); //Primo caso
				//ret = new ArrayList();
				if(ret!=null) {
					ret.get(0).setPlausibility(1);
					icdDisambigued.addElement(ret.getIcd(0));
					//System.out.println("WINNER: "+(String)ret.get(0)+", "+(String)ret.get(1));
				}
				else {
					ret = getFrequentRelDis(completeList); //Secondo caso
					if(ret!=null) {
						ret.getIcd(0).setPlausibility(1);
						icdDisambigued.addElement(ret.getIcd(0));
					}
					else {
						ret = getFrequentRel(completeList); //Terzo caso
						if(ret!=null) {
							ret.getIcd(0).setPlausibility(1);
							icdDisambigued.addElement(ret.getIcd(0));
						}
						else {
							//Altro caso
						}
					}
				}
				completeList.clear();
			}
			//System.out.println("FINE, completeList.size(): "+completeList.size());

			//printAllIcds(icdDisambigued);
			retIcds = retIcds.subtract(icdDisambigued); //icd ambigui
			icds = icds.subtract(retIcds); // icd totali meno icd ambigui
			icds.addAll(icdDisambigued); // icd totali piu' icd disambiguati
			//printAllIcds(icds);
			inputXdg.setSetOfIcds(icds);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return inputXdg;
	}
	
	public void printAllIcds(IcdList data) {
		for(int i=0; i<data.size(); i++) {
			if(data.getIcd(i).getFrom()!=null)
				System.out.println("Icd: "+i+", fromsur: "+data.getIcd(i).getFrom().getSurface()+":"+data.getIcd(i).getFromId()+", tosur: "+data.getIcd(i).getTo().getSurface()+":"+data.getIcd(i).getToId()+", plaus: "+data.getIcd(i).getPlausibility());
		}
	}
	
	public int getMax(ArrayList data) {
		for(int i=0; i<data.size(); i++) {
			//System.out.println("i: "+(Integer)data.get(i));
			for(int j=0; j<data.size(); j++) {
				//System.out.println("j: "+(Integer)data.get(j));
				if(j==i)
					continue;
				if( ((Integer)data.get(i)).compareTo((Integer)data.get(j)) > 0 ) {
					if(j==(data.size()-1))
						return i;
				}
				else {
					j=data.size();
				}
			}
		}
		return -1;
	}
	
	public int getMin(ArrayList data) {
		for(int i=0; i<data.size(); i++) {
			for(int j=0; j<data.size(); j++) {
				if(j==i)
					continue;
				if( ((Integer)data.get(i)).compareTo((Integer)data.get(j)) < 0 ) {
					if(j==(data.size()-1))
						return i;
				}
				else {
					j=data.size();
				}
			}
		}
		return -1;
	}
	
	/**
	 * @param data: Lista di coppie di costituenti da ricercare nel Corpus
	 * @return Lista contenente la coppia di costituenti piï¿½ frequente nel Corpus
	 */
	public IcdList getMoreFrequent(IcdList data) {
		//Icd icd;
		ArrayList queryResult = new ArrayList();
		//System.out.println("DATA: "+data.size());
		IcdList retList = new IcdList();
		//String query;
		//try {
			/*for(int i=0; i<data.size(); i++) {
				icd = data.getIcd(i);
				query = "SELECT COUNT(i.idfrase) FROM icd i WHERE ((i.fromcs = ?) OR" +
					" (i.tocs = ?)) AND " +
					"((i.fromcs = ?) OR (i.tocs = ?)) AND (((i.fromct = ?) AND (i.toct = ?)) OR ((i.fromct = ?) AND (i.toct = ?)))";
				//System.out.println(query);
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setString(1, icd.getFrom().getSurface());
				ps.setString(2, icd.getFrom().getSurface());
				ps.setString(3, icd.getTo().getSurface());
				ps.setString(4, icd.getTo().getSurface());
				ps.setString(5, icd.getFrom().getType());
				ps.setString(6, icd.getTo().getType());
				ps.setString(7, icd.getTo().getType());
				ps.setString(8, icd.getFrom().getType());
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					queryResult.add(new Integer(rs.getInt(1)));
					//System.out.println("Rs: "+rs.getInt(1));
				}
				rs.close();
				ps.close();
			}*/
			DBUtil.queryFrequentSurType(data, queryResult);
			int index = getMax(queryResult);
			if(index>=0) {
				retList.addElement(data.getIcd(index));
			}
			else {
				retList = null;
			}
		//}
		//catch(Exception e) {
		//	e.printStackTrace();
		//}
		
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
	
	public IcdList getFrequentRel(IcdList data) {
		IcdList retList = new IcdList();
		ArrayList queryResult = new ArrayList();
		/*String fromConstType;
		String toConstType;
		String query;*/
		
		//try {
			/*for(int i=0; i<data.size(); i++) {
				fromConstType=data.getIcd(i).getFrom().getType();
				toConstType=data.getIcd(i).getTo().getType();
				query="SELECT COUNT(i.idfrase) FROM icd i WHERE (i.fromct=? AND i.toct=?) OR (i.toct=? AND i.fromct=?)";
				
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setString(1, fromConstType);
				ps.setString(2, toConstType);
				ps.setString(3, fromConstType);
				ps.setString(4, toConstType);
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					System.out.println("Int: "+rs.getInt(1));
					out.add(new Integer(rs.getInt(1)));
				}
				rs.close();
				ps.close();
			}*/
		DBUtil.queryFrequentRel(data, queryResult);
			int index=getMax(queryResult);
			if(index>=0) {
				retList.addElement(data.getIcd(index));
				System.out.println("Index: "+index);
				printAllIcds(retList);
			}
			else
				retList = null;
		/*}
		catch(Exception e) {
			e.printStackTrace();
		}*/
		return retList;
	}
	
	public IcdList getFrequentRelDis(IcdList data) {
		ArrayList queryResult= new ArrayList();
		IcdList retList = new IcdList();
		/*String fromConstType;
		String toConstType;
		String query;*/
		/*try{
			for(int i=0; i<data.size();i++){
			
				fromConstType=data.getIcd(i).getFrom().getType();
				toConstType=data.getIcd(i).getTo().getType();
				query = "SELECT MIN(ABS(i.fromct-i.toct)) FROM icd i WHERE i.toct = ? AND i.fromct = ?";
			
				PreparedStatement ps1 = connection.prepareStatement(query);
				ps1.setString(1, toConstType);
				ps1.setString(2, fromConstType);
				ResultSet rs= ps1.executeQuery();
				
				if(rs.next()) {
					out.add(new Integer(rs.getInt(1)));
				}
				rs.close();
				ps1.close();
			}
			*/
		DBUtil.queryFrequentRelDis(data, queryResult);
			int index=getMin(queryResult);
			if(index>=0) {
				retList.addElement(data.getIcd(index));
			}
			else
				retList = null;
		/*}
		catch(Exception e){
				e.printStackTrace();		
		}*/
		
		return retList;
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
			for(int ii=0; ii<2; ii++) {
				if(list[ii].isDirectory())
					continue;
			Text t = DBLoader.load_new(list[ii]);
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