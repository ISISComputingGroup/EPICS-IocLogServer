package org.isis.logserver.server;

import java.util.Date;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.rdb.RdbHandler;

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
    /** Default port number */
    final private static int IN_PORTS[] = { 7004, 7011 }; // krw - 7004 is caput; 7011 is IOC log
    final private static String JMS_URL = "tcp://localhost:61616";

    final private static String SUPPRESSIONS =  "suppression/suppression.txt";

    /** Listen for incoming connections and handle them */
    public static void main(String[] args)
    {
        // Parse command line arguments
        String suppressions = SUPPRESSIONS;
        if (args.length>1)
        {
        	for (int i=1; i<args.length; i++)
        	{
        		if(args[i-1].equals("-port"))
        		{
        			//port = Integer.parseInt(args[i]);
        		}
        		else if(args[i-1].equals("-suppressions"))
        		{
        			suppressions = args[i];
        		}
        	}
        }
        
        // Welcome message
        System.out.println( "Starting IOC Log Server - " + new Date());
        
        // Manages connection to JMS including message dispatch - runs in separate thread
        JmsHandler jmsHandler = null;
		try
		{
			jmsHandler = new JmsHandler(JMS_URL);
			Thread jmsThread = new Thread(jmsHandler, "JMS Handler Thread");
			jmsThread.start();
		}
		catch(Exception ex)
		{
			System.out.println("Error connecting to JMS: " + ex);
		}
		
		// Manage connection top SQL database.
		RdbHandler rdbHandler = new RdbHandler();
		try
		{
			rdbHandler = new RdbHandler();
			
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
			matcher = new MessageMatcher(suppressions);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Start a port listener in a new thread for each port
		for(int i=0; i<IN_PORTS.length; ++i)
		{
			PortListener portListener = new PortListener(IN_PORTS[i], matcher, jmsHandler, rdbHandler);
			portListener.start();
		}
		
		// exit main() thread - JMS thread, SQL thread, and PortListener threads will keep going
    }
}
