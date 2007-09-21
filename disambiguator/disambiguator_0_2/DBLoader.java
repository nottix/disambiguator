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
	private static Connection connection;
	private static String corpusDir = System.getenv("CHAOS_HOME")+"//AI_train";
	private static double perNum = 0.70;
	
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
		IcdList icdList;
		
		String query ="";
		Statement st = null;
		Statement st2 = null;
		PreparedStatement ps1;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		System.out.println("PIPPO2");
		String query2 = "";
		String fromSurface, toSurface;
		try
		{
			File [] files = (new File(corpusDir)).listFiles(); //I docs vengono presi in ordine lessicografico
			for (int z=0; z < files.length*perNum; z++) { //70 e' il 70% dei file contenuti nella directory AI_train che verranno utilizzati per il train
				System.out.println("Caricamento file: "+files[z].getName());
				t =load_new(files[z]);
				paragraphs = t.getParagraphs(); 

			    for(int i =0; i < paragraphs.size(); i++)
			    {
			       /*ps1 = connection.prepareStatement("INSERT into frase(idfrase, frase) VALUES (?,?)");
			       ps.setInt(1, ((Paragraph)texts.get(i)).getId());
			       ps.setString(2, ((Paragraph)texts.get(i)).getSurface());
			       ps.executeUpdate();
			       ResultSet rs = ps.getGeneratedKeys();
			       rs.next();*/
			       	       
			       xdgs = ((Paragraph)paragraphs.get(i)).getXdgs();
			       for(int x=0; x < xdgs.size(); x++) {
			    	 //  ConstituentList consList = (ConstituentList)((XDG)xdgs.get(x)).getSetOfConstituents();
			    	//   for(int j=0; j < consList.size();j++)
				    //   {
				    	   //st = dbutil.startTransaction().createStatement();
			    	//	   query2 = "INSERT into costituente(idcostituente, surface, typec, mf, head, potgov, idfrase) VALUES (" +
			    	//	   		"'"+consList.getElementAt(j).getId()+"', " +
			    	//	   		"?, '"+consList.getElementAt(j).getType()+"', " +
			    	//	   		"'"+consList.getElementAt(j).getMorphologicalFeatures()+"', " +
			    	//	   		"'"+consList.getElementAt(j).getHead().getId()+"', " +
			    	//	   		"'"+consList.getElementAt(j).getGov().getId()+"', " +
			    	//	   		"'"+rs.getInt(1)+"')";
				    //	   ps2 = connection.prepareStatement(query2);
				    	   //ps2.setInt(1, consList.getElementAt(j).getId());
				    //	   ps2.setString(1, consList.getElementAt(j).getSurface());
				    	   /*ps2.setString(3, consList.getElementAt(j).getType());
				    	   ps2.setString(4, consList.getElementAt(j).getMorphologicalFeatures());
				    	   ps2.setInt(5, consList.getElementAt(j).getHead().getId());
				    	   ps2.setInt(6, consList.getElementAt(j).getGov().getId());
				    	   ps2.setInt(7, rs.getInt(1));*/
				    //	   System.out.println(query2);
				    //	   ps2.executeUpdate();
				    //	   ps2.close();
				    //   }
			    	   icdList = ((XDG)xdgs.get(x)).getSetOfIcds();
			    	   for(int k=0; k < icdList.size(); k++) {
			    		   if( icdList.getIcd(k).getFrom()!=null && icdList.getIcd(k).getTo()!=null ) {
			    			   fromSurface = icdList.getIcd(k).getFrom().getSurface();
			    			   toSurface = icdList.getIcd(k).getTo().getSurface();
			    			   query = "INSERT into icd (fromc, toc, fromcs, tocs, fromct, toct) VALUES ('"+icdList.getIcd(k).getFromId()+"'" +
			    			   ",'"+icdList.getIcd(k).getToId()+
			    			   "', ?, ?, ?, ?)";
			    			   ps1 = connection.prepareStatement(query);
			    			   ps1.setString(1, fromSurface);
			    			   ps1.setString(2, toSurface);
			    			   ps1.setString(3, icdList.getIcd(k).getFrom().getType());
			    			   ps1.setString(4, icdList.getIcd(k).getTo().getType());
			    			   System.out.println(query);
			    			   ps1.execute();
			    			   ps1.close();
			    		   }
			    	   }
			       }
			       //rs.close();
			       //ps.close();
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
		connection = DBUtil.startTransaction();
		addToDB();
		DBUtil.close();
	}
}
