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

import org.isis.logserver.message.LogMessage;

public interface ClientMessageParser 
{
	LogMessage parse(String text, LogMessage logMessage); 
}
