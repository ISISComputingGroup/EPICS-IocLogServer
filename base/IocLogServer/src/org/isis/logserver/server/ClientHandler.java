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
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.MessageFilter;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.message.MessageState;
import org.isis.logserver.parser.ClientMessageParser;
import org.isis.logserver.parser.XmlMessageParser;
import org.isis.logserver.rdb.RdbHandler;

/**
 * Handles the connection to a single client. Listens for new messages from the client,
 * 	processes the messages and sends them to be dispatched to listeners via JMS, and
 * 	saves them to a database.
 *
 */
public class ClientHandler implements Runnable
{
    /** Message property for storing the 'repeat' count */
    //private static final String REPEATED_PROPERTY = "REPEATED";

    /** Only update 'repeat' info for every ... repeats */
    //private static final int REPEAT_THRESHOLD = 10;

    /** Application ID used when invoked from standalone log server */
    private static final String STANDALONE_LOG_SERVER = "IOCLogServer";

    final private Socket client_socket;
    private String client_host="";
    final private String application_id;
    
    private MessageMatcher matcher =null;
    
    /** Handles connection to JMS server */
    private JmsHandler jmsHandler = null;
    
    /** Handles connection to the SQL database */
    private RdbHandler rdbHandler;

    final private MessageFilter filter = MessageFilter.getInstance();
    
    private ClientMessageParser messageParser;
    private ClientMessageParser xmlParser; 
    
    

	/**
	 * Sets parameters for messages sent by IOCLogServer.java
	 * @param client_socket
	 */
    public ClientHandler(final Socket client_socket, final MessageMatcher matcher, 
    		final JmsHandler jmsHandler, final RdbHandler rdbHandler, ClientMessageParser messageParser)
    {
    	this.matcher=matcher;
        this.client_socket = client_socket;
        this.client_host = client_socket.getInetAddress().getHostName();
        this.jmsHandler = jmsHandler;
        this.rdbHandler = rdbHandler;
        
        this.messageParser = messageParser;

        this.application_id = STANDALONE_LOG_SERVER;
        System.out.println("IOC Client " + client_host + ":" + client_socket.getPort() + " connected");
        
        xmlParser = new XmlMessageParser();
    }

    /** Thread runnable */
    public void run ()
    {
        try
        {
            // If called as log server, we keep reading messages from
            // the client, logging each one
        	String msg;
            final BufferedReader rdr = new BufferedReader (new InputStreamReader(client_socket.getInputStream()) );
            while ((msg = rdr.readLine())!= null)
            {
            	final Calendar calendar = Calendar.getInstance();
        		
        		msg = msg.trim();	  
                
                if (msg.length() <= 0)
                {
                    System.out.println(new Date() + "  Empty message from " + client_host);
                    continue;
                }
                
            	final boolean suppressible = matcher.check(msg);
            	
            	System.out.println("Message received from "+ client_host + ":" + client_socket.getPort() + " - " +  msg);
            	
            	if (suppressible==false) 
            	{
            		logMessage(msg, calendar);
            	}
            	else
            	{
            	    System.out.println(new Date() + "  Suppressed: " + client_host + " message '" + msg + "'");
            	}
            }
        }
        catch (Exception e)
        {
        	System.out.println("Lost connection with client at "+ client_host + ":" + client_socket.getPort());
        }
    }

    /** Log current text, severity, ... to RDB
     *  @throws Exception on error
     */
    private void logMessage(String message, Calendar timeReceived) throws Exception
    {
    	// Create the message
    	LogMessage clientMessage = null;
    	if(isMessageXml(message)) {
    		clientMessage = xmlParser.parse(message);
    	} 

    	// null will be returned if xml fails to parse correctly
    	if(clientMessage == null) {
    		clientMessage = messageParser.parse(message);
    	}
    	
    	clientMessage.setClientHost(client_host);
    	clientMessage.setApplicationId(application_id);
    	clientMessage.setCreateTime(timeReceived);
    	
    	// TODO: filter out repeat messages

		synchronized (filter)
        {
			final MessageState info = filter.checkMessageState(client_host, clientMessage.getRawMessage());
			
			// SEND MESSAGE BY JMS
			if(info.isNewMessage())
			{
				rdbHandler.saveMessageToDb(clientMessage);
				jmsHandler.addToDispatchQueue(clientMessage);
			}
        }
    }
    
    /**
     * Determines whether the message received from the IOC is XML formatted.
     * TODO: this is a naive implementation; improve it.
     */
    private boolean isMessageXml(String msg)
    {
    	return msg.trim().startsWith("<");
    }
}
