package org.isis.logserver.server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.parser.ClientMessageParser;
import org.isis.logserver.parser.XmlMessageParser;
import org.isis.logserver.rdb.RdbHandler;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class ClientHandlerTests {
	private ClientHandler handler;
	private Socket mockSocket = mock(Socket.class);
	private MessageMatcher mockMessageMatcher = mock(MessageMatcher.class);
	private JmsHandler mockjmsHandler = mock(JmsHandler.class);
	private RdbHandler mockRbdHandler = mock(RdbHandler.class);
	private ClientMessageParser mockClientMessageParser = mock(ClientMessageParser.class);
	private XmlMessageParser mockParser = mock(XmlMessageParser.class);
	
	private final LogMessage EMPTY_MESSAGE = new LogMessage();
	
	@Before
	public void setUp() {
		InetAddress mockAddress = mock(InetAddress.class);
		when(mockAddress.getHostName()).thenReturn("TEST_HOST");
		when(mockSocket.getInetAddress()).thenReturn(mockAddress);
		handler = new ClientHandler(mockSocket, mockMessageMatcher, mockjmsHandler, mockRbdHandler, mockClientMessageParser, mockParser);
		
		EMPTY_MESSAGE.setContents("");
	}
	
	@Test
	public void WHEN_log_message_called_THEN_client_message_parser_called_with_xml_contents() {
		String inputString = "<?xml><message></message>";
		
		String testContent = "SOME_TEST_CONTENT";
		LogMessage testMessage = new LogMessage();
		testMessage.setContents(testContent);
		
		when(mockParser.parse(any(LogMessage.class))).thenReturn(testMessage);
		when(mockClientMessageParser.parse(anyString(), eq(testMessage))).thenReturn(testMessage);
		
		handler.logMessage(inputString, Calendar.getInstance());
	}
	
	@Test
	public void GIVEN_empty_message_string_WHEN_log_message_called_THEN_no_message_sent_to_db_or_jms() {
		when(mockParser.parse(any(LogMessage.class))).thenReturn(EMPTY_MESSAGE);
		when(mockClientMessageParser.parse(anyString(), any(LogMessage.class))).thenReturn(EMPTY_MESSAGE);
		
		handler.logMessage("", Calendar.getInstance());
		
		verify(mockjmsHandler, never()).addToDispatchQueue(any(LogMessage.class));
		verify(mockRbdHandler, never()).saveMessageToDb(any(LogMessage.class));
	}
	
}
