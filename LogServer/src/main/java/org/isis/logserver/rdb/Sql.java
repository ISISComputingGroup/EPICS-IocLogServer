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

/**
 * Prepares the SQL 'insert into' statement for adding a message to the database.
 *
 */
public class Sql
{
    private static final String schemaName = "msg_log";
    private static final String old_tableName = "message_old";
    private static final String tableName = "message";
    
    private static final String[] old_dbColumnNames =
    {
    	"createTime",
    	"eventTime",
    	"type",
    	"contents",
    	"clientName",
    	"severity",
    	"clientHost",
    	"applicationId",
    	"repeatCount"
    };
    
    private static final String[] dbColumnNames =
        {
        	"createTime",
        	"eventTime",
        	"type_id",
        	"contents",
        	"clientName_id",
        	"severity_id",
        	"clientHost_id",
        	"application_id",
        	"repeatCount"
        };
    
    private static final String old_dbColumnList;
    private static final String dbColumnList;
    private static final String old_dbParameterList;
    private static final String dbParameterList;
    
    public static final String OLD_INSERT_STATEMENT;
    public static final String INSERT_STATEMENT;
    
    
    static
    {
    	StringBuilder old_cols = new StringBuilder();
    	StringBuilder cols = new StringBuilder();
    	StringBuilder old_params = new StringBuilder();
    	StringBuilder params = new StringBuilder();
    	
    	old_cols.append(old_dbColumnNames[0]);
    	old_params.append("?");

    	for(int i=1; i<old_dbColumnNames.length; ++i)
    	{
    		old_cols.append(", " + old_dbColumnNames[i]);
    		old_params.append(", ?");
    	}
    	
    	cols.append(dbColumnNames[0]);
    	params.append("?");

    	for(int i=1; i<dbColumnNames.length; ++i)
    	{
    		cols.append(", " + dbColumnNames[i]);
    		params.append(", ?");
    	}
    	
    	old_dbColumnList = old_cols.toString();
    	old_dbParameterList = old_params.toString();
    	dbColumnList = cols.toString();
    	dbParameterList = params.toString();
    }
    
    static
    {
		OLD_INSERT_STATEMENT =
	            "INSERT INTO " + schemaName + "." + old_tableName 
	            + " (" + old_dbColumnList + ")"
	            + " VALUES (" + old_dbParameterList + ")";
    }
    
    static
    {
		INSERT_STATEMENT =
	            "INSERT INTO " + schemaName + "." + tableName 
	            + " (" + dbColumnList + ")"
	            + " VALUES (" + dbParameterList + ")";
    }
}
