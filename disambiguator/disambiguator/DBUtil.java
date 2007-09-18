package dis;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBUtil{
	
	private Properties property = null;
	Connection c=null;
	
	public DBUtil() throws IOException, ClassNotFoundException, Exception{
		
		try{
			
		
		File ff = new File(System.getProperty("user.dir")+"/driver/conf/sql-ds.properties");
		
		FileInputStream f;
		f = new FileInputStream (ff);

		property = new Properties();
		property.load(f);
		f.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	
	}	
	
	public Connection startTransaction()
	{
		try
		{
		
		Class.forName(property.getProperty("jdbcDriver"));
		c = DriverManager.getConnection(property.getProperty("connectionURL"),property.getProperty("username"),property.getProperty("password"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return c;
	}
	
	public void close() 
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
