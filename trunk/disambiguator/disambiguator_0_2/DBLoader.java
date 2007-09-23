package disambiguator_0_2;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.*;


public class DBLoader {
	private static double perNum;
	
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
	
	public static int addToDB()
	{
		int ret = 0;
		Text t = null;
		Vector paragraphs;
		Vector xdgs;
		try
		{
			File [] files = (new File(DBUtil.getCorpusTrainDir())).listFiles(); //I docs vengono presi in ordine lessicografico
			for (int z=0; z < files.length*perNum; z++) {
				System.out.println("Caricamento file: "+files[z].getName());
				t =load_new(files[z]);
				paragraphs = t.getParagraphs(); 

			    for(int i =0; i < paragraphs.size(); i++)
			    {
			       xdgs = ((Paragraph)paragraphs.get(i)).getXdgs();
			       for(int x=0; x < xdgs.size(); x++)
			    	   DBUtil.queryAddToDB(((XDG)xdgs.get(x)).getSetOfIcds());
			    }
			  
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}	

	public static void main(String[] args)
	{
		DBUtil.startTransaction();
		perNum = DBUtil.getPercentualeTrain();
		addToDB();
		DBUtil.close();
	}
}
