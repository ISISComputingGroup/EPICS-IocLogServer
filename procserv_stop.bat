@echo off
setlocal
set MYDIR=%~dp0
REM kill procservs that manage log servers, which in turn terminates the log servers

set CSPID=
if exist "c:\instrument\var\run\EPICS_IOCLOG.pid" (
    for /F %%i in ( c:\instrument\var\run\EPICS_IOCLOG.pid ) DO set CSPID=%%i
)
if "%CSPID%" == "" (
    @echo %DATE% %TIME% IOC Log server is not running
) else (
    @echo %DATE% %TIME% Killing IOC Log server cygwin PID %CSPID%
    REM %ICPCYGBIN%\kill.exe %CSPID%
    del c:\instrument\var\run\EPICS_IOCLOG.pid
)

