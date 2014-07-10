@echo off
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%

python %MYDIRBLOCK%dev-tools\ioc_message_simulator.py