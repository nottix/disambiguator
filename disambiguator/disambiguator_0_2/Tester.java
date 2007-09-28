package disambiguator_0_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Vector;

import chaos.XDG.IcdList;
import chaos.XDG.XDG;
import chaos.alternatives.AvailableOutputFormat;
import chaos.textstructure.Paragraph;
import chaos.textstructure.Text;

public class Tester {

	/**
	 * Serve per dare una dimostrazione delle funzionalità di tutto il progetto.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			InputStreamReader isr;
			BufferedReader stdin;
			int input;
			do {
				printMenu();
				isr = new InputStreamReader( System.in );
				stdin = new BufferedReader( isr );
				input = stdin.read();
				switch(input) {
					case '1':
						loadDB();
						break;
					case '2':
						removeTrainCorpus();
						break;
					case '3':
						removeDB();
						break;
					case '4':
						startDisambiguator();
						System.out.print("\nPremere un tasto per continuare...");
						stdin.read();
						break;
					case '5':
						evaluate();
						System.out.print("\nPremere un tasto per continuare...");
						stdin.read();
						break;
					case '0':
						break;
					default:
						System.out.println("Selezionare una delle opzioni elencate!");
						break;
				}
			}while(input != '0');
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printMenu() {
		System.out.print("Progetto di Intelligenza Artificiale 2006/2007\n\n" +
			"1: Creare il DB e caricare il corpus di training\n" +
			"2: Rimuovere il corpus di training dal DB\n" +
			"3: Rimuovere il DB chaos\n" +
			"------------------------------------------\n" +
			"4: Avviare la disambiguazione\n" +
			"5: Calcolare la precision e la recall\n" +
			"0: Uscire\n\n>");
	}
	
	private static void loadDB() {
		DBUtil.startTransaction();
		DBLoader.setPerNum(DBUtil.getPercentualeTrain());
		DBLoader.addToDB();
		DBUtil.close();
	}
	
	private static void removeTrainCorpus() {
		DBUtil.startTransaction();
		DBUtil.queryFreeTable();
		DBUtil.close();
	}

	private static void removeDB() {
		DBUtil.startTransaction();
		DBUtil.queryFreeDB();
		DBUtil.close();
	}
	
	private static void evaluate() {
		Evaluator.loadDir();
		Evaluator.calcPrecisionRecall();
	}
	
	private static void startDisambiguator() {
		try {
			Text text;
			Vector paragraphs;
			Paragraph parVet;
			Vector xdgs;
			Disambiguator disambiguator = new Disambiguator();
			disambiguator.initialize(); //Inizializzazione del Disambiguatore
			long start = System.currentTimeMillis();
			File[] list = (new File(DBUtil.getCorpusChaosDir())).listFiles();
			for(int j=(int)Math.round(list.length*DBUtil.getPercentualeTrain()); j<list.length; j++) {
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
				text.save(new File(DBUtil.getCorpusOutputDir()+list[j].getName()), AvailableOutputFormat.valueOf("xml"), true);
				System.out.println("File scritto: "+DBUtil.getCorpusOutputDir()+list[j].getName());
			}
			disambiguator.finalize(); //Rilascio risorse del disambiguatore
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
