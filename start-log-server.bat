@echo off
set LOGDIRBLOCK=%~dp0
set CURRWORKINGDIR=%cd%

call %MYDIRBLOCK%..\..\config_env_base.bat

%MYDIRBLOCK%..\..\support\HideWindow\bin\%EPICS_HOST_ARCH%\HideWindow.exe H

cd %LOGDIRBLOCK%base\IOCLogServer\

IF NOT EXIST ioc-log-server.jar (
    echo Building IOC Log Server
	call ant -q clean build-jar
)

cd %LOGDIRBLOCK%base\IOCLogServer\

java -jar ioc-log-server.jar


REM return to previous working directory
cd %CURRWORKINGDIR%