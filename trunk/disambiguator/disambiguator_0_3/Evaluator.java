/**
 * Progetto di Intelligenza Artificiale 2006/2007
 * 
 * Disambiguatore sintattico:
 * Utilizza algoritmi di disambiguazione stocastici e randomici
 * 
 * @author Simone Notargiacomo, Lorenzo Tavernese
 */

package disambiguator_0_3;

import chaos.XDG.*;
import chaos.textstructure.*;
import java.util.Vector;
import java.io.*;

/**
 * Classe statica utilizzata calcolare la precision, la recall
 * e l'f-measure del disambiguatore sintattico.
 * Deve essere eseguito indipendentemente al disambiguatore.
 * 
 * @version 0.2
 * @author Simone Notargiacomo, Lorenzo Tavernese
 */
public class Evaluator {
	
	private static String dir1 = DBUtil.getCorpusTrainDir();
	private static String dir2 = DBUtil.getCorpusOutputDir();

	public static Vector<IcdList> load(String dir) {
		IcdList list1 = null;
		Vector<IcdList> vl = new Vector<IcdList>();
		Text t = null;
		try {
			File [] files = (new File(dir)).listFiles(); //I docs vengono presi in ordine lessicografico
			for (int z=(int)Math.round(files.length*DBUtil.getPercentualeTrain()); z < files.length; z++) {
				System.out.println("File: "+files[z].getAbsolutePath());
				t = DBLoader.load_new(files[z]);
				Vector<Paragraph> par = t.getParagraphs();
				for(int l = 0; l < par.size(); l++) {
					for(int w=0; w < par.get(l).getXdgs().size(); w++) {
						list1 = ((XDG)(par.get(l).getXdgs()).get(w)).getSetOfIcds();
						vl.add(list1);
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return vl;
	}
	
	public static double calcPrecision2(Vector<IcdList> vl, Vector<IcdList> vl2) {
		double precision = 0.0;
		double num = 0;
		double den = 0;
		double precTot = 0.0;
		double cont = 0;
		try {
			for(int i=0; i < vl.size(); i++) {
				num = intersection(vl,vl2,i);
				den = vl2.get(i).size();	
				if(den == 0.0 || den == 0) {
					cont++;
					continue;
				}
				precision += num/den;
			}
			precTot = precision/(vl.size()-cont);
			System.out.println("vlSize "+vl.size()+" precTot "+precTot);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return precTot;
	}
	
	public static double calcRecall2(Vector<IcdList> vl, Vector<IcdList> vl2) {
		double recall = 0.0;
		double recTot = 0.0;
		double den = 0.0;
		double cont = 0;
		double num;
		for(int i=0; i < vl.size(); i++) {
			num = intersection(vl,vl2,i);
			den = vl.get(i).size();
			if(den == 0.0 || den == 0) {
				cont++;
				continue;
			}
			recall += num/den;
		}
		recTot = recall/(vl.size()-cont);
		System.out.println("vlSize "+vl.size()+" precTot "+recTot);
		return recTot;
	}
	
	public static int intersection(Vector<IcdList> v, Vector<IcdList> v2) {
		int count = 0;
		for(int i=0; i < v.size(); i++)
			count += (v.get(i).intersectionWith(v2.get(i))).size();
		return count;
	}
	
	public static int intersection(Vector<IcdList> v, Vector<IcdList> v2, int i) {
		int count = (v.get(i).intersectionWith(v2.get(i))).size();
		return count;
	}
	
	public static double calcFMeasure(double precision, double recall) {
		return (2*precision*recall)/(precision+recall);
	}
	
	public static void main(String[] args) {
		Vector<IcdList> vl = load(dir1);
		Vector<IcdList> vl2 = load(dir2);
		double precision = calcPrecision2(vl,vl2);
		double recall = calcRecall2(vl,vl2);
		double fmeasure = calcFMeasure(precision, recall);
		System.out.println("Precision: "+precision+", Recall: "+recall+", F-Measure: "+fmeasure);
	}
}