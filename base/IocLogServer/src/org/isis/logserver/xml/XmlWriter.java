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
package org.isis.logserver.xml;

import org.isis.logserver.message.LogMessage;

public class XmlWriter 
{
	// XML tags - do not change these without changing the corresponding
	//	tags in the XML reader in any client code (e.g. Eclipse CSS)
	private static final String MESSAGE_TAG = "message";
	private static final String CONTENTS_TAG = "contents";
	private static final String SEVERITY_TAG = "severity";
	private static final String EVENTTIME_TAG = "eventTime";
	private static final String CLIENTNAME_TAG = "clientName";
	private static final String TYPE_TAG = "type";
	
	/**
	 * Convert the message in to an XML format that can be sent over JMS
	 */
	public static String MessageToXmlString(LogMessage message)
	{
		StringBuilder xml = new StringBuilder();
		
		xml.append(makeOpenTag(MESSAGE_TAG));
		
		xml.append(makeElement(CONTENTS_TAG, message.getContents()));
		xml.append(makeElement(SEVERITY_TAG, message.getSeverity()));
		xml.append(makeElement(EVENTTIME_TAG, message.getEventTime()));
		xml.append(makeElement(CLIENTNAME_TAG, message.getClientName()));
		xml.append(makeElement(TYPE_TAG, message.getType()));
		
		xml.append(makeCloseTag(MESSAGE_TAG));
		
		return xml.toString();
	}
	
	public static String makeElement(String tag, String data)
	{
		return makeOpenTag(tag) + data + makeCloseTag(tag);
	}
	
	public static String makeOpenTag(String tag)
	{
		return "<" + tag + ">";
	}
	
	public static String makeCloseTag(String tag)
	{
		return "</" + tag + ">";
	}

}
