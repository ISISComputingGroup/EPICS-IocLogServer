@echo off
setlocal
set CYGWIN=nodosfilewarning
set MYDIRIOCLOG=%~dp0

call %MYDIRIOCLOG%..\..\..\config_env_base.bat

REM Set Logging directory
IF "%ICPVARDIR%"=="" (
	set ICPVARDIR=C:/Instrument/Var
)
set IOCLOGROOT=%ICPVARDIR%/logs/ioc
for /F "usebackq" %%I in (`%ICPCYGBIN%\cygpath %IOCLOGROOT%`) do SET IOCCYGLOGROOT=%%I

REM Set config
copy %MYDIRIOCLOG%\logserver_config.ini %MYDIRIOCLOG%\LogServer\target\logserver_config.ini

REM *****************************************
REM *        LOG SERVER
REM *****************************************
set STARTCMD=%ComSpec% /c %MYDIRIOCLOG%start-log-server.bat
set CONSOLEPORT=9002
set LOG_FILE=%IOCCYGLOGROOT%/IOCLOG-%%Y%%m%%d.log

@echo Starting IOC Log Server on 127.0.0.1 (console port %CONSOLEPORT%)
@echo * log file - %LOG_FILE%
%ICPCYGBIN%\procServ.exe --logstamp --logfile="%LOG_FILE%" --timefmt="%%c" --restrict --ignore="^D^C" --name=IOCLOG --pidfile="/cygdrive/c/windows/temp/EPICS_IOCLOG.pid" %CONSOLEPORT% %STARTCMD%
