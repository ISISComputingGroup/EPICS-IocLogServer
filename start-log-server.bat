@echo off
set LOGDIRBLOCK=%~dp0
set CURRWORKINGDIR=%cd%

call %LOGDIRBLOCK%..\..\config_env_base.bat

%HIDEWINDOW% h

cd %LOGDIRBLOCK%LogServer\target\

java -jar IocLogServer-1.0-SNAPSHOT.jar

REM return to previous working directory
cd %CURRWORKINGDIR%