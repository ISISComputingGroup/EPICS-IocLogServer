SELECT  routine_schema,  
        routine_name,  
        routine_type 
FROM information_schema.routines 
WHERE routine_schema = 'msg_log' 
ORDER BY routine_name;

