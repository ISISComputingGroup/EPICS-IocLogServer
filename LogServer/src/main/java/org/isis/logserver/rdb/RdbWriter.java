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
package org.isis.logserver.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.isis.logserver.message.LogMessage;

/**
 * Writes log messages to the database.
 *
 */
public class RdbWriter 
{
    final Connection rdbConnection;
   
    public RdbWriter(Connection rdbConnection) throws SQLException
    {       
    	this.rdbConnection = rdbConnection;

        // Handle commits in code, not automatically
        rdbConnection.setAutoCommit(false);
    }
    
    public int saveLogMessageToDb(LogMessage message) throws SQLException
    {
    	PreparedStatement preparedInsertStatement = null;
    	ResultSet result = null;
    	int newId;
		
        try
        {        	
            preparedInsertStatement = rdbConnection.prepareStatement(Sql.OLD_INSERT_STATEMENT, Statement.RETURN_GENERATED_KEYS);
            
	        int property = 0;
	
	        Timestamp eventTime = new Timestamp(message.getEventTime().getTimeInMillis());
	        Timestamp createTime = new Timestamp(message.getCreateTime().getTimeInMillis());
	
	        preparedInsertStatement.setTimestamp(++property, eventTime);
	        preparedInsertStatement.setTimestamp(++property, createTime);
	        	
	        preparedInsertStatement.setString(++property, message.getType());
	        preparedInsertStatement.setString(++property, message.getContents());
	        preparedInsertStatement.setString(++property, message.getClientName());
	        preparedInsertStatement.setString(++property, message.getSeverity());
	        preparedInsertStatement.setString(++property, message.getClientHost());
	        preparedInsertStatement.setString(++property, message.getApplicationId());
	        
	        // Repeat count
	        preparedInsertStatement.setInt(++property, 1);
	
	        // Add message to Db
	        preparedInsertStatement.executeUpdate();
	        
	        // Read auto-assigned unique message ID
	        result = preparedInsertStatement.getGeneratedKeys();
	        
	        if (result.next())
	        {
	        	newId = result.getInt(1);
	        }
	        else
	        {
	            throw new SQLException("Cannot obtain next message ID");
	        }
	        
	        // Commit the result
	        rdbConnection.commit();
	        
	        // Test for writing to the new schema too - use that ID
	        
	        String messageType = message_type_id(message.getType());
	        String messageSeverity = severity_id(message.getSeverity());
	        String clientName = client_name(message.getClientName());
	        String clientHost = client_host(message.getClientHost());
	        String application = application(message.getApplicationId());
	        
	        PreparedStatement test = rdbConnection.prepareStatement(Sql.INSERT_STATEMENT, Statement.RETURN_GENERATED_KEYS);
	        property = 0;
	
	        test.setTimestamp(++property, eventTime);
	        test.setTimestamp(++property, createTime);
	        	
	        test.setString(++property, messageType);
	        test.setString(++property, message.getContents());
	        test.setString(++property, clientName);
	        test.setString(++property, messageSeverity);
	        test.setString(++property, clientHost);
	        test.setString(++property, application);
	        
	        // Repeat count
	        test.setInt(++property, 1);
	
	        // Add message to Db
	        test.executeUpdate();
	        
	        // Read auto-assigned unique message ID
	        result = test.getGeneratedKeys();
	        
	        if (result.next())
	        {
	        	newId = result.getInt(1);
	        }
	        else
	        {
	            throw new SQLException("Cannot obtain next message ID");
	        }
	        
	        rdbConnection.commit();	        
        }
        finally
        {
	        // Close connections
        	if(result != null) {
        		result.close();
        	}
        	
        	if(preparedInsertStatement != null) {
        		preparedInsertStatement.close();
        	}
        }

        return newId;
    }
    
