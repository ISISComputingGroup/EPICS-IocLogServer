@echo off
set MYDIRBLOCK=%~dp0

call %MYDIRBLOCK%..\..\config_env_base.bat

%MYDIRBLOCK%..\..\support\HideWindow\bin\%EPICS_HOST_ARCH%\HideWindow.exe H

call "%MYDIRBLOCK%ActiveMQ\bin\activemq.bat" start

exit