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
package org.isis.logserver.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages configuration options for the log server including settings for
 * 	connecting to the JMS server and database server. Loads the settings 
 * 	from file and saves a default config file if the file doesn't already
 * 	exist.
 *
 */
public class Config 
{
	private static final String KV_DELIMIT = ":";

	// Default values for each configuration setting
	private static final String DEFAULT_jmsServerAddress = "tcp://localhost";
	private static final String DEFAULT_jmsServerPort = "61616";
	private static final String DEFAULT_jmsTopic = "iocLogs";
	private static final String DEFAULT_sqlServerAddress = "jdbc:mysql://localhost";
	private static final String DEFAULT_sqlServerPort = "3306";
	private static final String DEFAULT_sqlUser = "msg_log";
	private static final String DEFAULT_sqlPassword = "$msg_log";
	private static final String DEFAULT_sqlSchema = "msg_log";
	private static final Integer[] DEFAULT_listenPorts = { 7004, 7011 };
	
	// The key for each configuration setting for file read/write
	private static final String KEY_jmsServerAddress = "jms_server_address";
	private static final String KEY_jmsServerPort = "jms_server_port";
	private static final String KEY_jmsTopic = "jms_topic";
	private static final String KEY_sqlServerAddress = "sql_server_address";
	private static final String KEY_sqlServerPort = "sql_server_port";
	private static final String KEY_sqlUser = "sql_user";
	private static final String KEY_sqlPassword = "sql_password";
	private static final String KEY_sqlSchema = "sql_schema";
	private static final String KEY_listenPorts = "listen_ports";
	
	private String jmsServerAddress;
	private String jmsServerPort;
	private String jmsTopic;
	private String sqlServerAddress;
	private String sqlServerPort;
	private String sqlUser;
	private String sqlPassword;
	private String sqlSchema;
	private Integer[] listenPorts;
	
	public Config() 
	{
		setDefaults();
	}
	
	/**
	 * Set the default values for each option
	 */
	public void setDefaults()
	{
		jmsServerAddress = DEFAULT_jmsServerAddress;
		jmsServerPort = DEFAULT_jmsServerPort;
		jmsTopic = DEFAULT_jmsTopic;
		sqlPassword = DEFAULT_sqlPassword;
		sqlServerAddress = DEFAULT_sqlServerAddress;
		sqlServerPort = DEFAULT_sqlServerPort;
		sqlUser = DEFAULT_sqlUser;
		sqlSchema = DEFAULT_sqlSchema;
		listenPorts = DEFAULT_listenPorts;
	}
	
