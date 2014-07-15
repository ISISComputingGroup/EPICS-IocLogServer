package org.isis.logserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.parser.CaputMessageParser;
import org.isis.logserver.parser.ClientMessageParser;
import org.isis.logserver.parser.IocMessageParser;
import org.isis.logserver.rdb.RdbHandler;

/**
 * Listens on a specified port for incoming client connections. Starts a client 
 * 	handler thread for each new client to deal with messages received from the 
 * 	client.
 * 
 */
public class PortListener extends Thread
{
	private int port;
	private MessageMatcher suppressions;
	
	/** Handles connection to the Java message server */
	private JmsHandler jmsHandler;
	
    /** Handles connection to the SQL database */
    private RdbHandler rdbHandler;
	
	private ServerSocket listener;
	
	private boolean active;
	
	public PortListener(int port, MessageMatcher suppressions, JmsHandler jmsHandler, RdbHandler rdbHandler)
	{
		this.port = port;
		this.suppressions = suppressions;
		this.jmsHandler = jmsHandler;
		this.rdbHandler = rdbHandler;
		this.active = true;
	}
	
	@Override
	public void run() 
	{
		// Listen on the port specified in the constructor. If port is busy, keep
		//	keep retrying till its free
		while(listener == null)
		{
			try
			{
				listener = new ServerSocket(port);
				System.out.println("Listening for messages on port " + port);
			}
			catch(IOException ex)
			{
				System.out.println("Port: '" + port + "' is already in use by another process. Retrying in 2 seconds");
				
				// Wait a second before retrying the port if failed
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) { }
			}
		}
		
		// TODO: this shouldn't be hard-coded, move this selection elsewhere
		ClientMessageParser parser = null;
		if(port == 7011)
		{
			parser = new CaputMessageParser();
		}
		else
		{
			parser = new IocMessageParser();
		}
		
			
        try
        {
            while(active)
            {
                final Socket client_socket = listener.accept();
                final ClientHandler client_handler 
                	= new ClientHandler(client_socket, suppressions, jmsHandler, rdbHandler, parser);
                
                final Thread t = new Thread(client_handler);
                t.start();
            }
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
	}
}