    private static final String get_message_type_id = "SELECT id FROM message_type WHERE type=?";
    private String message_type_id(String type) throws SQLException
    {
    	PreparedStatement getMessType = rdbConnection.prepareStatement(get_message_type_id);
        getMessType.setString(1, type);
        ResultSet messageType = getMessType.executeQuery();
        String s = "value";
        
        if (!messageType.isBeforeFirst()){
        	throw new SQLException("Message Type not recognised");
        }
        else {
        	while(messageType.next()){
        		s = messageType.getString("id");
        	}
        }
        return s;
    }
    
    private static final String get_severity_id = "SELECT id FROM message_severity WHERE severity=?";
    private String severity_id(String severity) throws SQLException
    {
    	PreparedStatement getSevType = rdbConnection.prepareStatement(get_severity_id);
    	getSevType.setString(1, severity);
        ResultSet messageSeverity = getSevType.executeQuery();
        String s = "value";
        
        if (!messageSeverity.isBeforeFirst()){
        	throw new SQLException("Message Severity not recognised");
        }
        else {
        	while(messageSeverity.next()){
        		s = messageSeverity.getString("id");
        	}
        }
        return s;
    }
    
    private static final String get_client_name_id = "SELECT id FROM client_name WHERE name=?";
    private static final String create_client_name = "INSERT INTO client_name (name) VALUES (?)";
    private String client_name(String name) throws SQLException
    {
    	PreparedStatement getClientName = rdbConnection.prepareStatement(get_client_name_id);
    	getClientName.setString(1, name);
        ResultSet clientName = getClientName.executeQuery();
        String s = "value";
        
        if (!clientName.isBeforeFirst()){
        	PreparedStatement createMessType = rdbConnection.prepareStatement(create_client_name);
        	createMessType.setString(1, name);
        	createMessType.executeUpdate();
        	clientName = getClientName.executeQuery();
        	if (clientName.isBeforeFirst()){
        		s = clientName.getString("id");
        	}
        	rdbConnection.commit();
        }
        else {        
        	while(clientName.next()){
        		s = clientName.getString("id");
        	}
        }
        return s;
    }
    
    private static final String get_client_host_id = "SELECT id FROM client_host WHERE name=?";
    private static final String create_client_host = "INSERT INTO client_host (name) VALUES (?)";
    private String client_host(String name) throws SQLException
    {
    	PreparedStatement getClientHost = rdbConnection.prepareStatement(get_client_host_id);
    	getClientHost.setString(1, name);
        ResultSet clientHost = getClientHost.executeQuery();
        String s = "value";
        
        if (!clientHost.isBeforeFirst()){
        	PreparedStatement createMessType = rdbConnection.prepareStatement(create_client_host);
        	createMessType.setString(1, name);
        	createMessType.executeUpdate();
        	clientHost = getClientHost.executeQuery();
        	if (clientHost.isBeforeFirst()){
        		s = clientHost.getString("id");
        	}
        	rdbConnection.commit();
        }
        else {        
        	while(clientHost.next()){
        		s = clientHost.getString("id");
        	}
        }
        return s;
    }
    
    private static final String get_application_id = "SELECT id FROM application WHERE name=?";
    private static final String create_application = "INSERT INTO application (name) VALUES (?)";
    private String application(String name) throws SQLException
    {
    	PreparedStatement getApplication = rdbConnection.prepareStatement(get_application_id);
    	getApplication.setString(1, name);
        ResultSet application = getApplication.executeQuery();
        String s = "value";
        
        if (!application.isBeforeFirst()){
        	PreparedStatement createMessType = rdbConnection.prepareStatement(create_application);
        	createMessType.setString(1, name);
        	createMessType.executeUpdate();
        	application = getApplication.executeQuery();
        	if (application.isBeforeFirst()){
        		s = application.getString("id");
        	}
        	rdbConnection.commit();
        }
        else {        
        	while(application.next()){
        		s = application.getString("id");
        	}
        }
        return s;
    }
}
