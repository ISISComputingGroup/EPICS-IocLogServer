@echo off
set CURRWORKINGDIR=%cd%
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%\LogServer

mvn clean compile assembly:single

REM return to previous working directory
cd %CURRWORKINGDIR%