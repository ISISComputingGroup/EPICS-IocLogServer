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

import java.util.Date;
import java.util.List;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.rdb.RdbHandler;


/*	TODO List
 * 
 * - Add message suppression for the case of repeated messages.
 * - Move configuration of ports, jms server address, sql server details
 * 		to external configuration file.
 * - Develop standardised message format for use by IOCs
 * 
 * 
 */


/** IOCLogServer
 * 
 * A server that listens for messages (from IOCs) on a number of specified ports, processes 
 * 	them, and then dispatches them through JMS to listening clients and saves them to a SQL
 * 	database. The connections to the JMS server and the SQL database are independently 
 * 	buffered so that if the connection to either is temporarily lost, new incoming messages
 * 	will be stored up and only transmitted when the connection is reestablished.
 * 
 */
public class IOCLogServer
{
    final private static String SUPPRESSIONS =  "suppression.txt";
    
    private static final String CONFIG_FILE =  "logserver_config.ini";

    /** Listen for incoming connections and handle them */
    public static void main(String[] args)
    {       
        // Welcome message
        System.out.println( "Starting IOC Log Server - " + new Date());
        
        // Load configuration
        Config config = new Config();
        config.loadConfigFromFile(CONFIG_FILE);
        
        // Manages connection to JMS including message dispatch - runs in separate thread
        JmsHandler jmsHandler = null;
		try
		{
			jmsHandler = new JmsHandler(config);
			Thread jmsThread = new Thread(jmsHandler, "JMS Handler Thread");
			jmsThread.start();
		}
		catch(Exception ex)
		{
			System.out.println("Error connecting to JMS: " + ex);
		}
		
		// Manage connection top SQL database.
		RdbHandler rdbHandler = null;
		try
		{
			rdbHandler = new RdbHandler(config);
			
            Thread rdbThread = new Thread(rdbHandler, "SQL Database Handler Thread");
            rdbThread.start();
		}
		catch(Exception ex)
		{
			System.out.println("Error connecting to database: " + ex);
		}
        
		
		// Create message matcher that filters out messages that match a given pattern
		MessageMatcher matcher = null;
		try 
		{
			matcher = new MessageMatcher(SUPPRESSIONS);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Start a port listener in a new thread for each port
		List<Integer> ports = config.getListenPorts();
		for(int i=0; i<ports.size(); ++i)
		{
			PortListener portListener = new PortListener(ports.get(i), matcher, jmsHandler, rdbHandler);
			portListener.start();
		}
		
		// exit main() thread - JMS thread, SQL thread, and PortListener threads will keep going
    }
}
