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

import java.util.ArrayDeque;
import java.util.Queue;

import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.MessageFilter;
import org.isis.logserver.message.MessageState;

/**
 * Handles the connection to a MySQL database. Automatically attempts 
 * 	to reestablish connection if it is dropped and buffers any messages 
 * 	received while the connection is down, so they can be sent when it 
 * 	is reestablished.
 * 
 */
public class RdbHandler implements Runnable
{
	private Rdb rdb;
	
	private Queue<LogMessage> messageBuffer;
	private int MAX_BUFFER_SIZE = 10000;
	
	public RdbHandler()
	{
		messageBuffer = new ArrayDeque<LogMessage>();
	}

	@Override
	public void run() 
	{
		while(true)
		{
			
			boolean isConnected = false;
			if(rdb != null)
			{
				try
				{
					int timeoutSeconds = 1;
					isConnected = rdb.getConnection().isValid(timeoutSeconds);
				}
				catch(Exception ex) {}
			}
			
			if(isConnected)
			{
				saveAllMessagesInBuffer();
			}
			else
			{
				// Connect to Database if not currently connected
				try 
				{
					rdb = Rdb.connectToDatabase();
					System.out.println("Connected to MySQL database");
				} 
				catch (Exception e) 
				{
					System.out.println("Could not connect to MySQL database. Will retry in 2 seconds");
				}
			}
			
    		// Pause between cycles
	        try 
	        {
				Thread.sleep(1000);
			} 
	        catch (InterruptedException e) { ; }
		}
	}
	
	public void close()
	{
		rdb.close();
	}
		
	public void saveMessageToDb(LogMessage message)
	{
		if(message != null)
		{
			synchronized(messageBuffer)
			{
				// If the maximum size of the buffer has been exceeded, add
				if(messageBuffer.size() > MAX_BUFFER_SIZE)
				{
					messageBuffer.poll();
				}
				
				messageBuffer.add(message);
			}
		}
	}
	
	protected void saveAllMessagesInBuffer()
	{
		synchronized(messageBuffer)
		{
			while(messageBuffer.size() > 0)
			{
				try
				{
					LogMessage message = messageBuffer.peek();				
					saveLogMessageToDb(message);
					
					// remove the message from the queue if successfully sent
					messageBuffer.remove();
					System.out.println("Saved message to DB: " + message.getContents());
				}
				catch(Exception ex)
				{
					break;
				}
			}
		}
	}
	
	
	protected void saveLogMessageToDb(LogMessage message) throws Exception
    {
        try
        {
        	MessageFilter filter = MessageFilter.getInstance();
        	final MessageState info = filter.checkMessageState(message.getClientHost(), message.getContents());
        	
        	RdbWriter rdbWriter = new RdbWriter(rdb.getConnection());
        	long msg_id = rdbWriter.saveLogMessageToDb(message);
        	
        	if(info.getMessageID() == -1)
        	{
        		info.setMessageID(msg_id);
        	}
        	
        }
        catch(Exception ex)
        {
        	System.out.println("Could not connect to Database. Message not saved: " + message.getContents());
        	ex.printStackTrace();
        }
    }
}
