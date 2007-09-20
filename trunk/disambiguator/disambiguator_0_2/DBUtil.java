package disambiguator_0_2;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBUtil{
	
	private static Properties property = null;
	private static Connection c=null;
		
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
}
