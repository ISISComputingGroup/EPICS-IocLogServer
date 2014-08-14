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
package org.isis.logserver.jms;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.isis.logserver.message.LogMessage;
import org.isis.logserver.server.Config;
import org.isis.logserver.xml.XmlWriter;

/**
 * Handles the connection to a Java Message Service (JMS) Server.
 * 	Automatically attempts to reestablish connection if it is dropped
 * 	and buffers any messages received while the connection is down, so
 * 	they can be sent when it is reestablished.
 * 
 */
@SuppressWarnings("nls")
public class JmsHandler implements Runnable
{
	/** JMS Server URL */
	private final String url;
	private final String topic;
	

	/** Server name, <code>null</code> if not connected */
	private String jms_server = null;

	/** 'run' flag to thread */
	private volatile boolean run = true;

	/** JMS Connection */
	private Connection connection;

	/** JMS Session */
	private Session session;

	private boolean connectedToJms;

	private MessageProducer client_producer;

	private Queue<LogMessage> messageBuffer;
	private int MAX_BUFFER_SIZE = 10000;

	public JmsHandler(Config config) 
	{	
		this.url = config.getJmsUrl();
		this.topic = config.getJmsTopic();

		messageBuffer = new ArrayDeque<LogMessage>();
	}

	public synchronized boolean isConnected() {
		return jms_server != null && connectedToJms;
	}

	/**
	 * Create a producer. Derived class can use this to create one or more
	 * producers, sending MapMessages to them in the communicator thread.
	 */
	protected MessageProducer createProducer(final String topic_name)
			throws JMSException 
	{
		final Topic topic = session.createTopic(topic_name);
		final MessageProducer producer = session.createProducer(topic);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		return producer;
	}

	/**
	 * Create JMS producers and consumers. To be implemented by derived classes.
	 */
	protected void createProducer() throws Exception {
		client_producer = createProducer(topic);
	}

	/**
	 * Close previously created JMS producers and consumers. To be implemented
	 * by derived classes.
	 */
	protected void closeProducer() throws Exception {
		client_producer.close();
	}

	public void addToDispatchQueue(LogMessage msg) 
	{
		if (msg != null) {
			addMessageToBuffer(msg);
		}
	}

	/**
	 * Messages to be sent through JMS are added to the message buffer by other
	 * classes that use the JmsMessenger. The JmsMessenger checks for new
	 * messages in the buffer every second and dispatches them. The buffer
	 * allows for messages to be stored up if connection to the JMS server is
	 * temporarily lost.
	 */
	protected void addMessageToBuffer(LogMessage msg) 
	{
		synchronized (messageBuffer) {
			// If the maximum size of the buffer has been exceeded, add
			if (messageBuffer.size() > MAX_BUFFER_SIZE) {
				messageBuffer.poll();
			}

			messageBuffer.add(msg);
		}
	}

	protected void sendAllMessagesInBuffer() 
	{
		synchronized (messageBuffer) 
		{
			if (client_producer != null) 
			{
				while (messageBuffer.size() > 0) 
				{
					try
					{
						LogMessage message = messageBuffer.peek();
						
						String messageContent = message.getContents();
						String xmlMessage = XmlWriter.MessageToXmlString(message);
						
						client_producer.send(session.createTextMessage(xmlMessage));

						// remove the message from the queue if successfully sent
						messageBuffer.remove();
						System.out.println("Sent JMS message: " + messageContent);
					} 
					catch (JMSException ex) {
						break;
					}
				}
			}
		}
	}

	/** 'Runnable' for the thread */
	public void run() 
	{
		while (run) 
		{
			if (isConnected()) {
				sendAllMessagesInBuffer();
			}

			// If not connected or if connection is dropped, establish
			// connection with JMS
			else 
			{
				try 
				{
					connect();
					System.out.println("Connected to JMS server @ " + url);
					connectedToJms = true;

					synchronized (this) 
					{
						// Use URL as server name.
						jms_server = url;
					}

					// Give JMS clients a few seconds to potentially reconnect
					// to the JMS server so they don't miss any buffered messages
					Thread.sleep(4000);
				} 
				catch (Exception ex) 
				{
					System.out.println("Problem connecting to JMS server. Will retry in 5 seconds.");
					
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) { }
				}
			}

			// Pause between cycles
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
		}

		disconnect();

		synchronized (this) {
			jms_server = null;
		}
	}

	/**
	 * Connect to JMS
	 */
	private void connect() throws Exception 
	{
		connection = JMSConnectionFactory.connect(url);
		
		// Try to update JMS server info via connection listener
		JMSConnectionFactory.addListener(connection,
				new JMSConnectionListener() 
				{
					@Override
					public void linkUp(final String server) {
						synchronized (this) {
							jms_server = server;
						}
					}

					@Override
					public void linkDown() {
						synchronized (this) {
							jms_server = null;
						}
					}
		});

		// Log exceptions
		connection.setExceptionListener(new ExceptionListener() {
			@Override
			public void onException(final JMSException ex) {
				System.out.println("Lost connection to JMS server: " + url);
				connectedToJms = false;
			}
		});

		try 
		{
			// When server is unavailable, we'll hang in here
			connection.start();
		} 
		catch (JMSException ex) 
		{
			// Not an error if we already gave up
			if (run == false)
				return;
			throw ex;
		}
		
		session = connection.createSession(/* transacted */false,
				Session.AUTO_ACKNOWLEDGE);
		createProducer();
	}

	/** Disconnect from JMS */
	private void disconnect() {
		try {
			closeProducer();
		} catch (Exception ex) {
			System.out.println("JMS shutdown error: " + ex);
		}
		try {
			session.close();
		} catch (JMSException ex) {
			System.out.println("JMS shutdown error: " + ex);
		}
	}
}
