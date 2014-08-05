@echo off
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%base\IOCLogServer\

ant clean build-exe