	/**
	 * Load configuration details from the specified file. Default values are used
	 * for any options that are missing. If the file with the specified name does 
	 * not already exist, a new one will  be created at that location with the 
	 * default values of each option.
	 */
	public void loadConfigFromFile(String filename)
	{
		File f = new File(filename);
		
		if(!f.exists()) 
		{ 
			System.out.println("The specified file: '" + filename + "' does not exist. Creating default configuration file.");
			saveConfigToFile(filename);
			return;
		}
		
		try
		{
		    // Open the file
		    FileInputStream fstream = new FileInputStream(filename);
		     
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		         
		    String strLine;
		     
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)
		    {
		        strLine = strLine.trim();
		         
		        //skip empty lines:
		        if (strLine.length() <= 0) {
		            continue;
		        }
		         
		    	//skip comments:
		    	if(strLine.startsWith("#")) {
		    	    continue;
		    	}
		    	 
		    	processInputLine(strLine);
		    }
		    
		    System.out.println("Configuration loaded from file: '" + filename + "'");
		     
		    //Close the input stream
		    in.close();
		}
		catch (Exception ex)
		{
			System.out.println("Error reading config file; using default configuration");
			setDefaults();
		}
	}
	
	/**
	 * Process a single line of input which should be of the
	 * form 'key: value'.
	 */
	private void processInputLine(String line)
	{
		int delim = line.indexOf(KV_DELIMIT);
		
		if(delim == -1 || delim >= line.length())
		{
			System.out.println("Error processing config file line: '" + line + "'");
			return;
		}
		
		String key = line.substring(0, delim).trim();
		String value = line.substring(delim+1, line.length()).trim();

		switch(key)
		{
			case KEY_jmsServerAddress:
				jmsServerAddress = value;
				break;
				
			case KEY_jmsServerPort:
				jmsServerPort = value;
				break;
				
			case KEY_jmsTopic:
				jmsTopic = value;
				break;
				
			case KEY_sqlPassword:
				sqlPassword = value;
				break;
				
			case KEY_sqlServerAddress:
				sqlServerAddress = value;
				break;
				
			case KEY_sqlServerPort:
				sqlServerPort = value;
				break;
				
			case KEY_sqlUser:
				sqlUser = value;
				break;
				
			case KEY_sqlSchema:
				sqlSchema = value;
				break;
				
			case KEY_listenPorts:
				String[] ports = value.split(";");
				List<Integer> portList = new ArrayList<Integer>();
				for(String port: ports) {
					try
					{
						int portInt = Integer.parseInt(port);
						if(portInt > 0 && portInt < 65536) {
							portList.add(portInt);
						}
					}
					catch(Exception ex) {}
				}
				
				listenPorts = portList.toArray(new Integer[portList.size()]);
					
				break;
				
			default:
				System.out.println("Unrecognised config key: '" + key + "'");
		}
		
	}
	
	/**
	 * Saves the current configuration to the specified file.
	 */
	public void saveConfigToFile(String filename)
	{
		PrintWriter out = null;
		try 
		{
			out = new PrintWriter(filename);
			out.println(configToText());
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(out != null) {
				out.close();
			}
		}

	}
	
	/**
	 * Produces a text representation of the configuration so
	 * that it may be printed to file.
	 */
	public String configToText()
	{
		StringBuilder config = new StringBuilder();	
		
		String lineSep = System.lineSeparator();
		
		config.append(KEY_jmsServerAddress 	+ KV_DELIMIT + " " + jmsServerAddress);
		config.append(lineSep);
		config.append(KEY_jmsServerPort 	+ KV_DELIMIT + " " + jmsServerPort);
		config.append(lineSep);
		config.append(KEY_jmsTopic 			+ KV_DELIMIT + " " + jmsTopic);
		config.append(lineSep);
		config.append(lineSep);
		config.append(KEY_sqlServerAddress 	+ KV_DELIMIT + " " + sqlServerAddress);
		config.append(lineSep);
		config.append(KEY_sqlUser 			+ KV_DELIMIT + " " + sqlUser);
		config.append(lineSep);
		config.append(KEY_sqlPassword 		+ KV_DELIMIT + " " + sqlPassword);
		config.append(lineSep);
		config.append(KEY_sqlSchema 		+ KV_DELIMIT + " " + sqlSchema);
		config.append(lineSep);
		config.append(lineSep);
		
		config.append(KEY_listenPorts 		+ KV_DELIMIT + " ");
		if(listenPorts.length > 0) 
		{
			for(int p=0; p<listenPorts.length; ++p) 
			{
				config.append(listenPorts[p] + ";");
			}
		}
		
		return config.toString();
	}
	
	public String getJmsUrl() {
		return jmsServerAddress + ":" + jmsServerPort;
	}
	
	public String getSqlUrl() {
		return sqlServerAddress + ":" + sqlServerPort + "/" + sqlSchema;
	}


	public String getJmsServerAddress() {
		return jmsServerAddress;
	}

	public String getJmsServerPort() {
		return jmsServerPort;
	}

	public String getJmsTopic() {
		return jmsTopic;
	}

	public String getSqlServerAddress() {
		return sqlServerAddress;
	}

	public String getSqlServerPort() {
		return sqlServerPort;
	}

	public String getSqlUser() {
		return sqlUser;
	}

	public String getSqlPassword() {
		return sqlPassword;
	}
	
	public String getSqlSchema() {
		return sqlSchema;
	}

	public Integer[] getListenPorts() {
		return listenPorts;
	}
}
