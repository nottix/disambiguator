package disambiguator_0_2;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;
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
		initialize();
	}
	
	public void finalize() throws ProcessorException {
		
	}
	
	//Prende l'XDG di input e cerca di disambiguarne il contenuto
	public XDG run(XDG inputXdg) throws ProcessorException {
		IcdList icds = inputXdg.getSetOfIcds();
		IcdList retIcds = new IcdList();
		IcdList icdDisambigued = new IcdList();
		IcdList ret = null;
		IcdList zeroIcdList;
		Icd icd;
		completeList.clear();
		icdList.clear();
		try {
			for(int i=0; i< icds.size(); i++) {
				icd = icds.getIcd(i);
				if( (icd.getPlausibility() < 1) && (!icdList.contains(icd)) )
					icdList.addElement(icd); //Contiene tutti gli icd minori di 1 che sono nella frase.
			}
			if(!icdList.getIcdsWithSourceId(0).isEmpty()) {
				zeroIcdList = icdList.getIcdsWithSourceId(0);
				int toId = zeroIcdList.getIcd(0).getTo().getId();
				for(int i=0; i<icdList.size(); i++) {
					if( (icdList.getIcd(i).getToId()==toId) && (!zeroIcdList.isIn(icdList.getIcd(i))) ) {
						zeroIcdList.addElement(icdList.getIcd(i));
					}
				}
				icdList = icdList.subtract(zeroIcdList);
			}
			for(int h=0; h<icdList.size(); h++) {
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
						completeList.addElement(icdList.get(l));
					}
					
				}
				
				ret = getMoreFrequent(completeList); //Primo caso
				if(ret!=null) {
					ret.get(0).setPlausibility(1);
					icdDisambigued.addElement(ret.getIcd(0));
				}
				else {
					ret = getFrequentSurRelDis(completeList); //Secondo caso
					if(ret!=null) {
						ret.getIcd(0).setPlausibility(1);
						icdDisambigued.addElement(ret.getIcd(0));
					}
					else {
						ret = getFrequentRelDis(completeList); //Terzo caso
						
						if(ret!=null) {
							ret.getIcd(0).setPlausibility(1);
							icdDisambigued.addElement(ret.getIcd(0));
						}
						else {
							ret = getFrequentRel(completeList); //Quarto caso
							if(ret!=null) {
								ret.getIcd(0).setPlausibility(1);
								icdDisambigued.addElement(ret.getIcd(0));
							}
							else {
								Random rand = new Random();
								if(completeList.size()!=0) {
									icdDisambigued.addElement(completeList.getIcd(rand.nextInt(completeList.size())));
								}
							}
						}
					}
				}
				
				if(!icdDisambigued.isEmpty()) {
					completeList = completeList.subtract(icdDisambigued); //icd ambigui
					icds = icds.subtract(completeList); // icd totali meno icd ambigui
					icds.addAll(icdDisambigued); // icd totali piu' icd disambiguati
					icdDisambigued.clear();
				}
				completeList.clear();
			}

			inputXdg.setSetOfIcds(icds);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return inputXdg;
	}
	
	public void printAllIcds(IcdList data) {
		for(int i=0; i<data.size(); i++) {
			if(data.getIcd(i).getFromId()!=0)
				System.out.println("Icd: "+i+", fromsur: "+data.getIcd(i).getFrom().getSurface()+":"+data.getIcd(i).getFromId()+", tosur: "+data.getIcd(i).getTo().getSurface()+":"+data.getIcd(i).getToId()+", plaus: "+data.getIcd(i).getPlausibility());
		}
	}
	
	public int getMax(ArrayList data) {
		for(int i=0; i<data.size(); i++) {
			for(int j=0; j<data.size(); j++) {
				if(j==i) {
					if(j==(data.size())-1)
						return i;
					else
						continue;
				}
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
				if(j==i) {
					if(j==(data.size())-1)
						return i;
					else
						continue;
				}
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
		ArrayList queryResult = new ArrayList();
		IcdList retList = new IcdList();
		DBUtil.queryFrequentSurType(data, queryResult);
		
		int index = getMax(queryResult);
		if(index>=0) {
			retList.addElement(data.getIcd(index));
		}
		else {
			retList = null;
		}
		return retList;
	}
	
	public IcdList getFrequentSurRelDis(IcdList data) {
		ArrayList queryResult = new ArrayList();
		IcdList retList = new IcdList();
		String fromSur;
		String fromType;
		String toSur;
		String toType;
		String query;
		try {
			for(int i=0; i<data.size(); i++) {
				fromSur = data.getIcd(i).getFrom().getSurface(); //costituente from
				fromType = data.getIcd(i).getFrom().getType();
				toSur = data.getIcd(i).getTo().getSurface();
				toType = data.getIcd(i).getTo().getType();
				
				query = "SELECT COUNT(*) FROM icd i WHERE " +
						"(i.fromcs = ? OR i.tocs = ?) AND " +
						"((i.fromct = ? AND i.toct = ?) OR (i.fromct = ? AND i.toct = ?))";
				PreparedStatement ps1 = connection.prepareStatement(query);
				ps1.setString(1, fromSur);
				ps1.setString(2, fromSur);
				ps1.setString(3, fromType);
				ps1.setString(4, toType);
				ps1.setString(5, toType);
				ps1.setString(6, fromType);
				ResultSet rs1 = ps1.executeQuery();
				if(rs1.next())
					queryResult.add(new Integer(rs1.getInt(1)));
				rs1.close();
				ps1.close();
			}
			
			int index=getMax(queryResult);
			if(index>=0) {
				retList.addElement(data.getIcd(index));
				System.out.println("Index max: "+index);
				printAllIcds(retList);
			}
			else
				retList = null;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return retList;
	}
	
	public IcdList getFrequentRel(IcdList data) {
		IcdList retList = new IcdList();
		ArrayList queryResult = new ArrayList();
		System.out.println("DATA SIZE: "+data.size());
		DBUtil.queryFrequentRel(data, queryResult);
		int index=getMax(queryResult);
		if(index>=0) {
			retList.addElement(data.getIcd(index));
			System.out.println("Index max: "+index);
			printAllIcds(retList);
		}
		else
			retList = null;
		System.out.println("DATA SIZE FINE");
		return retList;
	}
	
	public IcdList getFrequentRelDis(IcdList data) {
		ArrayList queryResult= new ArrayList();
		IcdList retList = new IcdList();
		DBUtil.queryFrequentRelDis(data, queryResult);
		
		int index=getMin(queryResult);
		if(index>=0)
			retList.addElement(data.getIcd(index));
		else
			retList = null;
		return retList;
	}
	
	public static void main(String[] args) {
		try {
			Disambiguator disamb = new Disambiguator();
			disamb.initialize();
			String chaos_home = System.getenv("CHAOS_HOME");
			File file = new File(chaos_home+"//chaos");
			File[] list = file.listFiles();
			for(int ii=(int)Math.round(list.length*0.70); ii<list.length; ii++) {
				if(list[ii].isDirectory())
					continue;
				Text t = DBLoader.load_new(list[ii]);
				System.out.println("File aperto: "+list[ii].getName());
				Vector p = t.getParagraphs();
				for(int i=0; i<p.size(); i++) {
					Paragraph vet = (Paragraph)p.get(i);
					Vector xdgs = vet.getXdgs();
					for(int tt=0; tt<xdgs.size(); tt++) {
						XDG xdg = new XDG(); 
						xdg.setSetOfConstituents(((XDG)xdgs.elementAt(tt)).getSetOfConstituents());
						xdg.setSetOfIcds(((XDG)xdgs.elementAt(tt)).getSetOfIcds());
						System.out.println("PRIMA--->>>> "+xdg.getSetOfIcds().size());
						xdg = disamb.run(xdg);
						System.out.println("DOPO--->>>> "+xdg.getSetOfIcds().size());
						xdgs.setElementAt(xdg,tt);
					}
				}
				/*FileOutputStream out2 = new FileOutputStream(new File(chaos_home+"//chaos2//"+list[ii].getName()));
				System.out.println("File scritto: "+chaos_home+"//chaos2//"+list[ii].getName());
				out2.write(t.toXML().getBytes());
				out2.close();*/
				t.save(new File(chaos_home+"//chaos2//"+list[ii].getName()), AvailableOutputFormat.valueOf("xml"), true);
				DBUtil.close();
				System.out.println("Disambiguazione completata");
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}