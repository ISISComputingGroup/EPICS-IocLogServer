@echo off

set CYGWIN=nodosfilewarning
set MYDIRIOCLOG=%~dp0

REM Set Logging directory
IF "%ICPVARDIR%"=="" (
	set ICPVARDIR=C:/Instrument/Var
)
set IOCLOGROOT=%ICPVARDIR%/logs/ioc


REM *****************************************
REM *        JMS SERVER
REM *****************************************
set STARTCMD=%MYDIRIOCLOG%start-jms-server.bat
set CONSOLEPORT=9001
set LOG_FILE=%IOCLOGROOT%/JMS-%%Y%%m%%d.log

@echo Starting JMS Log Server on 127.0.0.1 (console port %CONSOLEPORT%)
@echo * log file - %LOG_FILE%
%MYDIRIOCLOG%..\..\tools\cygwin_bin\procServ.exe --logstamp --logfile="%LOG_FILE%" --timefmt="%%c" --restrict --ignore="^D^C" --name=JMS --pidfile="/cygdrive/c/windows/temp/EPICS_JMS.pid" %CONSOLEPORT% %STARTCMD%


REM *****************************************
REM *        LOG SERVER
REM *****************************************
set STARTCMD=%MYDIRIOCLOG%start-log-server.bat
set CONSOLEPORT=9002
set LOG_FILE=%IOCLOGROOT%/IOCLOG-%%Y%%m%%d.log

@echo Starting IOC Log Server on 127.0.0.1 (console port %CONSOLEPORT%)
@echo * log file - %LOG_FILE%
%MYDIRIOCLOG%..\..\tools\cygwin_bin\procServ.exe --logstamp --logfile="%LOG_FILE%" --timefmt="%%c" --restrict --ignore="^D^C" --name=IOCLOG --pidfile="/cygdrive/c/windows/temp/EPICS_IOCLOG.pid" %CONSOLEPORT% %STARTCMD%
