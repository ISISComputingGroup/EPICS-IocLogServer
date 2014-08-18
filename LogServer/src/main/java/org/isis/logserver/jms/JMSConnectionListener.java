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

/** JMS Connection Listener Interface */
public interface JMSConnectionListener
{
    /** Invoked when the connection to the JMS server is interrupted */
    public void linkDown();

    /** Invoked when the connection to the JMS server is re-connected (resumed)
     *  @param server Name of the JMS server to which we are connected.
     */
    public void linkUp(String server);
}
