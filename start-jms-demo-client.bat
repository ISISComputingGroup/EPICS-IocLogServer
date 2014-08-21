@echo off
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%

python %MYDIRBLOCK%dev-tools\jms_client.py