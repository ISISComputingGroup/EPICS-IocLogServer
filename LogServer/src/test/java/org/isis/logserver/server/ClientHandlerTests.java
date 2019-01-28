package org.isis.logserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

import org.isis.logserver.jms.JmsHandler;
import org.isis.logserver.message.LogMessage;
import org.isis.logserver.message.MessageMatcher;
import org.isis.logserver.rdb.RdbHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;


public class ClientHandlerTests {
	private ClientHandler handler;
	private Socket mockSocket = mock(Socket.class);
	private MessageMatcher mockMessageMatcher = mock(MessageMatcher.class);
	private JmsHandler mockjmsHandler = mock(JmsHandler.class);
	private RdbHandler mockRbdHandler = mock(RdbHandler.class);
	private LogMessageFactory mockFactory = mock(LogMessageFactory.class);
	
	private final LogMessage EMPTY_MESSAGE = new LogMessage();
	
	@Before
	public void setUp() {
		InetAddress mockAddress = mock(InetAddress.class);
		when(mockAddress.getHostName()).thenReturn("TEST_HOST");
		when(mockSocket.getInetAddress()).thenReturn(mockAddress);
		handler = new ClientHandler(mockSocket, mockMessageMatcher, mockjmsHandler, mockRbdHandler, mockFactory);
		
		EMPTY_MESSAGE.setContents("");
	}
	
	@Test
	public void GIVEN_message_is_not_parsable_WHEN_handle_messages_called_THEN_first_message_thrown_away() throws IOException{
		String firstMessage = "FIRST";
		String secondMessage = "SECOND";
		
		BufferedReader mockReader = mock(BufferedReader.class);
		doReturn(firstMessage).doReturn(secondMessage).doReturn(null).when(mockReader).readLine();
		doThrow(new RuntimeException()).when(mockFactory).createLogMessage(anyString(), any(Calendar.class));
		
		handler.handleMessages(mockReader);
		
		InOrder inOrder = inOrder(mockFactory);
		inOrder.verify(mockFactory).createLogMessage(eq(firstMessage), any(Calendar.class));
		inOrder.verify(mockFactory).createLogMessage(eq(secondMessage), any(Calendar.class));
	}
	
	@Test
	public void GIVEN_message_is_split_xml_WHEN_handle_messages_called_THEN_full_message_parsed() throws IOException{
		String firstMessage = "<messageFIRST";
		String secondMessage = "SECOND</message>";
		
		BufferedReader mockReader = mock(BufferedReader.class);
		doReturn(firstMessage).doReturn(secondMessage).doReturn(null).when(mockReader).readLine();
		doThrow(new RuntimeException()).when(mockFactory).createLogMessage(anyString(), any(Calendar.class));
		
		handler.handleMessages(mockReader);
		
		verify(mockFactory, times(1)).createLogMessage(eq(firstMessage + "\n" + secondMessage), any(Calendar.class));
	}
}
