@echo off
set MYDIRBLOCK=%~dp0

start "ActiveMQ" "%MYDIRBLOCK%ActiveMQ\bin\activemq.bat" start

exit