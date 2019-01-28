package org.isis.logserver.server;

import java.util.Calendar;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.parser.ClientMessageParser;
import org.isis.logserver.parser.XmlMessageParser;
import org.isis.logserver.rdb.RdbHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;


public class LogMessageFactoryTests {
	private LogMessageFactory factory;
	private JmsHandler mockjmsHandler = mock(JmsHandler.class);
	private RdbHandler mockRbdHandler = mock(RdbHandler.class);
	private ClientMessageParser mockClientMessageParser = mock(ClientMessageParser.class);
	private XmlMessageParser mockParser = mock(XmlMessageParser.class);
	
	private final LogMessage EMPTY_MESSAGE = new LogMessage();
	
	private void setUpMockParser(final String testContent) {
		doAnswer(new Answer<Object>() {
		      public Object answer(InvocationOnMock invocation) {
		          Object[] args = invocation.getArguments();
		          ((LogMessage)args[0]).setContents(testContent);
		          return null;
		      }})
		.when(mockParser).parse(any(LogMessage.class));
	}
	
	@Before
	public void setUp() {
		factory = new LogMessageFactory(mockClientMessageParser, mockParser);
		
		EMPTY_MESSAGE.setContents("");
	}
	
	@Test
	public void WHEN_log_message_called_THEN_client_message_parser_called_with_xml_contents() {
		String inputString = "<?xml><message></message>";
		final String testContent = "SOME_TEST_CONTENT";
		
		setUpMockParser(testContent);
		
		when(mockClientMessageParser.parse(eq(testContent), any(LogMessage.class))).thenReturn(EMPTY_MESSAGE);
		
		factory.createLogMessage(inputString, Calendar.getInstance());
		
		verify(mockClientMessageParser, times(1)).parse(eq(testContent), any(LogMessage.class));
	}
	
	@Test
	public void WHEN_xml_parser_doesnt_fill_contents_THEN_message_parser_called_with_raw_data() {
		String inputString = "INVALID_XML";
		
		setUpMockParser(null);
		when(mockClientMessageParser.parse(eq(inputString), any(LogMessage.class))).thenReturn(EMPTY_MESSAGE);
		
		factory.createLogMessage(inputString, Calendar.getInstance());
		
		verify(mockClientMessageParser, times(1)).parse(eq(inputString), any(LogMessage.class));
	}
	
	@Test
	public void GIVEN_empty_message_string_WHEN_log_message_called_THEN_no_message_sent_to_db_or_jms() {
		setUpMockParser("");
		when(mockClientMessageParser.parse(anyString(), any(LogMessage.class))).thenReturn(EMPTY_MESSAGE);
		
		factory.createLogMessage("", Calendar.getInstance());
		
		verify(mockjmsHandler, never()).addToDispatchQueue(any(LogMessage.class));
		verify(mockRbdHandler, never()).saveMessageToDb(any(LogMessage.class));
	}
}
