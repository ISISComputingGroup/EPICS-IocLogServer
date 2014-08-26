#Makefile at top of application tree
TOP = ../../extensions
include $(TOP)/configure/CONFIG

include $(TOP)/configure/RULES_TOP

install:
	build-log-server.bat

clean:
	clean-log-server.bat

uninstall:
