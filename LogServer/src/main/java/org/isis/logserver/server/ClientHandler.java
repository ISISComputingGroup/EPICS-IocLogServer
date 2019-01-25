/*
 * Copyright (C) 2013-2016 Research Councils UK (STFC)
 *
 * This file is part of the Instrument Control Project at ISIS.
 *
 * This code and information are provided "as is" without warranty of any kind,
 * either expressed or implied, including but not limited to the implied
 * warranties of merchantability and/or fitness for a particular purpose.
 */
package org.isis.logserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.MessageFilter;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.message.MessageState;
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

    /** Application ID used when invoked from stand-alone log server */
    private static final String STANDALONE_LOG_SERVER = "IOCLogServer";

    final private Socket clientSocket;
    private String clientHost="";
    final private String applicationId;
    
    private MessageMatcher matcher = null;
    
    /** Handles connection to JMS server */
    private JmsHandler jmsHandler = null;
    
    /** Handles connection to the SQL database */
    private RdbHandler rdbHandler;

    final private MessageFilter filter = MessageFilter.getInstance();
    
    private LogMessageFactory messageFactory;    

	/**
	 * Sets parameters for messages sent by IOCLogServer.java
	 * @param client_socket
	 */
    public ClientHandler(final Socket client_socket, final MessageMatcher matcher, 
    		final JmsHandler jmsHandler, final RdbHandler rdbHandler, LogMessageFactory messageFactory)
    {
    	this.matcher = matcher;
        this.clientSocket = client_socket;
        this.clientHost = client_socket.getInetAddress().getHostName();
        this.jmsHandler = jmsHandler;
        this.rdbHandler = rdbHandler;
        
        this.messageFactory = messageFactory;

        this.applicationId = STANDALONE_LOG_SERVER;
        System.out.println("IOC Client " + clientHost + ":" + client_socket.getPort() + " connected");
        
    }

    /**
     * Reads messages off of the reader and sends fully formed ones to be parsed.
     * @param rdr The reader to read messages from.
     * @throws IOException Thrown if reading a message fails.
     */
    public void handleMessages(BufferedReader rdr) throws IOException {
        // If called as log server, we keep reading messages from
        // the client, logging each one
    	String line;
    	String message = "";
    	
        while ((line = rdr.readLine())!= null)
        {
        	System.out.println("GOT LINE: " + line);
        	final Calendar calendar = Calendar.getInstance();
    		
    		// check if message is empty
        	line = line.trim();
            if (line.length() <= 0)
            {
                continue;
            }

            if(!message.equals(""))
            {
            	message += '\n';
            }
            
    		message += line;
    		
    		System.out.println("GOT MESSAGE: " + message);
            
            // Plain string messages are delimited by '\n', however XML formatted messages may contain '\n'
            //	inside the message. Check if message is a half-completed XML formatted message, and if so
    		//	continue to next loop iteration to collect the rest of the message.
    		if(isMessageStartXml(message) && !isMessageEndXml(message))
    		{
    			System.out.println("Message starts xml but does not finish, waiting for more: " + message);
    			continue;
    		}
            
        	final boolean suppressible = matcher.check(message);
            if (Config.verbose)
            {
        	    System.out.println("Message received from "+ clientHost + ":" + clientSocket.getPort() + " - " +  message);
        	}
            
        	if (suppressible==false) 
        	{
        		try {
        			logMessage(message, calendar);
        		} catch (Exception e) {
        			System.out.println("Failed to parse message from "+ clientHost + ":" + clientSocket.getPort() + " - " +  message);
        			message = "";
        		}
        	}
        	else
        	{
        	    System.out.println(new Date() + "  Suppressed: " + clientHost + " message '" + message + "'");
        	}
        	
        	message = "";
        }
    }
    
    /** Thread runnable */
    @Override
    public void run ()
    {	
        try(InputStream inputStream = clientSocket.getInputStream();
        		InputStreamReader isReader = new InputStreamReader(inputStream);
        		BufferedReader rdr = new BufferedReader(isReader))
        {
        	handleMessages(rdr);
        }
        catch (Exception e)
        {
        	System.out.println("Lost connection with client at "+ clientHost + ":" + clientSocket.getPort());
        }
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();
                System.out.println("IOC Client " + clientHost + ":" + clientSocket.getPort() + " disconnected");
            } catch (IOException e) {
                System.out.println(
                        "Excption on close socket with client at " + clientHost + ":" + clientSocket.getPort());
                e.printStackTrace();
            }
        }
    }


    /**
     * Takes a messages contents and logs it to DB/JMS.
     * 
     * @param messageStr The message contents.
     * @param timeReceived The time that the message was received.
     * @throws Exception 
     */
    public void logMessage(String messageStr, Calendar timeReceived) throws Exception
    {
    	LogMessage clientMessage = messageFactory.createLogMessage(messageStr, timeReceived);
    	
    	clientMessage.setClientHost(clientHost);
    	clientMessage.setApplicationId(applicationId);
    	
    	// TODO: filter out repeat messages
		synchronized (filter)
        {
			final MessageState info = filter.checkMessageState(clientHost, clientMessage.getRawMessage());
			
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
    private boolean isMessageStartXml(String msg)
    {
    	return msg.trim().startsWith("<?xml") || msg.trim().startsWith("<message");
    }
    
    private boolean isMessageEndXml(String msg)
    {
    	return msg.trim().endsWith("</message>");
    }
}
