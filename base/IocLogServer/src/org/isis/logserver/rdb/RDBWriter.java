package org.isis.logserver.rdb;

/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Class that writes LogMessages to the RDB
 *  @author Kay Kasemir, Katia Danilova
 */
@SuppressWarnings("nls")
public class RDBWriter
{
	private static final int MAX_VALUE_LENGTH = 100;

    private static final int MAX_NAME_LENGTH = 80;

    /** RDB Utility */
    final Connection rdbConnection;
    final boolean isOracle;

    /** SQL statements */
    final private SQL sql;

    /** Map of Property IDs, mapping property name to numeric ID */
    final private HashMap<String, Integer> properties =
    	new HashMap<String, Integer>();

    /** Lazily initialized statement */
    private PreparedStatement next_message_id_statement;

    /** Lazily initialized statement */
    private PreparedStatement insert_message_statement;

    /** Lazily initialized statement */
    private PreparedStatement insert_property_statement;

    /** Constructor
     *  @param url RDB URL
     *  @param schema Schema name or ""
     *  @throws Exception on error
     */
    public RDBWriter(final String schema, Connection rdbConnection) throws Exception
    {       
    	this.rdbConnection = rdbConnection;
    	this.isOracle = false;
    	
        boolean isOracle = false;
        sql = new SQL(isOracle, schema);

        // Handle commits in code, not automatically
        rdbConnection.setAutoCommit(false);

        if (sql.select_next_message_id != null)
            next_message_id_statement =
            		rdbConnection.prepareStatement(sql.select_next_message_id);

        insert_message_statement =
        		rdbConnection.prepareStatement(sql.insert_message_id_datum_type_name_severity,
                    Statement.RETURN_GENERATED_KEYS);

        insert_property_statement =
        		rdbConnection.prepareStatement(sql.insert_message_property_value);
    }

    /** Get numeric ID of a property, using either the local cache
     *  or querying the RDB.
     *  @param property_name
     *  @return Numeric property ID
     *  @throws Exception on error
     */
    private int getPropertyType(final String property_name) throws Exception
    {
    	// First try cache
    	final Integer int_id = properties.get(property_name);
    	if (int_id != null)
    		return int_id.intValue();
    	// Perform RDB query

    	PreparedStatement statement =
    			rdbConnection.prepareStatement(sql.select_property_id_by_name);
        statement.setString(1, property_name);
        try
        {
            final ResultSet result = statement.executeQuery();
            if (result.next())
            {	// Add to cache
            	final int id = result.getInt(1);
                properties.put(property_name, Integer.valueOf(id));
				return id;
            }
        }
        finally
        {
            statement.close();
        }
        // Insert unknown message property: Get next ID
        // This does not use a sequence!
        // Fundamentally, there is a small chance that multiple instances
        // of this program will try to create duplicate property entries.
        // In reality, it probably doesn't matter.
        // Since we wrap the whole one-message-write into a transaction,
        // the worst case would be one lost message because of a property ID clash.
        statement = rdbConnection.prepareStatement(sql.select_next_property_id);
        int next_id;
        try
        {
            final ResultSet result = statement.executeQuery();
            if (result.next())
            	next_id = result.getInt(1);
            else
            	throw new Exception("Cannot get new ID for " + property_name);
        }
        finally
        {
            statement.close();
        }
        statement = rdbConnection.prepareStatement(sql.insert_property_id);
        statement.setInt(1, next_id);
        statement.setString(2, property_name);
        try
        {
        	statement.executeUpdate();
        }
        finally
        {
            statement.close();
        }
        System.out.println(
    		"Inserted unkown Message Property " + property_name + " as ID "
    		+ next_id);
        // Add to cache
    	properties.put(property_name, Integer.valueOf(next_id));
		return next_id;
	}

    /** Close the RDB connection */
    public void close()
    {
        if (next_message_id_statement != null)
        {
            try
            {
                next_message_id_statement.close();
            }
            catch (Exception ex)
            { /* Ignore */ }
        }
        if (insert_message_statement != null)
        {
            try
            {
                insert_message_statement.close();
            }
            catch (Exception ex)
            { /* Ignore */ }
        }
        if (insert_property_statement != null)
        {
            try
            {
                insert_property_statement.close();
            }
            catch (Exception ex)
            { /* Ignore */ }
        }
    }


    /** Write log message to RDB from write.jsp
     *  @param severity String
     *  @param name String
     *  @param map Map
     *  @throws Exception on error
     */
    @SuppressWarnings("rawtypes")
    public long write(final String severity, final String type, final String name, final Map map) throws Exception
    {    	    	
		long message_id;
        try
        {
    		message_id = insertMessage(type, name, severity);

    		Iterator it = map.entrySet().iterator();
		    while (it.hasNext())
		    {
		        Map.Entry pairs = (Map.Entry)it.next();
		        // System.out.println(pairs.getKey() + " = " + pairs.getValue());

		        if (type.equals(pairs.getKey()) ||
		               	   name.equals(pairs.getKey()) ||
		               	   severity.equals(pairs.getKey()))
		        {
		        	continue;
		        }
		        
		        batchProperty(message_id, pairs.getKey().toString(), pairs.getValue().toString());
		    }
            insert_property_statement.executeBatch();
            rdbConnection.commit();
        }
        catch (Exception ex)
        {
        	rdbConnection.rollback();
            throw ex;
        }

        return message_id;
    }


