package disambiguator;

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
	
	private static String dir1 = System.getProperty("user.dir")+"/AI_train";
	private static String dir2 = System.getProperty("user.dir")+"/chaos";

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
	
	public static double calcPrecision()
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
			for (int z=0; z < files.length; z++) { //70 � il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
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
				for(int i = 0; i < files.length; i++ )
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
			for (int z=0; z < files.length; z++) { //70 � il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
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
				for(int i = 0; i < files.length; i++ )
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
	
	public static double calcFMeasure()
	{
		double f_measure = (2*calcPrecision()*calcRecall())/(calcPrecision()+calcRecall());
		return f_measure;
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
	
	public static void main(String[] args)
	{
		double a = calcPrecision();
		double b = calcRecall();
		double c = calcFMeasure();
		System.out.println(a+" "+b+" "+c);
	}
}