@echo off
set MYDIRBLOCK=%~dp0

cd /d %MYDIRBLOCK%

python %MYDIRBLOCK%dev-tools\jms_client.py