    /** Insert a new message
     *  @param type  Message type
     *  @param name Primary name (PV name, ...) to which the message refers
     *  @param severity Message severity
     *  @return ID of the new message row
     *  @throws Exception on error
     */
    private long insertMessage(
    		final String type, String name,
    		final String severity) throws Exception
    {

        long message_id = -1;
        if (isOracle)
        {

            final ResultSet result = next_message_id_statement.executeQuery();
            if (result.next())
            {
               message_id = result.getInt(1);
            }
            else
                throw new Exception("Cannot obtain next message ID");
            result.close();
            insert_message_statement.setLong(5, message_id);
        }
        // else: Depend on AUTO_INCREMENT resp. SERIAL for new ID, then read it after insert

        // Insert the main message
        final Calendar now = Calendar.getInstance();
        insert_message_statement.setTimestamp(1, new Timestamp(now.getTimeInMillis()));
        insert_message_statement.setString(2, type);
        // Overcome RDB limitations
        if (name == null)
            name = "";
        else if (name.length() > MAX_NAME_LENGTH)
        {
            System.out.println(
                "Limiting NAME = '" + name + "' to " + MAX_NAME_LENGTH);
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        insert_message_statement.setString(3, name);
        insert_message_statement.setString(4, severity);
        final int rows = insert_message_statement.executeUpdate();
        if (rows != 1)
            throw new Exception("Inserted " + rows + " instead of 1 Message");

        if (!isOracle)
        {
            // Read auto-assigned unique message ID
            final ResultSet result = insert_message_statement.getGeneratedKeys();
            if (result.next())
            {
               message_id = result.getInt(1);
            }
            else
                throw new Exception("Cannot obtain next message ID");
            result.close();
        }

//        Calendar calendar = Calendar.getInstance();
//        java.sql.Timestamp timeStamp = new java.sql.Timestamp(calendar.getTime().getTime());
//        System.out.println("Message " + message_id + ":");
//        System.out.println("  TYPE          : " + type);
//        System.out.println("  DATUM         : " + timeStamp);
//        System.out.println("  NAME          : " + name);
//        System.out.println("  SEVERITY      : " + severity);

        return message_id;
    }

    /** Insert a property, add content to a message
     *  @param message_id ID of message to which this property belongs
     *  @param property_id ID of the property type
     *  @param value Value of the property
     *  @throws Exception on error
     */
    private boolean batchProperty(final long message_id,
            final String property, String value) throws Exception
    {
        // Don't bother to insert empty properties
        if (value == null  ||  value.isEmpty())
            return false;

        final int property_id = getPropertyType(property);

        insert_property_statement.setLong(1, message_id);
        insert_property_statement.setInt(2, property_id);
        // Overcome RDB limitations
        if (value.length() > MAX_VALUE_LENGTH)
        {
            System.out.println(
                "(RDB) Limiting " + property + " = '" + value + "' to " + MAX_VALUE_LENGTH);
            value = value.substring(0, MAX_VALUE_LENGTH);
        }
        insert_property_statement.setString(3, value);
        insert_property_statement.addBatch();

        // System.out.println(String.format("  %-14s: %s", property, value));
        return true;
    }

    /** Save to RDB message property (for example REPEATED: how many times this message was repeated)
     *  @param message_id long
     *  @param property_name String
     *  @param property_value String
     *  @throws Exception on error
     */
    public void addProperty(final long message_id, final String property_name, final String property_value) throws Exception
    {
    	final String sql=
    		" insert into msg_log.message_content (message_id, msg_property_type_id, value) " +
    		" values(?, ( select id from msg_log.msg_property_type where name=? ), ?)";

    	final PreparedStatement statement =
    			rdbConnection.prepareStatement(sql);
        try
        {
        	statement.setLong(1, message_id);
        	statement.setString(2, property_name);
        	statement.setString(3, property_value);

        	//final ResultSet result = statement.executeQuery();
    		//result.close();
        	statement.executeUpdate();
        	rdbConnection.commit();
            
            System.out.println(">>>ADD PROPERTY " + property_name + ": " + property_value);
        }
        catch (Exception ex)
        {
        	rdbConnection.rollback();
            System.out.println("Message " + message_id + ": Failed to add property '" + property_name + "' = '" + property_value + "'");
            throw ex;
        }
    }

    /** Update in RDB message property (for example REPEATED: how many times this message was repeated)
     *  @param message_id long
     *  @param property_name String (REPEATED)
     *  @param property_value String (how many times)
     *  @throws Exception on error
     */
    public void updateProperty(final long message_id, final String property_name, final String property_value) throws Exception
    {
    	final String sql=
    		" update msg_log.message_content "+
    		" set value=? "+
    		" where message_id=? and msg_property_type_id= "+
    		" (select id from msg_log.msg_property_type where name=?) ";


    	final PreparedStatement statement =
    			rdbConnection.prepareStatement(sql);
        try
        {
        	statement.setString(1, property_value);
        	statement.setLong(2, message_id);
        	statement.setString(3, property_name);

        	final ResultSet result = statement.executeQuery();
    		result.close();
    		rdbConnection.commit();
        }
        catch (Exception ex)
        {
        	rdbConnection.rollback();
            throw ex;
        }
    }
}
