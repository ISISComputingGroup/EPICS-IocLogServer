package org.isis.logserver.parser;

import static org.junit.Assert.assertEquals;

import org.isis.logserver.message.LogMessage;
import org.junit.Before;
import org.junit.Test;

public class XmlMessageParserTests {
	private XmlMessageParser xmlParser = new XmlMessageParser();
	
	@Test
	public void GIVEN_invalid_xml_WHEN_parse_called_THEN_log_message_with_contents_and_raw_message_returned() {
		LogMessage message = new LogMessage();
		String invalidXML = "SOME_INVALID_XML";
		message.setRawMessage(invalidXML);
		
		LogMessage returnedMessage = xmlParser.parse(message);
		
		assertEquals(returnedMessage.getContents(), invalidXML);
		assertEquals(returnedMessage.getRawMessage(), invalidXML);
	}
}
