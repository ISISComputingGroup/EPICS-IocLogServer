@echo off
setlocal
set LOGDIRBLOCK=%~dp0
set CURRWORKINGDIR=%cd%

call %LOGDIRBLOCK%..\..\..\config_env_base.bat

%HIDEWINDOW% h

cd /d %LOGDIRBLOCK%LogServer\target\

java -Xms32m -Xmx64m -jar IocLogServer-1.0-SNAPSHOT.jar

REM return to previous working directory
cd /d %CURRWORKINGDIR%
