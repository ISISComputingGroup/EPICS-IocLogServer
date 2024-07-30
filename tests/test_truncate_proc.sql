USE msg_log;

SET @retention_period_days = 30;
SET @retention_period_hours = 0;
SET @retention_period_minutes = 0;

-- Create retention period as time format
-- MAKETIME(hour, minute, second)
SELECT MAKETIME(@retention_period_hours+(@retention_period_days*24), @retention_period_minutes, 0) INTO @retention_period;

SET @ret_first_row_id := 0;
SET @ret_row_number := 0;
CALL truncate_message_table(@retention_period, @ret_first_row_id, @ret_row_number);
SELECT @ret_first_row_id, @ret_row_number;
SET @new_first_datetime = NOW();
SET @new_last_datetime = NOW();

-- Check the new database time window to test that it matches the requested span.
SELECT createTime FROM message ORDER BY id LIMIT 1 INTO @new_first_datetime;
SELECT createTime FROM message ORDER BY id DESC LIMIT 1 INTO @new_last_datetime;
SET @td = TIMEDIFF(@new_last_datetime, @new_first_datetime);
SELECT "Time span in truncated message table:" AS "";
SELECT CONCAT( FLOOR(HOUR(@td) / 24), ' days ', MOD(HOUR(@td), 24), ' hours ', MINUTE(@td), ' minutes') AS "";

