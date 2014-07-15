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
		
		// caput message Format: 
		//		(0)Date, (1)Time, (2)PC_name, (3)user_name, (4)PV_name, (5)new=new_value, (6)old=old_value  
		String[] parts = text.split(" ");
		
		if(parts.length == 7)
		{
			message.setContents("Changed PV: '" + parts[4] + "' from '" + parts[6] + "' to '" + parts[5] + "'"  );
			message.setClientName(parts[2] + ":" + parts[3]);
		}
		else
		{
			message.setContents(text);
		}
		
		message.setSeverity(SEVERITY);
		message.setType("CA put");
		
		// temp - setting event time from current time rather than parsing from message
    	final Calendar calendar = Calendar.getInstance();
		final Timestamp timeStamp = new Timestamp(calendar.getTime().getTime());
		message.setEventTime(timeStamp.toString());
		
		
		return message;
	}

}
