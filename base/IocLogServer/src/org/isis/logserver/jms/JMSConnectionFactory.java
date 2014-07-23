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

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.TransportListener;

/** Helper for connecting to a JMS server.
 *  Shields from the underling ActiveMQ API,
 *  only providing a <code>javax.jms.Connection</code>.
 */
public class JMSConnectionFactory
{
    /** Connect to JMS */
    public static Connection connect(final String url) throws JMSException
    {
        return connect(url, ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD);
    }

    /** Connect to JMS */
    public static Connection connect(final String url,
            final String user, final String password) throws JMSException
    {
        // Instead of using JNDI lookup like this...
        //   Context ctx = new InitialContext();
        //   QueueConnectionFactory queueConnectionFactory = 
        //     (QueueConnectionFactory) ctx.lookup("SomeConnectionFactory");
        // ... which requires an appropriate jndi.properties file,
        // we directly use the ActiveMQConnectionFactory.
        final ActiveMQConnectionFactory factory =
            new ActiveMQConnectionFactory(user, password, url);
        return factory.createConnection();
    }
    
    /** Add a listener that is notified about JMS connection issues
     *  to an existing connection.
     *  Connection should not be 'start'ed, yet.
     *  <p>
     *  The implementation depends on the underlying API.
     *  What works for ActiveMQ might not be available for
     *  other implementations, in which case the listener
     *  might never get called.
     *  <p>
     *  For ActiveMQ it's not clear how to track the connection
     *  state dependably. 
     *  For "failover:..." URLs, the initial connection.start() call will
     *  hang until there is a connection established.
     *  On the other hand, it seems as if it will already try to connect
     *  before 'start()' is called, so even when calling addListener() before
     *  start(), the connection might already be up.
     *  We call the JMSConnectionListener for that case, but another
     *  'linkUp' might result from race conditions.
     *  <p>
     *  So in summary this is meant to help track the connection state
     *  and JMS server name, but only for info/debugging; it is not dependable.
     *  
     *  @param connection Connection to monitor
     *  @param listener JMSConnectionListener to notify
     */
    public static void addListener(final Connection connection,
            final JMSConnectionListener listener)
    {
        final ActiveMQConnection amq_connection =
                                               (ActiveMQConnection) connection;
        amq_connection.addTransportListener(new TransportListener()
        {
            public void onCommand(Object cmd)
            {
                // Ignore
                // Looks like one could track almost every send/receive
                // in here
            }

            public void onException(IOException ex)
            {
                // Ignore
            }

            public void transportInterupted()
            {
                listener.linkDown();
            }

            public void transportResumed()
            {
                listener.linkUp(amq_connection.getTransport().getRemoteAddress());
            }
        });
        // Is already connected?
        if (amq_connection.getTransport().isConnected())
            listener.linkUp(amq_connection.getTransport().getRemoteAddress());
    }
}
