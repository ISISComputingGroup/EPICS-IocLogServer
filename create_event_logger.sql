 "/* 
 * This schema creates the EventLog table, which is a development tool
 * to facilitate debugging and logging of SQL procedures and events.
 * It has been designed initially to assist with the log_truncation_event
 * trigger and to examine progress of the last log_truncation_event.
 *
 * Author: Ian Gillingham, July 2024
 */"
DELIMITER //

DROP TABLE IF EXISTS EventsLog;

CREATE TABLE IF NOT EXISTS EventsLog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eventName VARCHAR(20) NOT NULL,
    logMsg VARCHAR(1000) NOT NULL,
    dtWhenLogged DATETIME NOT NULL
);

DELIMITER ;

