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
import org.isis.logserver.message.LogMessageFields;

public class XmlWriter 
{
	// XML tags - do not change these without changing the corresponding
	//	tags in the XML reader in any client code (e.g. Eclipse CSS)
	private static final String MESSAGE_TAG = "message";
	
	private static final LogMessageFields[] XML_FIELDS = {
		LogMessageFields.CONTENTS,
		LogMessageFields.SEVERITY,
		LogMessageFields.EVENT_TIME,
		LogMessageFields.CREATE_TIME,
		LogMessageFields.EVENT_TIME,
		LogMessageFields.CLIENT_NAME,
		LogMessageFields.CLIENT_HOST,
		LogMessageFields.TYPE,		
		LogMessageFields.APPLICATION_ID
	};
	
	/**
	 * Convert the message in to an XML format that can be sent over JMS
	 */
	public static String MessageToXmlString(LogMessage message)
	{
		StringBuilder xml = new StringBuilder();
		
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append(makeOpenTag(MESSAGE_TAG));
		
		for(LogMessageFields field: XML_FIELDS)
		{
			String value = message.getProperty(field);
			if(value != null) {
				xml.append(makeElement(field, value));
			}
		}
		
		xml.append(makeCloseTag(MESSAGE_TAG));
		
		return xml.toString();
	}
	
	public static String makeElement(LogMessageFields field, String data)
	{
		return makeOpenTag(field.getTagName()) + makeContents(data) + makeCloseTag(field.getTagName());
	}
	
	public static String makeOpenTag(Object tag)
	{
		return "<" + tag + ">";
	}
	
	public static String makeCloseTag(Object tag)
	{
		return "</" + tag + ">";
	}
	
	public static String makeContents(Object contents)
	{
		return"<![CDATA[" + contents.toString() + "]]>";
	}

}
