/**
 * Progetto di Intelligenza Artificiale 2006/2007
 * 
 * Disambiguatore sintattico:
 * Utilizza algoritmi di disambiguazione stocastici e randomici
 * 
 * @author Simone Notargiacomo, Lorenzo Tavernese, Ibrahim Khalili
 */

package disambiguator_0_2;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;
import java.io.*;
import java.util.Date;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * Classe principale del modulo di Disambiguazione.
 * Può essere integrato nel parser Chaos per disambiguare
 * singolarmente un XDG
 * 
 * @version 0.2
 * @author Simone Notargiacomo, Lorenzo Tavernese, Ibrahim Khalili
 */
public class Disambiguator extends DependencyProcessor {
	
	/* Lista degli ICD ambigui con lo stesso ID di destinazione, all'interno di una frase */
	private IcdList completeList = new IcdList();
	/* Contiene tutti gli ICD con plausibilità minore di 1 che sono in una frase */
	private IcdList icdList = new IcdList();
	/* Array contenente i risultati di una query */
	private ArrayList queryResult;
	/* Lista contenente l'ICD scelto tra quelli ambigui */
	private IcdList icds;
	private IcdList retIcds;
	private IcdList icdDisambigued;
	private IcdList ret;
	private IcdList zeroIcdList;
	private Icd icd;
	private String toSur;
	private int index;
	public long[] statistics = new long[5];
	
	public Disambiguator() {

	}
	
	/**
	 * Metodo utilizzato per l'inizializzazione del modulo di disambiguazione,
	 * ovvero avvia la connessione al DB relazionale ed inizializza gli oggetti principali della classe.
	 */
	public void initialize() throws ProcessorException {
		DBUtil.startTransaction();
	}
	
	/**
	 * Equivale ad initialize()
	 */
	public void initialize(URL LinguisticResource) throws ProcessorException {
		initialize();
	}
	
	/**
	 * Metodo utilizzato per la chiusura della connessione al DB.
	 */
	public void finalize() throws ProcessorException {
		DBUtil.close();
	}
	
