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
package org.isis.logserver.parser;

import java.util.Calendar;

import org.isis.logserver.message.LogMessage;

public class IocMessageParser implements ClientMessageParser 
{
	
    /** Default severity */
    static final String SEVERITY = "ERROR";
    

	@Override
	public LogMessage parse(String text) 
	{
		LogMessage message = new LogMessage();
		
		message.setRawMessage(text);
		message.setContents(text);
		message.setSeverity(SEVERITY);
		message.setClientName("Fake IOC name");
		message.setType("ioclog");
		
		// temp - setting event time from current time rather than parsing from message
		message.setEventTime(Calendar.getInstance());
		
		
		return message;
	}

}
