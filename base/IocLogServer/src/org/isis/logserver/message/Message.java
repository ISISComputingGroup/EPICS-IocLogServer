package org.isis.logserver.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** A message with some fixed elements (type, date, ...)
 *  as well as arbitrary properties.
 *  @author Kay Kasemir
 */
public class Message
{
	final private static Set<String> property_names = new HashSet<String>();
	final private int id;
	final private String date;
	final private String type;
	final private String name;
	final private String eventtime;
	final private String severity;
	final private HashMap<String, String> properties =
			new HashMap<String, String>();
	
	public Message(final int id, final String date, final String type,
			       final String name, final String eventtime, final String severity)
	{
		this.date = date;
		this.id = id;
		this.type = type;
		this.name = name;
		this.eventtime = eventtime;
		this.severity = severity;
	}

	public void addProperty(final String prop, final String value)
	{
		property_names.add(prop);
		properties.put(prop, value);
	}

	public int getId()
	{
		return id;
	}

	public String getDate()
	{
		return date;
	}

	public String getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}
	
	public String getEventtime()
	{
		return eventtime;
	}

	public String getSeverity()
	{
		return severity;
	}

	/** @return Array of all property names, shared by all messages */
	public static String[] getPropertyNames()
	{
		return property_names.toArray(new String[property_names.size()]);
	}
	
	/** Get property
	 *  @param name Name of the property
	 *  @return Value of the property or <code>null</code>
	 */
	public String getProperty(final String name)
	{
		return properties.get(name);
	}
}
