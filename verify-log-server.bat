@echo off
set CURRWORKINGDIR=%cd%
set MYDIRBLOCK=%~dp0

cd /d %MYDIRBLOCK%\LogServer

mvn --settings=%MYDIRBLOCK%mvn_user_settings.xml verify
set builderr=%errorlevel% 

REM return to previous working directory
cd /d %CURRWORKINGDIR%

if %builderr% neq 0 exit /b %builderr% 
