@echo off
set CURRWORKINGDIR=%cd%
set MYDIRBLOCK=%~dp0

cd /d %MYDIRBLOCK%\LogServer

REM need to call config env to ensure a java 8 environment.
call %MYDIRBLOCK%\..\..\..\config_env.bat
mvn --settings=%MYDIRBLOCK%mvn_user_settings.xml clean verify
set builderr=%errorlevel% 

REM return to previous working directory
cd /d %CURRWORKINGDIR%

if %builderr% neq 0 exit /b %builderr% 
