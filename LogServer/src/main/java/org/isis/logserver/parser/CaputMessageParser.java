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

import org.isis.logserver.message.LogMessage;

public class CaputMessageParser implements ClientMessageParser 
{
    /** Default severity */
    static final String SEVERITY_INFO = "INFO";

	@Override
	public LogMessage parse(String text, LogMessage msg) 
	{
		// copy message
		LogMessage message = new LogMessage(msg);

		// caput message Format: 
		//		(0)Date, (1)Time, (2)PC_name, (3)user_name, (4)PV_name, (5)new=new_value, (6)old=old_value  
		String[] parts = text.split(" ");
		
		if(parts.length == 7)
		{
			message.setContents("Changed PV: '" + parts[4] + "' from '" + parts[6] + "' to '" + parts[5] + "'"  );
			
			// if a client name has not already been set, use PC_name:user_name
			String clientName = message.getClientName();
			if(clientName == null || clientName.trim().equals("")) {
				message.setClientName(parts[2] + ":" + parts[3]);
			}
		}
		else
		{
			message.setContents(text);
		}
		
		message.setSeverity(SEVERITY_INFO);
		message.setType("caput");
		
		
		return message;
	}

}
