/**
 * Progetto di Intelligenza Artificiale 2006/2007
 * 
 * Disambiguatore sintattico:
 * Utilizza algoritmi di disambiguazione stocastici e randomici
 * 
 * @author Simone Notargiacomo, Lorenzo Tavernese
 */

package disambiguator_0_3;

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
	
	public static Connection startTransaction() {
		try {
			loadProperty();
			//Class.forName(property.getProperty("jdbcDriver"));
			//DriverManager.registerDriver(new com.mysql.embedded.jdbc.MySqlEmbeddedDriver());
			String url = "jdbc:mysql-embedded/TestDatabase";
			Properties props = new Properties();
			props.put("library.path", "C://Documents and Settings//SeT//Desktop//mysql-je-1.30-windows//lib");
			props.put("--datadir", "c://chaosParser//");
	        props.put("--basedir", "C://Documents and Settings//SeT//Desktop//mysql-je-1.30-windows//");
	        props.put("--default-character-set","utf8");
	        props.put("--default-collation","utf8_general_ci");
	        c = DriverManager.getConnection(url,props);
	        
			//c = DriverManager.getConnection(property.getProperty("connectionURL"),property.getProperty("username"),property.getProperty("password"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public static void loadProperty() {
		try {
			File ff = new File(System.getProperty("user.dir")+"/conf/configuration.properties");
			
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
				else
					queryResult.add(new Integer(0));
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
					System.out.println("FromType: "+fromConstType+", ToType: "+toConstType+", Int: "+rs1.getInt(1));
					queryResult.add(new Integer(rs1.getInt(1)));
				}
				else
					queryResult.add(new Integer(0));
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
				e.printStackTrace();
		}
	}
	
	public static void queryDepProb(IcdList data, ArrayList queryResult) {
		try{
			double num=0, den=0;
			for(int i=0; i<data.size();i++){
				fromConstSur=data.getIcd(i).getFrom().getSurface();
				toConstSur=data.getIcd(i).getTo().getSurface();
				fromConstType=data.getIcd(i).getFrom().getType();
				toConstType=data.getIcd(i).getTo().getType();
				query = "SELECT COUNT(*) FROM icd i WHERE (i.fromcs = ? AND i.tocs = ?) AND " +
						"(i.fromct = ? AND i.toct = ?)";
				ps1 = c.prepareStatement(query);
				ps1.setString(1, fromConstSur);
				ps1.setString(2, toConstSur);
				ps1.setString(3, fromConstType);
				ps1.setString(4, toConstType);
				rs1 = ps1.executeQuery();

				if(rs1.next()) {
					//queryResult.add(new Integer(rs1.getInt(1)));
					num = rs1.getInt(1);
				}
				rs1.close();
				ps1.close();
				
				query = "SELECT COUNT(*) FROM icd i WHERE (i.fromcs = ? AND i.tocs = ?) OR " +
						"(i.fromcs = ? AND i.tocs = ?)";
				ps1 = c.prepareStatement(query);
				ps1.setString(1, fromConstSur);
				ps1.setString(2, toConstSur);
				ps1.setString(3, toConstSur);
				ps1.setString(4, fromConstSur);
				rs1 = ps1.executeQuery();
				
				if(rs1.next()) {
					//queryResult.add(new Integer(rs1.getInt(1)));
					den = rs1.getInt(1);
				}
				rs1.close();
				ps1.close();
				
				if(den!=0)
				{
					queryResult.add(new Integer( (int)((num/den)*100) ));
					System.out.println("AAAAAAAAAAAAAAAAAAAAAAA: "+(int)((num/den)*100)+", num: "+num+", den: "+den);
				}
				else
					queryResult.add(new Integer(0));
				
			}
		}
		catch(Exception e){
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
				else
					queryResult.add(new Integer(0));
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void queryAddToDB(IcdList data) {
		try {
			for(int k=0; k < data.size(); k++) {
				if( data.getIcd(k).getFrom()!=null && data.getIcd(k).getTo()!=null ) {
				   query = "INSERT into icd (fromc, toc, fromcs, tocs, fromct, toct) VALUES ('"+data.getIcd(k).getFromId()+"'" +
				   ",'"+data.getIcd(k).getToId()+
				   "', ?, ?, ?, ?)";
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
}
