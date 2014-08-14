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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.LogMessageFields;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses messages that are received from the IOC in an XML format
 *
 */
public class XmlMessageParser implements ClientMessageParser
{
	@Override
	public LogMessage parse(String text) 
	{
		LogMessage message = xmlToLogMessage(text);
		
		// message null if parse failed
		if(message == null) {
			return null;
		}
		
		if(message.getContents() == null || message.getContents().equals("")) {
			message.setContents(text);
		}
		
		message.setRawMessage(text);
		message.setType("IOC Error Log");
		
		// temp - setting event time from current time rather than parsing from message
		message.setEventTime(Calendar.getInstance());
		
		return message;
	}
	
	public LogMessage xmlToLogMessage(String xml)
	{
		LogMessage message = new LogMessage();
		
		Node root;
		try
		{
			root = getRootElement(xml);
		}
		catch(SAXException|IOException|ParserConfigurationException ex)
		{
			System.out.println("Could not parse XML formatted message.");
			return null;
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
				message.setProperty(field, value);
			}
			catch(IllegalArgumentException ex)
			{
				System.out.println("Unrecognised tag in XML message: '" + tag + "'");
			}
		}

		return message;
	}
	
	private Node getRootElement(String xml) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();

		InputSource inputSource = new InputSource( new StringReader( xml ) );
		
		Document newDoc = builder.parse(inputSource);

		return newDoc.getDocumentElement();
	}
}
