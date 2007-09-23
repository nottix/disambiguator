package disambiguator_0_2;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.Properties;
import chaos.XDG.*;

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
		return corpusTrainDir;
	}
	
	public static String getCorpusChaosDir() {
		return corpusTrainDir;
	}
	
	public static String getCorpusOutputDir() {
		return corpusTrainDir;
	}
	
	public static double getPercentualeTrain() {
		return percentualeTrain;
	}
	
	public static Connection startTransaction()
	{
		try
		{
			File ff = new File(System.getProperty("user.dir")+"/conf/configuration.properties");
			
			FileInputStream f;
			f = new FileInputStream (ff);
			property = new Properties();
			property.load(f);
			f.close();
	
			Class.forName(property.getProperty("jdbcDriver"));
			c = DriverManager.getConnection(property.getProperty("connectionURL"),property.getProperty("username"),property.getProperty("password"));
			
			corpusTrainDir = property.getProperty("corpusTrainDir");
			corpusChaosDir = property.getProperty("corpusChaosDir");
			corpusOutputDir = property.getProperty("corpusOutputDir");
			percentualeTrain = Double.valueOf(property.getProperty("percentualeTrain")).doubleValue();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return c;
	}
	
	public static void close() 
	{
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
//				query = "SELECT COUNT(*) FROM icd i WHERE ((i.fromcs = ?) OR" +
//				" (i.tocs = ?)) AND " +
//				"((i.fromcs = ?) OR (i.tocs = ?)) AND (((i.fromct = ?) AND (i.toct = ?)) OR ((i.fromct = ?) AND (i.toct = ?)))";
				ps1 = c.prepareStatement(query);
				ps1.setString(1, icd.getFrom().getSurface());
				ps1.setString(2, icd.getTo().getSurface());
				ps1.setString(3, icd.getFrom().getType());
				ps1.setString(4, icd.getTo().getType());
//				ps1.setString(5, icd.getFrom().getType());
//				ps1.setString(6, icd.getTo().getType());
//				ps1.setString(7, icd.getTo().getType());
//				ps1.setString(8, icd.getFrom().getType());
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
//				query="SELECT COUNT(*) FROM icd i WHERE (i.fromct=? AND i.toct=?)";
				
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
				rs1.close();
				ps1.close();
			}
		}
		catch(Exception e) {
				e.printStackTrace();
		}
	}
	
	public static void queryFrequentRelDis(IcdList data, ArrayList queryResult) {
		try{
			for(int i=0; i<data.size();i++){
				fromConstType=data.getIcd(i).getFrom().getType();
				toConstType=data.getIcd(i).getTo().getType();
				query = "SELECT MIN(ABS(i.fromc-i.toc)) FROM icd i WHERE (i.toct = ? AND i.fromct = ?) OR " +
						"(i.toct = ? AND i.fromct = ?)";
//				query = "SELECT MIN(ABS(i.fromc-i.toc)) FROM icd i WHERE (i.toct = ? AND i.fromct = ?)";
//				query = "SELECT COUNT(*) FROM icd i WHERE (i.toct = ? AND i.fromct = ?) OR " +
//				"(i.toct = ? AND i.fromct = ?)";
			
				ps1 = c.prepareStatement(query);
				ps1.setString(1, toConstType);
				ps1.setString(2, fromConstType);
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
				
//				query = "SELECT COUNT(*) FROM icd i WHERE " +
//						"(i.fromcs = ? OR i.tocs = ?) AND " +
//						"((i.fromct = ? AND i.toct = ?) OR (i.fromct = ? AND i.toct = ?))";
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
//				ps1.setString(1, fromConstSur);
//				ps1.setString(2, fromConstSur);
//				ps1.setString(3, fromConstType);
//				ps1.setString(4, toConstType);
//				ps1.setString(5, toConstType);
//				ps1.setString(6, fromConstType);
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
