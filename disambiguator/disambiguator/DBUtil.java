/**
 * Progetto di Intelligenza Artificiale 2006/2007
 * 
 * Disambiguatore sintattico:
 * Utilizza algoritmi di disambiguazione stocastici e randomici
 * 
 * @author Simone Notargiacomo, Lorenzo Tavernese
 */

package disambiguator;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.Properties;
import chaos.XDG.*;

/**
 * Classe statica che consente la gestione delle query al DB.
 * 
 * @version 0.2
 * @author Simone Notargiacomo, Lorenzo Tavernese
 */
public class DBUtil{
	
	private static Properties property = null;
	private static String corpusTrainDir;
	private static String corpusChaosDir;
	private static String corpusOutputDir;
	private static double percentualeTrain;
	private static Connection c=null;
	
	private static Icd icd;
	private static String query;
	private static String fromConstType;
	private static String toConstType;
	private static String fromConstSur;
	private static String toConstSur;
	private static PreparedStatement ps1;
	private static ResultSet rs1;
		
	public static String getCorpusTrainDir() {
		if(property==null)
			loadProperty();
		return corpusTrainDir;
	}
	
	public static String getCorpusChaosDir() {
		if(property==null)
			loadProperty();
		return corpusChaosDir;
	}
	
	public static String getCorpusOutputDir() {
		if(property==null)
			loadProperty();
		return corpusOutputDir;
	}
	
	public static double getPercentualeTrain() {
		if(property==null)
			loadProperty();
		return percentualeTrain;
	}
	
