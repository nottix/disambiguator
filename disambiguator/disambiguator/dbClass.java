package disambiguator;

import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.*;


public class dbClass {
	private static Connection connection;
	
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
		Vector texts = new Vector();
		//Hashtable<Integer,Vector<Paragraph>> partext = new Hashtable();
		
		String query ="";
		Statement st = null;
		Statement st2 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		System.out.println("PIPPO2");
		String query2 = "";
		String tempFrom, tempTo;
		try
		{
			String dir = "c:\\ChaosParser\\AI_train";
			File [] files = (new File(dir)).listFiles(); //I docs vengono presi in ordine lessicografico, cambiare la lettura facendoli prendere in ordine????
			for (int z=0; z < files.length*0.7; z++) { //70 � il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
				System.out.println("Caricamento file: "+files[z].getName());
				t =load_new(files[z]);
				texts = t.getParagraphs(); 

			    for(int i =0; i < texts.size(); i++)
			    {
			       PreparedStatement ps = connection.prepareStatement("INSERT into frase(idfrase, frase) VALUES (?,?)");
			       ps.setInt(1, ((Paragraph)texts.get(i)).getId());
			       ps.setString(2, ((Paragraph)texts.get(i)).getSurface());
			       ps.executeUpdate();
			       ResultSet rs = ps.getGeneratedKeys();
			       rs.next();
			       	       
			       Vector xdgs = ((Paragraph)texts.get(i)).getXdgs();
			       for(int x=0; x < xdgs.size(); x++) {
			    	   ConstituentList consList = (ConstituentList)((XDG)xdgs.get(x)).getSetOfConstituents();
			    	   for(int j=0; j < consList.size();j++)
				       {
				    	   //st = dbutil.startTransaction().createStatement();
			    		   query2 = "INSERT into costituente(idcostituente, surface, typec, mf, head, potgov, idfrase) VALUES (" +
			    		   		"'"+consList.getElementAt(j).getId()+"', " +
			    		   		"?, '"+consList.getElementAt(j).getType()+"', " +
			    		   		"'"+consList.getElementAt(j).getMorphologicalFeatures()+"', " +
			    		   		"'"+consList.getElementAt(j).getHead().getId()+"', " +
			    		   		"'"+consList.getElementAt(j).getGov().getId()+"', " +
			    		   		"'"+rs.getInt(1)+"')";
				    	   ps2 = connection.prepareStatement(query2);
				    	   //ps2.setInt(1, consList.getElementAt(j).getId());
				    	   ps2.setString(1, consList.getElementAt(j).getSurface());
				    	   /*ps2.setString(3, consList.getElementAt(j).getType());
				    	   ps2.setString(4, consList.getElementAt(j).getMorphologicalFeatures());
				    	   ps2.setInt(5, consList.getElementAt(j).getHead().getId());
				    	   ps2.setInt(6, consList.getElementAt(j).getGov().getId());
				    	   ps2.setInt(7, rs.getInt(1));*/
				    	   System.out.println(query2);
				    	   ps2.executeUpdate();
				    	   ps2.close();
				       }
			    	   IcdList icdList = ((XDG)xdgs.get(x)).getSetOfIcds();
			    	   for(int k=0; k < icdList.size(); k++) {
			    		   if(((Icd)icdList.get(k)).getFrom()!=null && ((Icd)icdList.get(k)).getTo()!=null) {
			    			   tempFrom = ((Icd)icdList.get(k)).getFrom().getSurface();
			    			   tempTo = ((Icd)icdList.get(k)).getTo().getSurface();
			    			   query = "INSERT into icd (fromc, toc, fromcs, tocs, fromct, toct, typeicd, idfrase) VALUES ('"+((Icd)icdList.get(k)).getFromId()+"'" +
			    			   ",'"+((Icd)icdList.get(k)).getToId()+
			    			   "', ?, ?, ?, ?, '"+((Icd)icdList.get(k)).getType()+"'" +
			    			   ", '"+rs.getInt(1)+"')";
			    			   ps3 = connection.prepareStatement(query);
			    			   ps3.setString(1, tempFrom);
			    			   ps3.setString(2, tempTo);
			    			   ps3.setString(3, ((Icd)icdList.get(k)).getFrom().getType());
			    			   ps3.setString(4, ((Icd)icdList.get(k)).getTo().getType());
			    			   System.out.println(query);
			    			   ps3.executeUpdate();
			    			   ps3.close();
			    		   }
			    	   }
			       }
			       rs.close();
			       ps.close();
			    }
			  
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void readData()
	{
		
	}
	

	public static void main(String[] args)
	{
		connection = DBUtil.startTransaction();
		addToDB();
		DBUtil.close();
	}
}