	/**
	 * Disambigua l'XDG che gli viene passato come parametro in ingresso, ovvero
	 * filtra tutti gli ICD ambigui di un XDG e li sottopone agli algoritmi
	 * di disambiguazione.
	 * 
	 * @param inputXdg: XDG da disambiguazre
	 * @return XDG disambiguato
	 */
	public XDG run(XDG inputXdg) throws ProcessorException {
		icds = inputXdg.getSetOfIcds();
		retIcds = new IcdList();
		icdDisambigued = new IcdList();
		ret = null;
		completeList = new IcdList();
		icdList = new IcdList();
		int toId;
		try {
			/* 
			 * Questo ciclo for prende tutti gli ICD che hanno
			 * plausibilità minore di 1 e li inserisce in icdList.
			 */
			for(int i=0; i< icds.size(); i++) {
				icd = icds.getIcd(i);
				if( (icd.getPlausibility() < 1) && (!icdList.contains(icd)) )
					icdList.addElement(icd);
			}
			
			/*
			 * Questa condizione if scarta tutti gli ICD che hanno
			 * ID sorgente uguale a 0. 
			 */
			if(!icdList.getIcdsWithSourceId(0).isEmpty()) {
				zeroIcdList = icdList.getIcdsWithSourceId(0);
				toId = zeroIcdList.getIcd(0).getTo().getId();
				for(int i=0; i<icdList.size(); i++) {
					if( (icdList.getIcd(i).getToId()==toId) && (!zeroIcdList.isIn(icdList.getIcd(i))) ) {
						zeroIcdList.addElement(icdList.getIcd(i));
					}
				}
				icdList = icdList.subtract(zeroIcdList);
			}
			
			/*
			 * Questo ciclo for è il più importante del metodo, infatti
			 * si occupa di sottoporre agli algoritmi di disambiguazione
			 * tutti gli ICD ambigui con lo stesso ID destinazione.
			 * Esegue tale operazione per tutti gli ICD ambigui di una frase.
			 */
			for(int h=0; h<icdList.size(); h++) {
				//fromSur = icdList.getIcd(h).getFrom().getSurface();
				toSur = icdList.getIcd(h).getTo().getSurface();
				if( !retIcds.isIn(icdList.getIcd(h)) ) {
					completeList.addElement(icdList.getIcd(h));
					retIcds.addElement(icdList.getIcd(h)); //Archi già aggiungi alla lista
				}
				for(int l=0; l<icdList.size(); l++) {
					if(icdList.getIcd(l).getTo().getSurface().equals(toSur) &&
							!retIcds.isIn(icdList.getIcd(l)) ) {
						retIcds.addElement(icdList.getIcd(l));
						completeList.addElement(icdList.get(l));
					}
					
				}
				
				/*
				 * Da questo punto iniziano i cinque algoritmi di disambiguazione.
				 * Si sottopone la lista degli ICD ambigui ad ogni algoritmo in sequenza,
				 * in modo che se uno degli algoritmi ritorna un risultato si può passare direttamente
				 * al set di ICD ambigui successivo, ovvero con lo stesso ID destinazione.
				 */
				int start=1;
				switch(start) {
					case 1:
						/*
						 * Se il primo algoritmo di disambiguazione ritorna un risultato
						 * valido, viene impostata la plausibilità dell'ICD disambiguato ad 1. 
						 */
						ret = getFrequentSurType(completeList); //Primo algoritmo
						if(ret!=null)
							break;
					case 2:
						ret = getFrequentSurRel(completeList); //Secondo algoritmo
						if(ret!=null)
							break;
					case 3:
						ret = getFrequentRelDis(completeList); //Terzo algoritmo
						if(ret!=null)
							break;
					case 4:
						ret = getFrequentRel(completeList); //Quarto algoritmo
						if(ret!=null)
							break;
					case 5:
						/*
						 * Questo è l'ultimo algoritmo che equivale alla semplice scelta casuale
						 * dell'ICD.
						 */
						if(completeList.size()!=0) {
							statistics[4]++;
							ret = new IcdList();
							ret.addElement(completeList.getIcd((new Random()).nextInt(completeList.size())));
							ret.getIcd(0).setPlausibility(1);
						}
						break;
					default:
				}
				if(ret!=null)
					icdDisambigued.addElement(ret.getIcd(0)); //Si aggiunge l'ICD alla lista degli ICD disambiguati
				
				/*
				 * Con questo if si eliminano gli ICD ambigui dalla frase e si lasciano quelli disambiguati.
				 */
				if(!icdDisambigued.isEmpty()) {
					completeList = completeList.subtract(icdDisambigued); //icd ambigui
					icds = icds.subtract(completeList); // icd totali meno icd ambigui
					icds.addAll(icdDisambigued); // icd totali piu' icd disambiguati
					icdDisambigued.clear();
				}
				completeList.clear();
			}

			/*
			 * Imposta il nuovo set di ICD nell'XDG da ritornare in uscita.
			 */
			inputXdg.setSetOfIcds(icds);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return inputXdg;
	}
	
	/**
	 * @param data: Lista di ICD da stampare a schermo
	 */
	public void printAllIcds(IcdList data) {
		for(int i=0; i<data.size(); i++) {
			if(data.getIcd(i).getFromId()!=0)
				System.out.println("Icd: "+i+", fromSur: "+data.getIcd(i).getFrom().getSurface()+":"+data.getIcd(i).getFromId()+", toSur: "+data.getIcd(i).getTo().getSurface()+":"+data.getIcd(i).getToId()+", plaus: "+data.getIcd(i).getPlausibility());
		}
	}
	
	/**
	 * @param data: Lista di Integer
	 * @return Il valore massimo della lista
	 */
	public int getMax(ArrayList data) {
		for(int i=0; i<data.size(); i++) {
			for(int j=0; j<data.size(); j++) {
				if(j==i) {
					if(j==(data.size()-1))
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
	
	/**
	 * @param data: Lista di Integer
	 * @return Il valore minimo della lista
	 */
	public int getMin(ArrayList data) {
		for(int i=0; i<data.size(); i++) {
			for(int j=0; j<data.size(); j++) {
				if(j==i) {
					if(j==(data.size()-1))
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
	 * 
	 * 
	 * @param data: Lista contenente gli ICD di coppie di costituenti da ricercare nel Corpus
	 * @return Lista contenente l'ICD della coppia di costituenti più frequente nel Corpus
	 */
	public IcdList getFrequentSurType(IcdList data) {
		queryResult = new ArrayList();
		IcdList resultIcd = new IcdList();
		DBUtil.queryFrequentSurType(data, queryResult);
		
		index = getMax(queryResult);
		if(index>=0) {
			data.getIcd(index).setPlausibility(1);
			resultIcd.addElement(data.getIcd(index));
			statistics[0]++;
		}
		else {
			resultIcd = null;
		}
		return resultIcd;
	}
	
	public IcdList getFrequentSurRel(IcdList data) {
		queryResult = new ArrayList();
		IcdList resultIcd = new IcdList();
		DBUtil.queryFrequentSurRel(data, queryResult);
		index=getMax(queryResult);
		if(index>=0) {
			data.getIcd(index).setPlausibility(1);
			resultIcd.addElement(data.getIcd(index));
			printAllIcds(resultIcd);
			statistics[1]++;
		}
		else
			resultIcd = null;
		return resultIcd;
	}
	
	public IcdList getFrequentRel(IcdList data) {
		queryResult = new ArrayList();
		IcdList resultIcd = new IcdList();
		DBUtil.queryFrequentRel(data, queryResult);
		index=getMax(queryResult);
		if(index>=0) {
			data.getIcd(index).setPlausibility(1);
			resultIcd.addElement(data.getIcd(index));
			printAllIcds(resultIcd);
			statistics[3]++;
		}
		else
			resultIcd = null;
		return resultIcd;
	}
	
	public IcdList getFrequentRelDis(IcdList data) {
		queryResult = new ArrayList();
		IcdList resultIcd = new IcdList();
		DBUtil.queryFrequentRelDis(data, queryResult);
		index=getMin(queryResult);
		if(index>=0) {
			data.getIcd(index).setPlausibility(1);
			resultIcd.addElement(data.getIcd(index));
			statistics[2]++;
		}
		else
			resultIcd = null;
		return resultIcd;
	}
	
	public static void main(String[] args) {
		try {
			String chaos_home = System.getenv("CHAOS_HOME");
			Text text;
			Vector paragraphs;
			Paragraph parVet;
			Vector xdgs;
			Disambiguator disambiguator = new Disambiguator();
			disambiguator.initialize(); //Inizializzazione del Disambiguatore
			long start = System.currentTimeMillis();
			File[] list = (new File(chaos_home+"//chaos")).listFiles();
			for(int j=(int)Math.round(list.length*0.70); j<list.length; j++) {
				if(list[j].isDirectory())
					continue;
				text = DBLoader.load_new(list[j]);
				System.out.println("File aperto: "+list[j].getName());
				paragraphs = text.getParagraphs();
				for(int i=0; i<paragraphs.size(); i++) {
					parVet = (Paragraph)paragraphs.get(i);
					xdgs = parVet.getXdgs();
					for(int t=0; t<xdgs.size(); t++) {
						XDG xdg = new XDG(); 
						xdg.setSetOfConstituents(((XDG)xdgs.elementAt(t)).getSetOfConstituents());
						xdg.setSetOfIcds(((XDG)xdgs.elementAt(t)).getSetOfIcds());
						System.out.println("Icd prima della disambiguazione: "+xdg.getSetOfIcds().size());
						xdg = disambiguator.run(xdg);
						System.out.println("Icd dopo la disambiguazione: "+xdg.getSetOfIcds().size());
						xdgs.setElementAt(xdg,t);
					}
				}
				/*FileOutputStream out2 = new FileOutputStream(new File(chaos_home+"//chaos2//"+list[ii].getName()));
				System.out.println("File scritto: "+chaos_home+"//chaos2//"+list[ii].getName());
				out2.write(t.toXML().getBytes());
				out2.close();*/
				text.save(new File(chaos_home+"//chaos2//"+list[j].getName()), AvailableOutputFormat.valueOf("xml"), true);
				System.out.println("File scritto: "+chaos_home+"//chaos2//"+list[j].getName());
			}
			disambiguator.finalize();
			Date date = new Date(System.currentTimeMillis()-start);
			System.out.println("Tempo impiegato: "+date.getMinutes()+" minuti e "+date.getSeconds()+" secondi");
			System.out.println("Statistiche: Primo: "+disambiguator.statistics[0]+", Secondo: "+disambiguator.statistics[1]+"," +
					" Terzo: "+disambiguator.statistics[2]+", Quarto: "+disambiguator.statistics[3]+", Quinto: "+disambiguator.statistics[4]);
			System.out.println("Disambiguazione completata");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}