#Makefile at top of application tree
TOP = ../../../extensions/master
include $(TOP)/configure/CONFIG

include $(TOP)/configure/RULES_TOP

# Linux
SCRIPT_CLEAN=./clean-log-server.sh
SCRIPT_BUILD=./build-log-server.sh
SCRIPT_VERIFY=./verify-log-server.sh

# Windows
ifneq ($(findstring windows,$(EPICS_HOST_ARCH)),)
SCRIPT_CLEAN=clean-log-server.bat
SCRIPT_BUILD=build-log-server.bat
SCRIPT_VERIFY=verify-log-server.bat
endif

# need to clean before build or sometimes get an invalid jar
install:
	$(SCRIPT_CLEAN)
	$(SCRIPT_BUILD)
	$(SCRIPT_VERIFY)

clean:
	$(SCRIPT_CLEAN)

uninstall:

