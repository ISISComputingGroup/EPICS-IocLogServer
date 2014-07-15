/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.isis.logserver.jms;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.isis.logserver.message.LogMessage;
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
	final private String url;

	/** Server name, <code>null</code> if not connected */
	private String jms_server = null;

	/** 'run' flag to thread */
	private volatile boolean run = true;

	/** JMS Connection */
	private Connection connection;

	/** JMS Session */
	private Session session;

	private boolean connectedToJms;

	private static String TOPIC = "iocLogs";
	private MessageProducer client_producer;


	private Queue<LogMessage> messageBuffer;
	private int MAX_BUFFER_SIZE = 10000;

	/**
	 * Initialize
	 * 
	 * @param url
	 *            JMS Server URL
	 * @throws NullPointerException
	 *             for <code>null</code> URL
	 */
	public JmsHandler(final String url) 
	{
		if (url == null) 
		{
			throw new NullPointerException("JMS URL must not be null");
		}

		this.url = url;

		messageBuffer = new ArrayDeque<LogMessage>();
	}

	/** @return <code>true</code> when connected */
	public synchronized boolean isConnected() {
		return jms_server != null && connectedToJms;
	}

	/**
	 * Create a producer. Derived class can use this to create one or more
	 * producers, sending MapMessages to them in the communicator thread.
	 * 
	 * @param topic_name
	 *            Name of topic for the new producer
	 * @return MessageProducer
	 * @throws JMSException
	 *             on error
	 */
	protected MessageProducer createProducer(final String topic_name)
			throws JMSException {
		final Topic topic = session.createTopic(topic_name);
		final MessageProducer producer = session.createProducer(topic);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		return producer;
	}

	/**
	 * Create a consumer.
	 * 
	 * @param topic_name
	 *            Name of topic for the new consumer
	 * @return MessageProducer
	 * @throws JMSException
	 *             on error
	 */
	protected MessageConsumer createConsumer(final String topic_name)
			throws JMSException {
		final Topic topic = session.createTopic(topic_name);
		final MessageConsumer consumer = session.createConsumer(topic);
		return consumer;
	}

	/**
	 * Create JMS producers and consumers. To be implemented by derived classes.
	 * 
	 * @throws Exception
	 *             on error
	 */
	protected void createProducersAndConsumers() throws Exception {
		client_producer = createProducer(TOPIC);
	}

	/**
	 * Close previously created JMS producers and consumers. To be implemented
	 * by derived classes.
	 * 
	 * @see #createProducersAndConsumers()
	 * @throws Exception
	 *             on error
	 */
	protected void closeProducersAndConsumers() throws Exception {
		client_producer.close();
	}

	public void addToDispatchQueue(LogMessage msg) {
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
	protected void addMessageToBuffer(LogMessage msg) {
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
						
						client_producer.send(createTextMessage(xmlMessage));

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

	/**
	 * Create empty map message on the communicator's session
	 * 
	 * @return MapMessage
	 * @throws JMSException
	 *             on error
	 */
	protected synchronized MapMessage createMapMessage() throws JMSException {
		return session.createMapMessage();
	}

	protected synchronized TextMessage createTextMessage(String msg)
			throws JMSException 
	{
		return session.createTextMessage(msg);
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
					System.out.println("Connected to JMS server: " + url);
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
					System.out.println("Problem connecting to JMS server. Will retry in 1 second.");
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
	 * 
	 * @throws Exception
	 *             on error
	 */
	private void connect() throws Exception {
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
				// Activator.getLogger().log(Level.SEVERE, "JMS Exception", ex);
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
		createProducersAndConsumers();
	}

	/** Disconnect from JMS */
	private void disconnect() {
		try {
			closeProducersAndConsumers();
		} catch (Exception ex) {
			// Activator.getLogger().log(Level.WARNING, "JMS shutdown error",
			// ex);
			System.out.println("JMS shutdown error: " + ex);
		}
		try {
			session.close();
		} catch (JMSException ex) {
			// Activator.getLogger().log(Level.WARNING, "JMS shutdown error",
			// ex);
			System.out.println("JMS shutdown error: " + ex);
		}
	}
}
