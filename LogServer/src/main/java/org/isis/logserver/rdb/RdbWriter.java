/*
 * Copyright (C) 2013-2016 Research Councils UK (STFC)
 *
 * This file is part of the Instrument Control Project at ISIS.
 *
 * This code and information are provided "as is" without warranty of any kind,
 * either expressed or implied, including but not limited to the implied
 * warranties of merchantability and/or fitness for a particular purpose.
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
public class RdbWriter {

    private final Connection rdbConnection;
   
    /**
     * Instantiates a new rdb writer.
     *
     * @param rdbConnection the rdb connection
     * @throws SQLException if there is a problem setting up the connection
     */
    public RdbWriter(Connection rdbConnection) throws SQLException {
    	this.rdbConnection = rdbConnection;

        // Handle commits in code, not automatically
        rdbConnection.setAutoCommit(false);
    }
    
    /**
     * Save log a message to the Database.
     *
     * @param message the message
     * @return primary key of the new message
     * @throws SQLException if there is a problem writing the message
     */
    public int saveLogMessageToDb(LogMessage message) throws SQLException {
        int newId;

        // Get the property values
        Timestamp eventTime = new Timestamp(message.getEventTime().getTimeInMillis());
        Timestamp createTime = new Timestamp(message.getCreateTime().getTimeInMillis());

        String messageType = messageTypeId(message.getType());
        String messageSeverity = severityId(message.getSeverity());
        String clientName = clientName(message.getClientName());
        String clientHost = clientHost(message.getClientHost());
        String application = application(message.getApplicationId());

        try (PreparedStatement test =
                rdbConnection.prepareStatement(Sql.INSERT_STATEMENT, Statement.RETURN_GENERATED_KEYS)) {
            int property = 0;
			// Set the property values for the SQL statement
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
            try (ResultSet result = test.getGeneratedKeys()) {

                if (result.next()) {
                    newId = result.getInt(1);
                } else {
                    throw new SQLException("Cannot obtain next message ID");
                }

                rdbConnection.commit();
	        }
        }

        return newId;
    }
    
    static final String SELECT_MESSAGE_ID_SQL = "SELECT id FROM message_type WHERE type=?";
    private String messageTypeId(String type) throws SQLException {
        return getIdForString(type, SELECT_MESSAGE_ID_SQL, "Message Type");
    }

    static final String SELECT_SEVERITY_ID_SQL = "SELECT id FROM message_severity WHERE severity=?";
    private String severityId(String severity) throws SQLException {
        return getIdForString(severity, SELECT_SEVERITY_ID_SQL, "Message Severity");

    }

    static final String SELECT_CLIENT_NAME_ID_SQL = "SELECT id FROM client_name WHERE name=?";
    static final String INSERT_CLIENT_NAME_SQL = "INSERT INTO client_name (name) VALUES (?)";
    private String clientName(String name) throws SQLException {
        return getIdCreateIfNotFound(name, SELECT_CLIENT_NAME_ID_SQL, INSERT_CLIENT_NAME_SQL);
    }

    static final String SELECT_CLIENT_HOST_ID_SQL = "SELECT id FROM client_host WHERE name=?";
    private static final String INSERT_CLIENT_HOST_SQL = "INSERT INTO client_host (name) VALUES (?)";
    private String clientHost(String name) throws SQLException {
        return getIdCreateIfNotFound(name, SELECT_CLIENT_HOST_ID_SQL, INSERT_CLIENT_HOST_SQL);
    }

    static final String SELECT_APPLICATION_ID_SQL = "SELECT id FROM application WHERE name=?";
    private static final String INSERT_APPLICATION_SQL = "INSERT INTO application (name) VALUES (?)";
    private String application(String name) throws SQLException {
        return getIdCreateIfNotFound(name, SELECT_APPLICATION_ID_SQL, INSERT_APPLICATION_SQL);
    }

    /**
     * Get an id for a value from the database using a sql statement. If it
     * doesn't exist create it
     * 
     * @param value the value to get an id for
     * @param sqlSelectId the SQL to select the id for that value
     * @param sqlInsertItem the SQL to insert the value into the database
     * @return the id of the newly created value
     * @throws SQLException if anything goes wrong reading or writting to the
     *             database
     */
    private String getIdCreateIfNotFound(String value, String sqlSelectId, String sqlInsertItem) throws SQLException {
        try (PreparedStatement getClientName = rdbConnection.prepareStatement(sqlSelectId)) {
            getClientName.setString(1, value);

            String id = getIdForValueOrNullWithPreparedStatement(getClientName);
            if (id == null) {
                try (PreparedStatement createMessType = rdbConnection.prepareStatement(sqlInsertItem)) {
                    createMessType.setString(1, value);
                    createMessType.executeUpdate();
                }
                id = getIdForValueOrNullWithPreparedStatement(getClientName);
                if (id == null) {
                    rdbConnection.rollback();
                    throw new SQLException("Tried to insert '" + value + "' but this failed.");
                }
                rdbConnection.commit();
            }
            return id;
    	}
    }
    
    /**
     * Get an Id for a value from the database.
     * 
     * @param value the value to find
     * @param sqlStatement the SQL statement to find the value
     * @param valueType what the value is
     * @return the id for the value
     * @throws SQLException if the value can not be found or there is a problem
     *             accessing the database
     */
    private String getIdForString(String value, String sqlStatement, String valueType) throws SQLException {

        try (PreparedStatement getMessType = rdbConnection.prepareStatement(sqlStatement)) {
            getMessType.setString(1, value);
            String s = getIdForValueOrNullWithPreparedStatement(getMessType);

            if (s == null) {
                throw new SQLException(valueType + " not recognised");
            }
            return s;
        }
    }

    /**
     * Get an id for a value with a prepared statement.
     * 
     * @param value the value to find
     * @param sqlStatement the prepared statement to us
     * @return the id or null if it does not exist
     * @throws SQLException if the database can not be accessed
     */
    private String getIdForValueOrNullWithPreparedStatement(PreparedStatement sqlStatement) throws SQLException {
        String s = null;
        try (ResultSet messageType = sqlStatement.executeQuery()) {
            while (messageType.next()) {
                s = messageType.getString("id");
            }
        }
        return s;
    }
    
}
