@echo off
set CURRWORKINGDIR=%cd%
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%\LogServer

mvn verify

REM return to previous working directory
cd %CURRWORKINGDIR%