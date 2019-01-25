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

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.LogMessageFields;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses messages that are received from the IOC in an XML format.
 */
public class XmlMessageParser
{

	public void parse(LogMessage message)
	{	
		Node root;
		try
		{
			root = getRootElement(message.getRawMessage());
		}
		catch(SAXException|IOException|ParserConfigurationException ex)
		{
			System.out.println("Could not parse XML formatted message: " + ex.getMessage());
			return;
		}
		
		NodeList messageProperties = root.getChildNodes();
		
		for(int i=0; i<messageProperties.getLength(); ++i)
		{
			String tag = messageProperties.item(i).getNodeName();
			String value = messageProperties.item(i).getTextContent();
			
			// Get the enum value for the log message filed from the XML tag name
			try
			{
				LogMessageFields field = LogMessageFields.getFieldByTagName(tag);
				
				boolean isTimeField = tag.equals(LogMessageFields.EVENT_TIME.getTagName()) ||
						tag.equals(LogMessageFields.CREATE_TIME.getTagName());
				
				// time fields must be transformed from string to calendar
				if(isTimeField)
				{
					// Parse ISO 8601 compliant date using joda-time
					DateTimeFormatter timeParser = ISODateTimeFormat.dateTime();
					DateTime dateTime = timeParser.parseDateTime(value);
					Calendar time = dateTime.toCalendar(Locale.UK);
					message.setProperty(field, time);
				}
				else
				{
					message.setProperty(field, value);
				}
				
			}
			catch(IllegalArgumentException ex)
			{
				System.out.println("Unrecognised tag in XML message: '" + tag + "'");
			}
		}
	}
	
	private Node getRootElement(String xml) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		InputSource inputSource = new InputSource( new StringReader( xml ) );
		
		Document newDoc = builder.parse(inputSource);

		return newDoc.getDocumentElement();
	}
}
