@echo off
set LOGDIRBLOCK=%~dp0
set CURRWORKINGDIR=%cd%

call %LOGDIRBLOCK%..\..\config_env_base.bat

%HIDEWINDOW% h

cd %LOGDIRBLOCK%base\IOCLogServer\

REM IF NOT EXIST ioc-log-server.jar (
REM     echo Building IOC Log Server
REM 	call ant -q clean build-jar
REM )
REM cd %LOGDIRBLOCK%base\IOCLogServer\

java -jar ioc-log-server.jar

REM return to previous working directory
cd %CURRWORKINGDIR%