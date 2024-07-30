/*=======================================================================
 * log-truncation-schema.sql
 *
 * This SQL script is intended to be used for existing log databases
 * where the automatic truncation mechanism needs to be added.
 * This will normally be facilitated for systems via the usual 
 * SystemSetup\log_mysql_schema.txt schema file.
 * 
 * Use:
 * C:\Instrument\Apps\MySQL\bin\mysql.exe -u root --password=<db root password> < log-truncation-schema.sql
 *
 * Ian Gillingham, July 2024
 * ======================================================================
 */

USE msg_log;

SET default_storage_engine=INNODB;

# Automatic log message table truncation
source create_event_logger.sql;
source debug_log.sql;
source binary-search.sql;
source truncate_message_table.sql
source truncate_event.sql;
