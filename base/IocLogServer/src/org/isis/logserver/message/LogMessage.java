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
package org.isis.logserver.message;

import java.sql.Timestamp;
import java.util.Calendar;


public class LogMessage 
{
	// message info
	private String rawMessage;
	private String contents;
	private String severity;
	private Calendar eventTime;
	private String clientName;
	private String type;
	
	// meta info
	private String clientHost;
	private Calendar createTime;
	private String applicationId;
	
	public String getRawMessage() {
		return rawMessage;
	}
	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public Calendar getEventTime() {
		return eventTime;
	}
	public void setEventTime(Calendar eventTime) {
		this.eventTime = eventTime;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClientHost() {
		return clientHost;
	}
	public void setClientHost(String clientHost) {
		this.clientHost = clientHost;
	}
	public Calendar getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Calendar createTime) {
		this.createTime = createTime;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public void setProperty(LogMessageFields property, String value)
	{
		switch(property)
		{
			case CONTENTS:
				contents = value;
				break;
			case SEVERITY:
				severity = value;
				break;
			case CLIENT_NAME:
				clientName = value;
				break;
			case CLIENT_HOST:
				clientHost = value;
				break;
			case TYPE:
				type = value;
				break;
			case APPLICATION_ID:
				applicationId = value;
				break;
			default:
				new Throwable().getStackTrace();
		}
	}
	
	public void setProperty(LogMessageFields property, Calendar value)
	{
		switch(property)
		{
			case EVENT_TIME:
				eventTime = value;
				break;
			case CREATE_TIME:
				createTime = value;
				break;
				
			default:
				new Throwable().getStackTrace();
		}	
	}
	
	public String getProperty(LogMessageFields property)
	{
		switch(property)
		{
			case CONTENTS:
				return contents;
			case SEVERITY:
				return severity;
			case EVENT_TIME:
				return timeToString(eventTime);
			case CREATE_TIME:
				return timeToString(createTime);	
			case CLIENT_NAME:
				return clientName;
			case CLIENT_HOST:
				return clientHost;
			case TYPE:
				return type;
			case APPLICATION_ID:
				return applicationId;
			default:
				new Throwable().getStackTrace();
				return null;
		}
	}
	
	public static String timeToString(Calendar time)
	{
		if(time == null) {
			return "";
		}
		
		Timestamp timestamp = new Timestamp(time.getTimeInMillis());
		int num_chars = Math.min(23, timestamp.toString().length()); // only want milliseconds, not nanoseconds
		String timePrint = timestamp.toString().substring(0,num_chars);
		
		return timePrint;
	}
}
