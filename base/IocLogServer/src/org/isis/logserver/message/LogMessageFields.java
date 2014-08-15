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
package org.isis.logserver.message;

/**
 * Names for log message fields as used by in the SQL database and XML representations
 */
class LogMessageFieldTags
{
	static final String TAG_CONTENTS = "contents";
	static final String TAG_SEVERITY = "severity";
	static final String TAG_EVENTTIME = "eventTime";
	static final String TAG_CREATETIME = "createTime";
	static final String TAG_CLIENTNAME = "clientName";
	static final String TAG_CLIENTHOST = "clientHost";
	static final String TAG_TYPE = "type";
	static final String TAG_APPID = "applicationId";
}

public enum LogMessageFields 
{
	CONTENTS ("Content", LogMessageFieldTags.TAG_CONTENTS, 500),
	SEVERITY ("Severity", LogMessageFieldTags.TAG_SEVERITY, 100),
	EVENT_TIME ("Event Time", LogMessageFieldTags.TAG_EVENTTIME, 200),
	CREATE_TIME ("Create Time", LogMessageFieldTags.TAG_CREATETIME, 0),
	CLIENT_NAME ("Sender", LogMessageFieldTags.TAG_CLIENTNAME, 150),
	CLIENT_HOST ("Sender Host", LogMessageFieldTags.TAG_CLIENTHOST, 0),
	TYPE ("Type", LogMessageFieldTags.TAG_TYPE, 100),
	APPLICATION_ID ("Application ID", LogMessageFieldTags.TAG_APPID, 0);
	
	private String displayName;
	private String tagName;
	private int defaultColumnWidth;
	
	private LogMessageFields(String displayName, String tagName, int defaultColumnWidth)
	{
		this.displayName = displayName;
		this.tagName = tagName;
		this.defaultColumnWidth = defaultColumnWidth;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getTagName() {
		return tagName;
	}

	public int getDefaultColumnWidth() {
		return defaultColumnWidth;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
	public static LogMessageFields getFieldByTagName(String tagName) throws IllegalArgumentException
	{
		switch(tagName)
		{
			case LogMessageFieldTags.TAG_CONTENTS:
				return CONTENTS;
			case LogMessageFieldTags.TAG_SEVERITY:
				return SEVERITY;
			case LogMessageFieldTags.TAG_EVENTTIME:
				return EVENT_TIME;
			case LogMessageFieldTags.TAG_CREATETIME:
				return CREATE_TIME;	
			case LogMessageFieldTags.TAG_CLIENTNAME:
				return CLIENT_NAME;
			case LogMessageFieldTags.TAG_CLIENTHOST:
				return CLIENT_HOST;
			case LogMessageFieldTags.TAG_TYPE:
				return TYPE;
			case LogMessageFieldTags.TAG_APPID:
				return APPLICATION_ID;
			default:
				throw new IllegalArgumentException("Unknown log message field tag: " + tagName);
		}
	}
}