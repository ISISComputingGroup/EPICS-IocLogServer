#Makefile at top of application tree
TOP = .
ACTIONS += uninstall

.PHONY: install uninstall clean

install:
	$(TOP)\build-log-server.bat

clean:
	$(TOP)\clean-log-server.bat

uninstall: clean