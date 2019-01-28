package org.isis.logserver.parser;

import org.isis.logserver.message.LogMessage;

public class NullMessageParser implements ClientMessageParser {

	@Override
	public LogMessage parse(String text, LogMessage logMessage) {
		return logMessage;
	}

}