	public static Connection startTransaction(String db) {
		try {
			loadProperty();
			Class.forName(property.getProperty("jdbcDriver"));
			if(db!=null) {
				c = DriverManager.getConnection(property.getProperty("connectionURL")+db,property.getProperty("username"),property.getProperty("password"));
			}
			else {
				c = DriverManager.getConnection(property.getProperty("connectionURL"),property.getProperty("username"),property.getProperty("password"));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public static void loadProperty() {
		try {
			File ff = new File(System.getProperty("user.dir")+"//conf//configuration.properties");
			
			FileInputStream f;
			f = new FileInputStream (ff);
			property = new Properties();
			property.load(f);
			f.close();
			
			corpusTrainDir = property.getProperty("corpusTrainDir");
			corpusChaosDir = property.getProperty("corpusChaosDir");
			corpusOutputDir = property.getProperty("corpusOutputDir");
			percentualeTrain = Double.valueOf(property.getProperty("percentualeTrain")).doubleValue();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void close() {
		if(c!= null)
		{
			try
			{
				c.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void queryFrequentSurType(IcdList data, ArrayList queryResult) {
		try {
			for(int i=0; i<data.size(); i++) {
				icd = data.getIcd(i);
				query = "SELECT COUNT(*) FROM icd i WHERE (i.fromcs = ?) AND" +
					" (i.tocs = ?)" +
					" AND (i.fromct = ?) AND (i.toct = ?)";
				ps1 = c.prepareStatement(query);
				ps1.setString(1, icd.getFrom().getSurface());
				ps1.setString(2, icd.getTo().getSurface());
				ps1.setString(3, icd.getFrom().getType());
				ps1.setString(4, icd.getTo().getType());
				rs1 = ps1.executeQuery();
				if(rs1.next())
					queryResult.add(new Integer(rs1.getInt(1)));
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void queryFrequentRel(IcdList data, ArrayList queryResult) {
		try {
			for(int i=0; i<data.size(); i++) {
				fromConstType=data.getIcd(i).getFrom().getType();
				toConstType=data.getIcd(i).getTo().getType();
				query="SELECT COUNT(*) FROM icd i WHERE (i.fromct=? AND i.toct=?) OR (i.toct=? AND i.fromct=?)";
				ps1 = c.prepareStatement(query);
				ps1.setString(1, fromConstType);
				ps1.setString(2, toConstType);
				ps1.setString(3, fromConstType);
				ps1.setString(4, toConstType);
				rs1 = ps1.executeQuery();
				if(rs1.next()) {
					queryResult.add(new Integer(rs1.getInt(1)));
				}
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
				e.printStackTrace();
		}
	}
	
	public static void queryFrequentSurRel(IcdList data, ArrayList queryResult) {
		try {
			for(int i=0; i<data.size(); i++) {
				fromConstSur = data.getIcd(i).getFrom().getSurface(); //costituente from
				fromConstType = data.getIcd(i).getFrom().getType();
				toConstSur = data.getIcd(i).getTo().getSurface();
				toConstType = data.getIcd(i).getTo().getType();
				query = "SELECT COUNT(*) FROM icd i WHERE " +
					"((i.fromcs = ?) AND (i.fromct = ? AND i.toct = ?)) OR " +
					"((i.tocs = ?) AND (i.fromct = ? AND i.toct = ?))";
				
				ps1 = c.prepareStatement(query);
				ps1.setString(1, fromConstSur);
				ps1.setString(2, fromConstType);
				ps1.setString(3, toConstType);
				ps1.setString(4, toConstSur);
				ps1.setString(5, fromConstType);
				ps1.setString(6, toConstType);

				ResultSet rs1 = ps1.executeQuery();
				if(rs1.next())
					queryResult.add(new Integer(rs1.getInt(1)));
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Aggiunge nel DB i valori necessari per la disambiguazione.
	 * 
	 * @param data Lista di ICD da aggiungere al DB
	 */
	public static void queryAddToDB(IcdList data) {
		try {
			for(int k=0; k < data.size(); k++) {
				if( data.getIcd(k).getFrom()!=null && data.getIcd(k).getTo()!=null ) {
				   query = "INSERT into icd (fromc, toc, fromcs, tocs, fromct, toct) VALUES ('"+data.getIcd(k).getFromId()+"'" +
				   ",'"+data.getIcd(k).getToId()+
				   "', ?, ?, ?, ?)";
				   System.out.println(query);
				   ps1 = c.prepareStatement(query);
				   ps1.setString(1, data.getIcd(k).getFrom().getSurface());
				   ps1.setString(2, data.getIcd(k).getTo().getSurface());
				   ps1.setString(3, data.getIcd(k).getFrom().getType());
				   ps1.setString(4, data.getIcd(k).getTo().getType());
				   ps1.execute();
				   ps1.close();
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
    private static Vector readText() {
        Vector comandi = null;
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(new File("sql//chaos.sql")));
			comandi = new Vector();
			String letto = null;
			boolean avanti = true;
			
			while(avanti)
			{
				int ch;
			    letto="";
			    while( (ch=buffer.read()) != ';' && ch!=-1 ) {  
			    	letto += (char)ch;
			    }
			    if(ch==-1)
			        break;
			    letto += ';';
			    System.out.println(letto);
			    if(letto.equals("\n") || letto==null)
			    	avanti = false;
			    else {
			    	comandi.add(letto);
			    }
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return comandi;
    }
	
	public static void queryCreateDB() {
		try {
//	        ps1.execute("CREATE DATABASE chaos");
//	        ps1.execute("USE chaos");
//	        ps1.execute("CREATE TABLE icd");
//	        ps1.close();
			
		    InputStreamReader isr = new InputStreamReader( System.in );
		    BufferedReader stdin = new BufferedReader( isr );
		    System.out.print( "Sei sicuro di voler creare il DB? [si/no]: " );
		    String input = stdin.readLine();
		    if(input.compareToIgnoreCase("si")==0) {
		    	Statement st = c.createStatement();
		    	Vector vet = readText();
		    	for(int i=0; i<vet.size(); i++) {
		    		st.addBatch((String)vet.get(i));
		    	}
		    	st.executeBatch();
		    	st.close();
		    }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Questo metodo rimuove chaos dal DB.
	 * Dopo aver chiamato tale funzione bisogna ripopolare il DB.
	 */
	public static void queryFreeDB() {
		try {
		    InputStreamReader isr = new InputStreamReader( System.in );
		    BufferedReader stdin = new BufferedReader( isr );
		    System.out.print( "Sei sicuro di voler eliminare chaos dal DB? [si/no]: " );
		    String input = stdin.readLine();
		    if(input.compareToIgnoreCase("si")==0) {
		    	query = "DROP DATABASE IF EXISTS chaos";
		    	ps1 = c.prepareStatement(query);
		    	ps1.execute();
		    }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Questo metodo rimuove i dati nella tabella ICD presente nel DB.
	 * Dopo aver chiamato tale funzione bisogna ripopolare il DB.
	 */
	public static void queryFreeTable() {
		try {
		    InputStreamReader isr = new InputStreamReader( System.in );
		    BufferedReader stdin = new BufferedReader( isr );
		    System.out.print( "Sei sicuro di voler eliminare i dati nella tabella? [si/no]: " );
		    String input = stdin.readLine();
		    if(input.compareToIgnoreCase("si")==0) {
		    	query = "DELETE FROM icd";
		    	ps1 = c.prepareStatement(query);
		    	ps1.execute();
		    }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
