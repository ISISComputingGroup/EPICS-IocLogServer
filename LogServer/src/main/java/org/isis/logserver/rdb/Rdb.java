/*
 * Copyright (C) 2013-2014 Research Councils UK (STFC)
 *
 * This file is part of the Instrument Control Project at ISIS.
 *
 * This code and information are provided "as is" without warranty of any 
 * kind, either expressed or implied, including but not limited to the
 * implied warranties of merchantability and/or fitness for a particular 
 * purpose.
 */
package org.isis.logserver.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.isis.logserver.server.Config;

public class Rdb
{
	private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	
	/** RDB connection */
	final private Connection connection;
		
	public static Rdb connectToDatabase(Config config) throws Exception
	{
        return new Rdb(config);
	}
	
	public Rdb(Config config) throws Exception
	{
		Class.forName(MYSQL_DRIVER).newInstance();
		
		String url = config.getSqlUrl();
		String userName = config.getSqlUser();
		String password = config.getSqlPassword();
		
        connection = DriverManager.getConnection(url, userName, password);
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
	    catch (SQLException ex)
	    {
	        System.out.println("Error closing RDB connection: " + ex.getMessage());
        }
	}
}
