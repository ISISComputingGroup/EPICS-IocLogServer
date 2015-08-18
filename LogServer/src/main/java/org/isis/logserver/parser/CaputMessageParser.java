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
import java.util.Locale;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.LogMessageFields;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

public class CaputMessageParser implements ClientMessageParser 
{
    /** Default severity */
    static final String SEVERITY_INFO = "INFO";
    static int fake_millisecs = 0;
    static int last_secs = -1;
	@Override
	public LogMessage parse(String text, LogMessage msg) 
	{
		// copy message
		LogMessage message = new LogMessage(msg);

		// caput message Format: 
		//		(0)Date, (1)Time, (2)PC_name, (3)user_name, (4)PV_name, (5)new=new_value, (6)old=old_value  
		//		(0)Date, (1)Time, (2)PC_name, (3)user_name, (4)PV_name, (5)new=new_value, (6)old=old_value, (7)min=min_value, (8)max=max_value

		String[] parts = text.split("[\\s]+");  // this will remove embedded whitespace/newlines
		
		if (parts.length >= 5)
		{
            String mess = "";
            for(int i=4; i< parts.length; ++i)
            {
                mess += parts[i];
                mess += " ";
            }   
			message.setContents("Changed PV: " + mess);
			// if a client name has not already been set, use PC_name:user_name
			String clientName = message.getClientName();
			if(clientName == null || clientName.trim().equals("")) 
            {
				message.setClientName(parts[2] + ":" + parts[3]);
			}
            try
            {
                DateTimeFormatter timeParser = DateTimeFormat.forPattern("dd-MMM-YY HH:mm:ss");
                String time_value = parts[0] + " " + parts[1];
			    DateTime dateTime = timeParser.parseDateTime(time_value);
                // add a made up milliseconds to preserve order
                int secs = dateTime.getSecondOfMinute();
                ++fake_millisecs;
                if (fake_millisecs > 900)
                {
                    fake_millisecs = 0;
                }
                Calendar time = dateTime.plusMillis(fake_millisecs).toCalendar(Locale.UK);
			    message.setProperty(LogMessageFields.EVENT_TIME, time);
                last_secs = secs;
            }
			catch(Exception ex)
			{
				System.out.println("Exception " + ex.toString());
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
