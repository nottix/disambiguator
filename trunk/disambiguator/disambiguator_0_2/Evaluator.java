package disambiguator_0_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;
import java.io.*;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Evaluator {
	
	private static String chaos_home = System.getenv("CHAOS_HOME");
	private static String dir1 = chaos_home+"//AI_train";
	private static String dir2 = chaos_home+"//Chaos2";

	public static Text load_new(File text_file) throws Exception {
        Text text = null;
        
        if (text_file.getName().endsWith(".xml")) {
            text = new Text();
            DocumentBuilder dbuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document xmldoc =
                    dbuilder.parse(text_file);
            text.fromDOM(xmldoc.getDocumentElement());
            dbuilder.reset();
        } else if (text_file.getName().endsWith(".cha")) {
            text = new Text();
            ObjectInputStream ois = new ObjectInputStream(new
                    FileInputStream(text_file));
            text = (chaos.textstructure.Text)ois.readObject();
        } else if (text_file.getName().endsWith(".txt")) {
            String plain_text = "";
            BufferedReader inputFile = new BufferedReader(new
                    FileReader(text_file));
            String inputLine ;
            while ( (inputLine = inputFile.readLine()) != null )
                plain_text += inputLine + "\n";
            inputFile.close();
            text = new Text(plain_text);
            
        }
        return text;
    }
	
	public static Vector<IcdList> load(String dir)
	{
		IcdList list1 = null;
		Vector<IcdList> vl = new Vector<IcdList>();
		Text t = null;
		try
		{
			
			File [] files = (new File(dir)).listFiles(); //I docs vengono presi in ordine lessicografico, cambiare la lettura facendoli prendere in ordine????
			for (int z=(int)Math.round(files.length*0.7); z < files.length; z++) { //70 è il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
				System.out.println("File in caricamento: "+files[z].getAbsolutePath());
				t =load_new(files[z]);
				Vector<Paragraph> par = t.getParagraphs();
				for(int l = 0; l < par.size(); l++)
				{
					Vector<XDG> xdg = par.get(l).getXdgs();
					for(int w=0; w < xdg.size(); w++ )
					{
						 list1 = xdg.get(w).getSetOfIcds();
						 vl.add(list1);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return vl;
	}
	
	/*public static double calcPrecision()
	{
		double precision = 0.0;
		Text train = null;
		Text chaos = null; 
		IcdList list1 = null; //list di ai_train
		IcdList list2 = null; //list di chaos
		Vector<IcdList> vl = new Vector<IcdList>();
		Vector<IcdList> vl2 = new Vector<IcdList>();
		double den = 0;
		try
		{
			File [] files = (new File(dir1)).listFiles(); //I docs vengono presi in ordine lessicografico, cambiare la lettura facendoli prendere in ordine????
			File[] files2 = (new File(dir2)).listFiles();
			for (int z=0; z < files.length*0.3; z++) { //70 è il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
				train =load_new(files[z]);
				Vector<Paragraph> par = train.getParagraphs();
				for(int l = 0; l < par.size(); l++)
				{
					Vector<XDG> xdg = par.get(l).getXdgs();
					for(int w=0; w < xdg.size(); w++ )
					{
						 list1 = xdg.get(w).getSetOfIcds();
						 vl.add(list1);
					}
				}
			}	
				for(int i = 0; i < files.length*0.3; i++ )
				{
					chaos = load_new(files[i]);
					Vector<Paragraph> parchaos = train.getParagraphs();
					for(int l = 0; l < parchaos.size(); l++)
					{
						Vector<XDG> xdgchaos = parchaos.get(l).getXdgs();
						for(int w=0; w < xdgchaos.size(); w++ )
						{
							 list2 = xdgchaos.get(w).getSetOfIcds();
							 vl2.add(list2);
							 den += list2.size();
							 System.out.println("den: "+den);
						}
					}
				}
				
				double num = intersection(vl,vl2);
				System.out.println("num: "+num);
				precision = num/den;
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return precision;
	}*/
	
	public static double calcPrecision2(Vector<IcdList> vl, Vector<IcdList> vl2)
	{
		double precision = 0.0;
		double num = 0;
		double den = 0;
		double precTot = 0.0;
		try
		{
			
				for(int i=0; i < vl.size(); i++)
				{
				num = intersection(vl,vl2,i);
				den = vl2.get(i).size();	
				if(num==0 || den==0)
					continue;
				System.out.println("num: "+num+" den "+den);
					precision += num/den;
					System.out.println("precision "+precision);
					
				}
				
				 precTot = precision/vl.size();
				 System.out.println("vlSize "+vl.size()+" precTot "+precTot);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return precTot;
	}
	
	public static double calcRecall2(Vector<IcdList> vl, Vector<IcdList> vl2)
	{
		double recall = 0.0;
		double recTot = 0.0;
		double den = 0.0;
		double num;
		for(int i=0; i < vl.size(); i++)
		{
		num = intersection(vl,vl2,i);
		den = vl.get(i).size();
		if(num==0 || den==0)
			continue;
		recall += num/den;
		}
		recTot = recall/vl.size();
		 System.out.println("vlSize "+vl.size()+" precTot "+recTot);
		 return recTot;
	}
	
	public static int intersection(Vector<IcdList> v, Vector<IcdList> v2)
	{
		int count = 0;
		for(int i=0; i < v.size(); i++)
		{
				count += (v.get(i).intersectionWith(v2.get(i))).size();
		}
		return count;
	}
	
	public static int intersection(Vector<IcdList> v, Vector<IcdList> v2, int i)
	{
		int count = (v.get(i).intersectionWith(v2.get(i))).size();
		return count;
	}
	
	public static double calcRecall()
	{
		double recall = 0.0;
		Text train = null;
		Text chaos = null; 
		IcdList list1 = null;
		IcdList list2 = null;
		Vector<IcdList> vl = new Vector<IcdList>();
		Vector<IcdList> vl2 = new Vector<IcdList>();
		double den = 0;
		try
		{
			File [] files = (new File(dir1)).listFiles(); //I docs vengono presi in ordine lessicografico, cambiare la lettura facendoli prendere in ordine????
			File[] files2 = (new File(dir2)).listFiles();
			for (int z=0; z < files.length*0.3; z++) { //70 è il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
				train =load_new(files[z]);
				Vector<Paragraph> par = train.getParagraphs();
				for(int l = 0; l < par.size(); l++)
				{
					Vector<XDG> xdg = par.get(l).getXdgs();
					for(int w=0; w < xdg.size(); w++ )
					{
						 list1 = xdg.get(w).getSetOfIcds();
						 vl.add(list1);
						 den += list1.size();
					}
				}
			}	
				for(int i = 0; i < files.length*0.3; i++ )
				{
					chaos = load_new(files[i]);
					Vector<Paragraph> parchaos = train.getParagraphs();
					for(int l = 0; l < parchaos.size(); l++)
					{
						Vector<XDG> xdgchaos = parchaos.get(l).getXdgs();
						for(int w=0; w < xdgchaos.size(); w++ )
						{
							 list2 = xdgchaos.get(w).getSetOfIcds();
							 vl2.add(list2);
							
						}
					}
				}
				
				double num = intersection(vl,vl2);
				recall = num/den;
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return recall;
	}
	
	/*public static double calcFMeasure()
	{
		double f_measure = (2*calcPrecision()*calcRecall())/(calcPrecision()+calcRecall());
		return f_measure;
	}*/
	
	public static void main(String[] args)
	{
		Vector<IcdList> vl = load(dir1);
		Vector<IcdList> vl2 = load(dir2);
		double a = calcPrecision2(vl,vl2);
		double b = calcRecall2(vl,vl2);
		
		//double b = calcRecall();
		//double c = calcFMeasure();
		System.out.println(a+" "+b/*+" "+c*/);
	}
}