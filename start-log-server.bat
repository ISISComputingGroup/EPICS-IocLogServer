@echo off
set MYDIRBLOCK=%~dp0

cd %MYDIRBLOCK%base\IOCLogServer\

IF NOT EXIST ioc-log-server.jar (
    echo Building IOC Log Server
	call ant -q clean build-jar
)

cd %MYDIRBLOCK%base\IOCLogServer\

java -jar ioc-log-server.jar