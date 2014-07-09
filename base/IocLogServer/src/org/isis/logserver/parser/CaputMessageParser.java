package org.isis.logserver.parser;

import java.sql.Timestamp;
import java.util.Calendar;

import org.isis.logserver.message.LogMessage;

public class CaputMessageParser extends ClientMessageParser 
{
    /** Default severity */
    static final String SEVERITY = "NORMAL";

	@Override
	public LogMessage parse(String text) 
	{
		LogMessage message = new LogMessage();
		
		message.setRawMessage(text);
		message.setContents(text);
		message.setSeverity(SEVERITY);
		message.setClientName("Fake IOC name");
		message.setType("ioclog2");
		
		// temp - setting event time from current time rather than parsing from message
    	final Calendar calendar = Calendar.getInstance();
		final Timestamp timeStamp = new Timestamp(calendar.getTime().getTime());
		message.setEventTime(timeStamp.toString());
		
		
		return message;
	}

}
