package org.isis.logserver.parser;

import org.isis.logserver.message.LogMessage;

public abstract class ClientMessageParser 
{
	abstract public LogMessage parse(String text); 
}
