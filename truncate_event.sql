 /* log_truncation_event():
 * 
 * This is an event scheduler which periodically calls truncate_message_table() 
 * with parameters of a retention period defined in the DECLARE section as days, hours and minutes.
 * Record deletion is performed in chunks, typically between 1000 and 5000 with a defined sleep 
 * period, typically 1 second. These parameters are tunable and can simply be edited.
 *
 * Deponds on: binary_search_time() and truncate_message_table() procedured.
 *
 * Author: Ian Gillingham, July 2024
 */


DROP EVENT IF EXISTS log_truncation_event;
DELIMITER //

CREATE EVENT IF NOT EXISTS log_truncation_event
	ON SCHEDULE
		-- ############################################################
		-- Modify these parameters to tune the deletion event frequency
		-- ############################################################
		EVERY 1 DAY
        -- Start the recurring event tomorrow at 1 am.
		STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 1 HOUR)
        -- vv TESTING parameters - schedule to trigger 1 minute from now  vv --
		-- STARTS (TIMESTAMP(CURRENT_DATE())+CURRENT_TIME()+ interval 1 minute)
		COMMENT 'Truncate message table at a given time at a defined frequency.'
	DO
		BEGIN
            -- #################################################################################
			--  Modify these parameters to tune the log retention period and chunk size
            -- #################################################################################
			DECLARE retention_period_days INT DEFAULT 30;
			DECLARE retention_period_hours INT DEFAULT 0;
			DECLARE retention_period_minutes INT DEFAULT 0;
            DECLARE log_events BOOL DEFAULT TRUE;
            -- #################################################################################

			DECLARE target_row INT DEFAULT 0;
			DECLARE first_row_id INT DEFAULT 0;
			DECLARE retention_period time;
            DECLARE starttime TIME;
			DECLARE eventName VARCHAR(20);
            DECLARE logMessage VARCHAR(255);
			
            SET GLOBAL event_scheduler = ON;
            -- Attempt to work around 'Lock wait timeout exceeded' errors
			SET GLOBAL transaction_isolation = 'READ-COMMITTED';
            
            SET eventName := 'LogTruncationEvent';
            
			-- Record metric for this event
			SET starttime := now();
                   
			-- Create retention period as time format
            -- MAKETIME(hour, minute, second)
			SELECT MAKETIME(retention_period_hours+(retention_period_days*24), retention_period_minutes, 0) INTO retention_period;

            IF log_events THEN
				SELECT concat(eventName, ': In log_truncation_event : Time: ', now()) INTO logMessage;
				CALL debug_log(logMessage);
			END IF;

			CALL truncate_message_table(retention_period, first_row_id, target_row);
                     
            -- Log the event
			SET @duration := TIMEDIFF(now(), starttime);

            IF log_events THEN
				SELECT concat(eventName, ': DELETED: First row: ', first_row_id, '   Target row: ', target_row, '   Retention period: ', retention_period, '  Time to execute: ', @duration) INTO logMessage;
				CALL debug_log(logMessage);
			END IF;
            
		END //
DELIMITER ;
