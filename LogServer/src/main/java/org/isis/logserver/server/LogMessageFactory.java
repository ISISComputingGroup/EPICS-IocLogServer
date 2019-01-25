package org.isis.logserver.server;

import java.util.Calendar;

import org.isis.logserver.message.LogMessage;
import org.isis.logserver.parser.ClientMessageParser;
import org.isis.logserver.parser.XmlMessageParser;

/**
 * Creates LogMessage objects from message strings.
 */
public class LogMessageFactory {

    private ClientMessageParser messageParser;
    private XmlMessageParser xmlParser;
	
	public LogMessageFactory(ClientMessageParser messageParser, XmlMessageParser xmlParser) {
		this.messageParser = messageParser;
		this.xmlParser = xmlParser;
	}
	
	/**
	 * Creates a LogMessage from a string.
	 * 
	 * @param rawMessage The raw messages string.
	 * @param timeReceived The time that the message was received.
	 * @return The LogMessage object.
	 */
	public LogMessage createLogMessage(String rawMessage, Calendar timeReceived) {
    	LogMessage clientMessage = new LogMessage();
    	clientMessage.setRawMessage(rawMessage);
    	
    	// Try to create a message through the xml parser
    	xmlParser.parse(clientMessage);
    	
    	if (clientMessage.getContents() != null) {
			// Use the supplied parser on the message contents
			clientMessage = messageParser.parse(clientMessage.getContents(), clientMessage);
    	} else {
    		// Use the supplied parser on the raw message
    		clientMessage = messageParser.parse(rawMessage, clientMessage);
    	}
    	
    	// If the contents is empty, drop message
    	String contents = clientMessage.getContents();
    	if(contents == null || contents.trim().equals("") ) {
			return null;
		}
    	
    	clientMessage.setCreateTime(timeReceived);
    	
    	return clientMessage;
	}
}
