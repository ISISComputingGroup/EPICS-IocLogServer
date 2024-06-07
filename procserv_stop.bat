@echo off
setlocal
set MYDIR=%~dp0
REM kill procservs that manage log servers, which in turn terminates the log servers

set CSPID=
for /F %%i in ( c:\instrument\var\run\EPICS_IOCLOG.pid ) DO set CSPID=%%i
if "%CSPID%" == "" (
    @echo IOC Log server is not running
) else (
    @echo Killing IOC Log server PID %CSPID%
    REM %ICPCYGBIN%\kill.exe %CSPID%
    del c:\instrument\var\run\EPICS_IOCLOG.pid
)

