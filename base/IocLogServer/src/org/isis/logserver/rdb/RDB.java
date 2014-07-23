package org.isis.logserver.rdb;

import java.sql.Connection;
import java.sql.DriverManager;

public class Rdb
{
	private static String SCHEMA = "MSG_LOG.";
	private static String URL = "jdbc:mysql://localhost:3306/msg_log";
	private static String USER_NAME = "admin";
	private static String PASSWORD = "admin";
	
	/** RDB connection */
	final private Connection connection;
		
	public static Rdb connectToDatabase() throws Exception
	{
        return new Rdb();
	}
	
	public Rdb() throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
	}

    /** @return JDBC connection */
	public Connection getConnection()
	{
	    return connection;
	}

	/** Must be called when RDB no longer used to release resources */
	public void close()
	{
	    try
	    {
	        connection.close();
	    }
	    catch (Exception e)
	    {
	        // Ignore, we're closing anyway
        }
	}
}
