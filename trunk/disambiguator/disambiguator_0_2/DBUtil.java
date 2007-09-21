package disambiguator_0_2;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.Properties;
import chaos.XDG.*;

public class DBUtil{
	
	private static Properties property = null;
	private static Connection c=null;
	
	private static Icd icd;
	private static String query;
	private static String fromConstType;
	private static String toConstType;
	private static PreparedStatement ps1;
	private static ResultSet rs1;
		
	public static Connection startTransaction()
	{
		try
		{
			File ff = new File(System.getProperty("user.dir")+"/conf/sql-ds.properties");
			
			FileInputStream f;
			f = new FileInputStream (ff);
			property = new Properties();
			property.load(f);
			f.close();
	
			Class.forName(property.getProperty("jdbcDriver"));
			c = DriverManager.getConnection(property.getProperty("connectionURL"),property.getProperty("username"),property.getProperty("password"));
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
				query = "SELECT COUNT(i.idfrase) FROM icd i WHERE ((i.fromcs = ?) OR" +
					" (i.tocs = ?)) AND " +
					"((i.fromcs = ?) OR (i.tocs = ?)) AND (((i.fromct = ?) AND (i.toct = ?)) OR ((i.fromct = ?) AND (i.toct = ?)))";
				//System.out.println(query);
				ps1 = c.prepareStatement(query);
				ps1.setString(1, icd.getFrom().getSurface());
				ps1.setString(2, icd.getFrom().getSurface());
				ps1.setString(3, icd.getTo().getSurface());
				ps1.setString(4, icd.getTo().getSurface());
				ps1.setString(5, icd.getFrom().getType());
				ps1.setString(6, icd.getTo().getType());
				ps1.setString(7, icd.getTo().getType());
				ps1.setString(8, icd.getFrom().getType());
				rs1 = ps1.executeQuery();
				if(rs1.next()) {
					queryResult.add(new Integer(rs1.getInt(1)));
					//System.out.println("Rs: "+rs.getInt(1));
				}
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
				query="SELECT COUNT(i.idfrase) FROM icd i WHERE (i.fromct=? AND i.toct=?) OR (i.toct=? AND i.fromct=?)";
				
				ps1 = c.prepareStatement(query);
				ps1.setString(1, fromConstType);
				ps1.setString(2, toConstType);
				ps1.setString(3, fromConstType);
				ps1.setString(4, toConstType);
				rs1 = ps1.executeQuery();
				if(rs1.next()) {
					System.out.println("Int: "+rs1.getInt(1));
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
				query = "SELECT MIN(ABS(i.fromct-i.toct)) FROM icd i WHERE i.toct = ? AND i.fromct = ?";
			
				ps1 = c.prepareStatement(query);
				ps1.setString(1, toConstType);
				ps1.setString(2, fromConstType);
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
}
