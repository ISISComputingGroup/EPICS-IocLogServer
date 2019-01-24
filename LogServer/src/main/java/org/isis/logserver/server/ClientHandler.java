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
    
    private ClientMessageParser messageParser;
    private XmlMessageParser xmlParser; 
    
    

	/**
	 * Sets parameters for messages sent by IOCLogServer.java
	 * @param client_socket
	 */
    public ClientHandler(final Socket client_socket, final MessageMatcher matcher, 
    		final JmsHandler jmsHandler, final RdbHandler rdbHandler, ClientMessageParser messageParser, final XmlMessageParser xmlParser)
    {
    	this.matcher=matcher;
        this.clientSocket = client_socket;
        this.clientHost = client_socket.getInetAddress().getHostName();
        this.jmsHandler = jmsHandler;
        this.rdbHandler = rdbHandler;
        
        this.messageParser = messageParser;

        this.applicationId = STANDALONE_LOG_SERVER;
        System.out.println("IOC Client " + clientHost + ":" + client_socket.getPort() + " connected");
        
        this.xmlParser = xmlParser;
    }

    /** Thread runnable */
    @Override
    public void run ()
    {	
        try(InputStream inputStream = clientSocket.getInputStream();
        		InputStreamReader isReader = new InputStreamReader(inputStream);
        		BufferedReader rdr = new BufferedReader(isReader))
        {
            // If called as log server, we keep reading messages from
            // the client, logging each one
        	String line;
        	String message = "";
            
            while ((line = rdr.readLine())!= null)
            {
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
                
                // Plain string messages are delimited by '\n', however XML formatted messages may contain '\n'
                //	inside the message. Check if message is a half-completed XML formatted message, and if so
        		//	continue to next loop iteration to collect the rest of the message.
        		if(isMessageStartXml(message) && !isMessageEndXml(message))
        		{
        			continue;
        		}
                
            	final boolean suppressible = matcher.check(message);
                if (Config.verbose)
                {
            	    System.out.println("Message received from "+ clientHost + ":" + clientSocket.getPort() + " - " +  message);
            	}
                
            	if (suppressible==false) 
            	{
            		logMessage(message, calendar);
            	}
            	else
            	{
            	    System.out.println(new Date() + "  Suppressed: " + clientHost + " message '" + message + "'");
            	}
            	
            	message = "";
            }
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

    /** Log current text, severity, ... to RDB
     */
    public void logMessage(String messageStr, Calendar timeReceived)
    {
    	LogMessage clientMessage = new LogMessage();
    	clientMessage.setRawMessage(messageStr);
    	
    	// Try to create a message through the xml parser
    	clientMessage = xmlParser.parse(clientMessage);
    		
		// Use the supplied parser on the message contents
		if(messageParser != null)
		{
			clientMessage = messageParser.parse(clientMessage.getContents(), clientMessage);
		} 
    	
    	// If the contents is empty, drop message
    	String contents = clientMessage.getContents();
    	if(contents == null || contents.trim().equals("") ) {
			return;
		}
    	
    	// Fill property in blanks if any
		if(clientMessage.getEventTime() == null) {
			clientMessage.setEventTime(Calendar.getInstance());
		}
		
		if(clientMessage.getClientName() == null) {
			clientMessage.setClientName("UNKNOWN");
		}
		
		if(clientMessage.getSeverity() == null) {
			clientMessage.setSeverity("-");
		}
		
		if(clientMessage.getType() == null) {
			clientMessage.setType("-");
		}
		
    	
    	clientMessage.setClientHost(clientHost);
    	clientMessage.setApplicationId(applicationId);
    	clientMessage.setCreateTime(timeReceived);
    	clientMessage.setRawMessage(messageStr);
    	
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
    private boolean isMessageXml(String msg)
    {   	
    	// check that message starts and ends with the expected xml tags
    	return isMessageStartXml(msg) && isMessageEndXml(msg);
    }
    
    private boolean isMessageStartXml(String msg)
    {
    	String trimmedMsg = msg.trim();
    	return trimmedMsg.startsWith("<?xml") || trimmedMsg.startsWith("<message");
    }
    
    private boolean isMessageEndXml(String msg)
    {
    	String trimmedMsg = msg.trim();
    	return trimmedMsg.endsWith("</message>");
    }
}
