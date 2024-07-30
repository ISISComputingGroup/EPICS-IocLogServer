DROP PROCEDURE IF EXISTS truncate_message_table;

DELIMITER //

CREATE PROCEDURE truncate_message_table(IN p_retention_period time, OUT p_first_row_id INT, OUT p_row_number INT)
COMMENT "/* truncate_message_table():
 * 
 * This is a SQL procedure facilitates simply and quickly deleting records 
 * between the earliest and up to the target time record number,
 * with just a time period of the retention period remaining in the table.
 *
 * Parameters:
 *		IN p_retention_period time -- The retention period as a time type 
 *									  (e.g. '336:00:00' == 2 wks)
 *									  Note that this is limited in SQL to '838:59:59', about 35 days
 *      OUT p_first_row_id   -- The id value of the earliest row in the table (zeroth row).
 *		OUT p_row_number INT -- The returned record number in the table which is closest to the target time. 
 *  							This will be a reference to a variable provided by the calling scope.
 *
 * Deponds on: binary_search_time()
 *
 * Preconditions: The createTime field values in the table must be time-series monotonic.
 *
 * Author: Ian Gillingham, July 2024
 */"

		BEGIN
            -- DECLARE chunk_size INT DEFAULT 3000; -- impirically approx 0.5 sec for deletion on dev machine
            DECLARE chunk_size INT DEFAULT 1000; -- throttle back for user machine
            DECLARE sleep_period_sec INT DEFAULT 1;
            DECLARE log_events BOOL DEFAULT TRUE;
            -- #################################################################################

			DECLARE target_row INT DEFAULT 0;
			DECLARE first_row_id INT DEFAULT 0;
            DECLARE starttime TIME;
			DECLARE eventName VARCHAR(40);
            DECLARE logMessage VARCHAR(255);
            DECLARE duration TIME;
			
            SET GLOBAL event_scheduler = ON;
            -- Attempt to work around 'Lock wait timeout exceeded' errors
			SET autocommit = 1;         
			-- SET GLOBAL transaction_isolation = 'READ-COMMITTED';
            
            SET eventName := 'truncate_message_table()';
            
			-- Record metric for this event
			SET starttime := now();
                   
            IF log_events THEN
				-- Empty the debug log table from previous runs
                TRUNCATE debug_messages;
				SELECT concat(eventName, ': In truncate_message_table() : Time: ', now()) INTO logMessage;
				CALL debug_log(logMessage);
			END IF;
            
            
            -- Find the row closest to the retention period offset from last row.
			CALL binary_search_time(p_retention_period, first_row_id, target_row);

			SELECT concat('truncate_message_table(): Returned from binary search. log_events = ', log_events) INTO logMessage;
			CALL debug_log(logMessage);
            -- INSERT INTO debug_messages (debug_message) VALUES (logMessage);
            
            IF log_events THEN
			    -- Log the event
				SET duration = TIMEDIFF(now(), starttime);
                SET @durstr = "";
                SELECT DATE_FORMAT(duration, '%T') INTO @durstr;
				SELECT concat(eventName, ': binary_search_time() completed: First row: ', first_row_id, '   Target row: ', target_row, '   Retention period: ', p_retention_period) INTO logMessage;
				CALL debug_log(logMessage);
				-- SELECT concat(eventName, ': Truncation completed:  Time to execute: ', @durstr) INTO logMessage;
				-- SELECT concat(eventName, ': binary_search_time() completed: First row: ', first_row_id, '   Target row: ', target_row, '   Retention period: ', p_retention_period) INTO logMessage;
				-- CALL debug_log(logMessage);
			END IF;

            -- Delete all rows from oldest to retention period offset.
			-- Do it in chunks, with a sleep between chunks to play nicely
			SET @a = first_row_id;
			chunk: LOOP
				-- Determine the end id of this chunk to delete
				SELECT id FROM message WHERE id >= @a ORDER BY id LIMIT chunk_size,1 INTO @z;
				IF @z IS NULL OR (@z + chunk_size) >= target_row THEN
					LEAVE chunk;  -- last chunk is less than chunk size
				END IF;
				-- Delete the chuck
				DELETE FROM message WHERE id >= @a AND id <  @z AND id <target_row;
				-- Iterate again starting at the next chunk
				SET @a = @z;
				DO SLEEP(sleep_period_sec);  -- play nicely!
			END LOOP chunk;
			-- Last chunk: We have a chunk with fewer rows than the defined chunk size,
			-- so just delete the remaining rows up to the defined last row of the overall deletion.
			DELETE FROM message WHERE id BETWEEN @a AND target_row;
            
            -- Set output values
            SET p_first_row_id = first_row_id;
            SET p_row_number = target_row;
            
            -- Log the event
			SET @duration := TIMEDIFF(now(), starttime);

			IF log_events THEN
	            SELECT concat(eventName, ': DELETED: First row: ', first_row_id, '   Target row: ', target_row, '   Retention period: ', p_retention_period, '  Time to execute: ', @duration) INTO logMessage;
				CALL debug_log(logMessage);
			END IF;
            SELECT logMessage;
		END //
DELIMITER ;
