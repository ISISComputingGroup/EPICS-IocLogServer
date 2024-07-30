DROP PROCEDURE IF EXISTS debug_log;

DELIMITER //
CREATE PROCEDURE debug_log(IN log_message VARCHAR(255))
	BEGIN
		SET autocommit = 1;
		CREATE TABLE IF NOT EXISTS debug_messages(
		id INT AUTO_INCREMENT PRIMARY KEY,
		witten_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		debug_message VARCHAR(255)) ENGINE=INNODB;
       
		INSERT INTO debug_messages (debug_message) VALUES (log_message);
	END//

DELIMITER ;
