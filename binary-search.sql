DROP PROCEDURE IF EXISTS binary_search_time;

DELIMITER //

CREATE PROCEDURE binary_search_time(IN p_retention_period time, OUT p_first_row_id INT, OUT p_row_number INT)
COMMENT "/* binary_search_time():
 * 
 * This is a SQL procedure to determine the record number in a table which has
 * the closest createTime field value to that calculated by subtracting the given
 * retention period from the createTime value of the last record. This will facilitate 
 * simply and quickly deleting records between zero up to the target time record number,
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
 * Preconditions: The createTime field values in the table must be time-series monotonic.
 *
 * Author: Ian Gillingham, July 2024
 */"
	BEGIN
		DECLARE first_row INT;
		DECLARE last_row INT;
		DECLARE mid_row INT;
		DECLARE lte_tree INT;
        DECLARE first_time TIMESTAMP;
        DECLARE last_time TIMESTAMP;
        DECLARE mid_time TIMESTAMP;
        DECLARE target_time TIMESTAMP;
        DECLARE dbg_txt VARCHAR(255);
        DECLARE proc_start_time TIMESTAMP;
        DECLARE proc_last_sleep_time TIMESTAMP;
        DECLARE proc_elapsed_sec INT;
        DECLARE proc_elapsed_sec_since_sleep INT;

		SET proc_elapsed_sec = 0;
        SET proc_elapsed_sec_since_sleep = 0;
		SET proc_last_sleep_time = now();
		SET proc_start_time = now();
		SELECT concat('binary_search_time(): Time now: ', proc_start_time, '  Retention period: ',p_retention_period) INTO dbg_txt;
		CALL debug_log(dbg_txt);

		-- Get the initial first and last row IDs
		SELECT id FROM message ORDER BY id DESC LIMIT 1 INTO last_row;

		SELECT concat('binary_search_time(): Get the initial last row ID: last_row: ',last_row) INTO dbg_txt;
		CALL debug_log(dbg_txt);

		SELECT id FROM message ORDER BY id LIMIT 1 INTO first_row;
        SET p_first_row_id = first_row; -- remember this so it can be returned from the call.

		SELECT concat('binary_search_time(): Get the initial first and last row IDs: first_row: ',first_row,'  last_row: ',last_row) INTO dbg_txt;
		CALL debug_log(dbg_txt);
		
        -- Get the corresponding first and last timestamps
		SELECT createTime from message where id = last_row limit 1 INTo last_time;
        SET target_time = ADDTIME(last_time, -p_retention_period);

		-- Do a binary search for the target time until we are within 2 rows of a solution.
        -- The target time is calculated and may not (probably won't) exist as a field value
        -- so need to be prepared to resolve to the rows straddling the target value.
        WHILE last_row - first_row >= 2 DO
			select time_to_sec(timediff(now(), proc_start_time)) INTO proc_elapsed_sec;
            
            select time_to_sec(timediff(now(), proc_last_sleep_time)) INTO proc_elapsed_sec_since_sleep;
            
            IF proc_elapsed_sec_since_sleep > 4 THEN
				SELECT concat('binary_search_time(): SLEEP 1 sec: ') INTO dbg_txt;
				CALL debug_log(dbg_txt);
				DO SLEEP(1);  -- play nicely!
                SET proc_last_sleep_time = now();
			END IF;
            
			SELECT concat('binary_search_time(): Elapsed time: ', proc_elapsed_sec,'  WHILE last_row - first_row >= 2 DO...: first_row: ',first_row,'  last_row: ',last_row) INTO dbg_txt;
			CALL debug_log(dbg_txt);
			SELECT createTime from message where id = first_row limit 1 INTO first_time;
			SELECT createTime from message where id = last_row limit 1 INTO last_time;
		
			-- Calculate the mid-point row between first and last
			SET mid_row = first_row + abs((last_row - first_row)/2);
            -- get the createTime value at the mid row.
			SELECT createTime from message where id = mid_row limit 1 INTO mid_time;
			SELECT concat('target_time: ', target_time, '  first_time: ', first_time, '  mid_time: ', mid_time, '  last_time: ', last_time) INTO dbg_txt;
            CALL debug_log(dbg_txt);
			SELECT concat('first_row: ', first_row, '  mid_row: ', mid_row, '  last_row: ', last_row) INTO dbg_txt;
            CALL debug_log(dbg_txt);
            
			-- Determine on which half of the tree the target resides
			IF target_time < mid_time THEN
				SET last_row = mid_row;
			ELSE
				SET first_row = mid_row;
			END IF;
		END WHILE;
        -- Return the row number which has the closest creatTime timestamp to the target time.
        SET p_row_number = mid_row;
        
		SELECT concat('binary_search_time(): Exiting procedure...') INTO dbg_txt;
        CALL debug_log(dbg_txt);

	END//

DELIMITER ;

