# Automated Log Truncation Testing

## How the truncation works
The `message` table in the `msg_log` database is appended to whenever the IOC Log Server generates a log message. Over time, the message table can become huge and needs some housekeeping by deleting records older than a predefined retention period (default 30 days).

A simple delete operation can not be performed, as it is likely to lock the database during the process, which is undesirable on an operational experiment machine.

The automatic truncation procedure examines the `createTime` field for the earliest and latest records. A target cutoff date and time is derived from the latest record and a binary search invoked to find the record closest to the cutoff time. The `SELECT` statements are few and limited to 1 record, so the search should normally complete in under a second.

Once the target record is known, records are deleted in batches of typically 1000, from the oldest to target record.

## Design of Truncation Procedures
There are five SQL files associated with the truncation process:
| SQL file | Description |
| ------------ | -----------|
| log-truncation-schema.sql | Used to setup the truncation event and debug table for existing systems.|
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
### Creating the truncation event and procedures on systems with and existing msg_log database
There is a utility SQL script: `log-truncation-schema.sql` which should be run to setup the periodic truncation event, thus:
`C:\Instrument\Apps\MySQL\bin\mysql.exe -u root --password=<root password> < tests\log-truncation-schema.sql`

### Populating the `message` table with simulated records:
For testing, the `message` table is emptied and populated with simulated messages, with `createTime` timestamps at one hour intervals over a period of 60 days. This is accomplished using the `test_fill_message.sql` SQL file:
 `C:\Instrument\Apps\MySQL\bin\mysql.exe -u root --password=<root password> < tests\test_fill_message.sql`


### Testing the `truncate_message_table()` procedure

A SQL script has been written to call the `truncate_message_table()` procedure with standard default values, i.e. retention period of 30 days. Call the script thus:
`C:\Instrument\Apps\MySQL\bin\mysql.exe -u root -
-password=<root password> < tests\test_truncate_proc.sql`

A result similar to the following should be output:
```
@ret_first_row_id       @ret_row_number
1       722

Time span in truncated message table:

29 days 22 hours 0 minutes
```

Note that the actual remaining time span may not be exactly the given retention period, but should be close. This is due to the binary search termination condition providing a cutoff record which is within three records of the ideal. Nobody should quibble about three records!

### Testing the `log_truncation_event()` event
The `log_truncation_event` eventt is called periodically to ensure that the msg_log database is regularly pruned to the message retention period, given by the declared variables: retention_period_days, retention_period_hours, retention_period_minutes towards the top of the `truncate_event.sql` file. The retention period can be adjust as required, but there is a restriction of around 35 days, due to a limitation within the built-in SQL MAKETIME() function.
To expedite testing of the event, it may be necessary to set the initial event to occur to some time soon (a minute or so). After editing the required event time & period, it will be necessary to re-create the event in the database, thus:
`C:\Instrument\Apps\MySQL\bin\mysql.exe -u root --password=<root password> < truncate_event.sql`
After the event should have triggered, check the message table to ensure that it is now timebound to within the given retention period.
Further details on the execution of the even and the truncation procedure should be visible in the `debug_message` table.


