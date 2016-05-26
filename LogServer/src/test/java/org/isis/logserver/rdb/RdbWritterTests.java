package org.isis.logserver.rdb;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.isis.logserver.message.LogMessage;
import org.isis.test.utitilies.ResultAndStatement;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class RdbWritterTests {

    @Test
    public void GIVEN_message_to_save_WHEN_succesful_save_THEN_statements_closed() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        int result = writer.saveLogMessageToDb(expectedMessage);

        // Assert
        assertThat(result, is(expectedMessageId));
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }

    @Test
    public void GIVEN_message_to_save_WHEN_sql_exception_on_message_type_THEN_statements_closed() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, true);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        try {
            writer.saveLogMessageToDb(expectedMessage);
            fail("Should have thrown exception");
        }
        catch (SQLException ex) {
            // ok
        }

        // Assert
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }

    @Test
    public void GIVEN_message_to_save_WHEN_sql_exception_on_severity_THEN_statements_closed() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, true);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        try {
            writer.saveLogMessageToDb(expectedMessage);
            fail("Should have thrown exception");
        } catch (SQLException ex) {
            // ok
        }

        // Assert
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }


    @Test
    public void GIVEN_message_to_save_WHEN_sql_exception_on_client_name_THEN_statements_closed() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, true);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        try {
            writer.saveLogMessageToDb(expectedMessage);
            fail("Should have thrown exception");
        } catch (SQLException ex) {
            // ok
        }

        // Assert
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }


    @Test
    public void GIVEN_message_to_save_WHEN_sql_exception_on_client_host_THEN_statements_closed() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, true);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        try {
            writer.saveLogMessageToDb(expectedMessage);
            fail("Should have thrown exception");
        } catch (SQLException ex) {
            // ok
        }

        // Assert
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }


    @Test
    public void GIVEN_message_to_save_WHEN_sql_exception_on_application_id_THEN_statements_closed()
            throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, true);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        try {
            writer.saveLogMessageToDb(expectedMessage);
            fail("Should have thrown exception");
        } catch (SQLException ex) {
            // ok
        }

        // Assert
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }

    @Test
    public void GIVEN_message_to_save_WHEN_new_client_name_THEN_new_client_name_is_saved() throws SQLException {
        // Arrange
        LogMessage expectedMessage = createMessage();
        Integer expectedMessageId = 1;
        String newClientNameId = "3";

        Connection mockConnection = mock(Connection.class);
        ResultAndStatement messageTypePreparedStatement =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_MESSAGE_ID_SQL, false);
        ResultAndStatement severityPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_SEVERITY_ID_SQL, false);
        ResultAndStatement clientNamePreparedStatment =
                dbMockForForEmptyReturnThenRow(mockConnection, RdbWriter.SELECT_CLIENT_NAME_ID_SQL, newClientNameId);
        ResultAndStatement clientNameInsertPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.INSERT_CLIENT_NAME_SQL, false);
        ResultAndStatement clientHostPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_CLIENT_HOST_ID_SQL, false);
        ResultAndStatement applicationPreparedStatment =
                dbMockForForStringReturn(mockConnection, RdbWriter.SELECT_APPLICATION_ID_SQL, false);

        ResultAndStatement insertMessage =
                dbMockForInsertAndKeyReturn(expectedMessageId, mockConnection, Sql.INSERT_STATEMENT);
        RdbWriter writer = new RdbWriter(mockConnection);

        // Act
        int result = writer.saveLogMessageToDb(expectedMessage);

        // Assert
        assertThat(result, is(expectedMessageId));
        insertMessage.verifyIsClosed();
        messageTypePreparedStatement.verifyIsClosed();
        severityPreparedStatment.verifyIsClosed();
        clientNamePreparedStatment.verifyIsClosed();
        clientNameInsertPreparedStatment.verifyIsClosed();
        verify(clientNameInsertPreparedStatment.preparedStatement).executeUpdate();
        clientHostPreparedStatment.verifyIsClosed();
        applicationPreparedStatment.verifyIsClosed();
    }

    /**
     * Create a db mock for a prepared statement and result set which returns a
     * single id for inserted item.
     *
     * @param dbId id of the database item
     * @param mockConnection mock connection
     * @param insertStatement insert sql statement
     * @return a prepared statement and results set mock
     * @throws SQLException the SQL exception
     */
    private ResultAndStatement dbMockForInsertAndKeyReturn(Integer dbId, Connection mockConnection,
            String insertStatement) throws SQLException {
        ResultAndStatement insertMessage = new ResultAndStatement();

        ResultSet insertMessageResultSet = mock(ResultSet.class);
        when(insertMessageResultSet.next()).thenReturn(true);
        when(insertMessageResultSet.getInt(1)).thenReturn(dbId);


        PreparedStatement insertMessagePreparedStatement = mock(PreparedStatement.class);
        when(insertMessagePreparedStatement.getGeneratedKeys()).thenAnswer(insertMessage.new ResultsSetAnswer());

        when(mockConnection.prepareStatement(eq(Sql.INSERT_STATEMENT), anyInt()))
                .thenAnswer(insertMessage.new StatementAnswer());


        insertMessage.preparedStatement = insertMessagePreparedStatement;
        insertMessage.resultSet = insertMessageResultSet;
        return insertMessage;
    }

    /**
     * Create a prepared statement which returns a results set on execute query
     * Also does resource counting on open statement and result set.
     *
     * @param mockConnection the connections on which it is called
     * @param sqlStatement the sql statement that will be issued
     * @param throwOnGetId when get id is called then a sql exception is thrown
     * @return prepared statement and results set
     * @throws SQLException the SQL exception
     */
    private ResultAndStatement dbMockForForStringReturn(Connection mockConnection, String sqlStatement, boolean throwOnGetId)
            throws SQLException {

        ResultAndStatement resultAndStatement = new ResultAndStatement();

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        if (throwOnGetId) {
            when(resultSet.getString("id")).thenThrow(new SQLException());
        } else {
            when(resultSet.getString("id")).thenReturn("1");
        }
        when(resultSet.isBeforeFirst()).thenReturn(true);

        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
         
        when(mockPreparedStatement.executeQuery()).thenAnswer(resultAndStatement.new ResultsSetAnswer());

        when(mockConnection.prepareStatement(sqlStatement)).thenAnswer(resultAndStatement.new StatementAnswer());

        resultAndStatement.preparedStatement = mockPreparedStatement;
        resultAndStatement.resultSet = resultSet;
        return resultAndStatement;
    }

    /**
     * Create a mock prepared statement and results set which contains no rows.
     *
     * @param mockConnection connection to add the mock to
     * @param sqlStatement sql statement
     * @param newId id of the new row
     * @return result and statment
     * @throws SQLException the SQL exception
     */
    private ResultAndStatement dbMockForForEmptyReturnThenRow(Connection mockConnection, String sqlStatement,
            String newId)
            throws SQLException {

        ResultAndStatement resultAndStatement = new ResultAndStatement();

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false, true, false);
        when(resultSet.isBeforeFirst()).thenReturn(false, true, false);

        when(resultSet.getString("id")).thenReturn(newId);

        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockPreparedStatement.executeQuery()).thenAnswer(resultAndStatement.new ResultsSetAnswer());

        when(mockConnection.prepareStatement(sqlStatement)).thenAnswer(resultAndStatement.new StatementAnswer());

        resultAndStatement.preparedStatement = mockPreparedStatement;
        resultAndStatement.resultSet = resultSet;
        return resultAndStatement;
    }

    /**
     * Create a dummy log message.
     *
     * @return the log message
     */
    private LogMessage createMessage() {
        LogMessage expectedMessage = new LogMessage();
        expectedMessage.setApplicationId("app");
        expectedMessage.setClientHost("host");
        expectedMessage.setClientName("name");
        expectedMessage.setContents("message");
        expectedMessage.setCreateTime(Calendar.getInstance());
        expectedMessage.setEventTime(Calendar.getInstance());
        expectedMessage.setType("type");
        return expectedMessage;
    }
}
