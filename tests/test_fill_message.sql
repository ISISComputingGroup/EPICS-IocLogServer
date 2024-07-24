/* This SQL script will insert dummy log records into the message table.
 * Message records will be assigned a createDate field value at one hour intervals
 * over the given period (period_days).
 * Ian Gillingham, July 2024.
 */
USE msg_log;
drop procedure if exists fillMessage;
DELIMITER //  
CREATE PROCEDURE fillMessage()   
BEGIN
DECLARE start_datetime DATETIME DEFAULT NOW();
DECLARE period_days INT DEFAULT 60;
DECLARE period_hours INT DEFAULT period_days*24;
DECLARE hr INT DEFAULT 0;
TRUNCATE message; -- empty the message table
WHILE (hr <= period_hours) DO
	-- Create a datetime entry from the 
	SET @ct = DATE_ADD(start_datetime, INTERVAL hr HOUR);
    INSERT INTO message (createTime, eventTime, type_id, contents, severity_id, clientHost_id, application_id, repeatCount) 
		values (@ct, now(), 3, "Test content", 1, 1, 1, 0);
	-- Establish the next hour
    SET hr = hr+1;
END WHILE;
END;
//  
DELIMITER ;
CALL fillMessage(); 
