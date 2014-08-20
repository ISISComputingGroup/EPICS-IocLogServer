@echo off
set CURRWORKINGDIR=%cd%
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%\LogServer

mvn --settings=%MYDIRBLOCK%mvn_user_settings.xml clean
set builderr=%errorlevel% 

REM return to previous working directory
cd %CURRWORKINGDIR%

if %builderr% neq 0 exit /b %builderr% 
