# Automated Log Truncation Testing

## How the truncation works
The `message` table in the `msg_log` database is appended to whenever the IOC Log Server generates a log message. Over time, the message table can become huge and needs some housekeeping by deleting records older than a predefined retention period (default 30 days).

A simple delete operation can not be performed, as it is likely to lock the database during the process, which is undesirable on an operational experiment machine.

The automatic truncation procedure examines the `createTime` field for the earliest and latest records. A target cutoff date and time is derived from the latest record and a binary search invoked to find the record closest to the cutoff time. The `SELECT` statements are few and limited to 1 record, so the search should normally complete in under a second.

Once the target record is known, records are deleted in batches of typically 1000, from the oldest to target record.

## Design of Truncation Procedures
There are four SQL files associated with the truncation process:
| SQL file | Description |
| ------------ | -----------|
|create_event_logger.sql| Creates a table to log truncation events|
| debug_log.sql | A procedure that can be called to log debug or status messages.|
| binary-search.sql | Procedure to perform a binary search on `createTime` to determine the cutoff target record.|
| truncate_message_table.sql | Procedure that calls `binary_search_time()` with the given retention period, then deletes records in chunks of say 1000, followed by a `sleep()` of one second to allow cooperation of other database requests.|
| truncate_event.sql | This creates the event to call the `truncate_message_table()` procedure on a regulalry recurring basis, typically every day at 01:00. |

Once a large message table has been truncated, this process should keep it at a managable size and subsequent truncations should be relatively quick.

`log_truncation_event` is where the parameters for event scheduling and log retention period are defined. When this event is triggered, it will call the `truncate_message_table()` procedure.

`truncate_message_table()` calls `binary_search_time()` to determine the record which is closest to the calculated target cutoff date/time. It then proceeds to delete recrods in chunks, with a `sleep` between chunks to cooperate with other database requests.

`binary_search_time()` takes a single input parameter which defines the retention period as a `TIME` type (note that this is limited in SQL to '838:59:59', about 35 days). There are two output parameter reference values, which return the id value of the earliest row in the table (zeroth row) and the record number in the table which is closest to the target cutoff time.

## Test Design
