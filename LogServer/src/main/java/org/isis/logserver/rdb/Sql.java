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
    private static final String tableName = "message";
    
    private static final String[] dbColumnNames =
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
    
    private static final String dbColumnList;
    private static final String dbParameterList;
    
    public static final String INSERT_STATEMENT;
    
    
    static
    {
    	StringBuilder cols = new StringBuilder();
    	StringBuilder params = new StringBuilder();
    	
    	cols.append(dbColumnNames[0]);
    	params.append("?");

    	for(int i=1; i<dbColumnNames.length; ++i)
    	{
    		cols.append(", " + dbColumnNames[i]);
    		params.append(", ?");
    	}
    	
    	dbColumnList = cols.toString();
    	dbParameterList = params.toString();
    }
    
    static
    {
		INSERT_STATEMENT =
	            "INSERT INTO " + schemaName + "." + tableName 
	            + " (" + dbColumnList + ")"
	            + " VALUES (" + dbParameterList + ")";
    }
}
