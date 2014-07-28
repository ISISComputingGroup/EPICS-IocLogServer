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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.isis.logserver.message.LogMessage;

/**
 * Writes log messages to the database.
 *
 */
public class RdbWriter 
{
    final Connection rdbConnection;
   
    public RdbWriter(Connection rdbConnection) throws Exception
    {       
    	this.rdbConnection = rdbConnection;

        // Handle commits in code, not automatically
        rdbConnection.setAutoCommit(false);
    }
    
    public int saveLogMessageToDb(LogMessage message) throws Exception
    {
        PreparedStatement preparedInsertStatement =
        		rdbConnection.prepareStatement(Sql.INSERT_STATEMENT, Statement.RETURN_GENERATED_KEYS);
		
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
        final ResultSet result = preparedInsertStatement.getGeneratedKeys();
        int newId;
        
        if (result.next())
        {
        	newId = result.getInt(1);
        }
        else
        {
            throw new Exception("Cannot obtain next message ID");
        }
        
        // Commit the result
        rdbConnection.commit();
        
        // Close connections
        result.close();
        preparedInsertStatement.close();

        return newId;
    }
}
