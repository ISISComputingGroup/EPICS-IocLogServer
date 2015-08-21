#Makefile at top of application tree
TOP = ../../../extensions/master
include $(TOP)/configure/CONFIG

include $(TOP)/configure/RULES_TOP

# need to clean before build or sometimes get an invalid jar
install:
	clean-log-server.bat
	build-log-server.bat
	verify-log-server.bat

clean:
	clean-log-server.bat

uninstall:
