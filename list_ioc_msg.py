import os
import glob
import datetime
import mysql.connector

conn = mysql.connector.connect(host="localhost", 
                     user="msg_log", # your username
                      passwd="$msg_log", # your password
                      db="msg_log") # name of the data base

c = conn.cursor()

c.execute("SELECT clientName,severity,contents FROM message")
for (clientName,severity,contents) in c :
    print clientName,':',severity,':',contents

#c.execute("SELECT * FROM message")
#for row in c :
#    print row

c.close()
conn.close()
