package org.isis.logserver.rdb;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
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
	private RDB rdb;
	
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
					rdb = RDB.connectToCluster("ics_msg_log_app", "fake_password", "MSG_LOG.");
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
		String contents = message.getContents();
		String severity = message.getSeverity();
		String type = message.getType();
		String eventTime = message.getEventTime();
		String clientName = message.getClientName();
		String clientHost = message.getClientHost();
		String receivedTime = message.getTimeReceived();
		String applicationId = message.getApplicationId();
		
		final Map<String, String> messagePropertyMap = new HashMap<String, String>();
		messagePropertyMap.put("TEXT", contents);
		messagePropertyMap.put("HOST",  clientHost);
		messagePropertyMap.put("APPLICATION-ID", applicationId);
		messagePropertyMap.put("CREATETIME", receivedTime);
		messagePropertyMap.put("EVENTTIME", eventTime);
		
		
    	// Get DB Connection
        RDBWriter rdbwriter = null;
        try
        {
        	MessageFilter filter = MessageFilter.getInstance();
        	final MessageState info = filter.checkMessageState(clientHost, contents);
        	
        	rdbwriter = new RDBWriter("msg_log", rdb.getConnection());
        	long msg_id = rdbwriter.write(severity, type, clientName, messagePropertyMap);
        	
        	info.setMessageID(msg_id);	
        }
        catch(Exception ex)
        {
        	System.out.println("Could not connect to Database. Message not saved: " + contents);
        }
    	finally
    	{
    		if(rdbwriter != null)
    		{
    			rdbwriter.close();
    		}
		}
    }
}
