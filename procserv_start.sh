#!/bin/bash

# Path to this script
SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
MYDIRIOCLOG=$SCRIPTPATH

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $MYDIRIOCLOG/../../../config_env_base.sh
fi

# Set Logging directory
IOCLOGROOT="$ICPVARDIR/logs/ioc"

# *****************************************
# *        LOG SERVER
# *****************************************
STARTCMD="/bin/bash -i -O huponexit $MYDIRIOCLOG/start-log-server.sh"
CONSOLEPORT="9002"
LOG_FILE="$IOCLOGROOT/IOCLOG-$(date +'%Y%m%d').log"

echo "Starting IOC Log Server on 127.0.0.1 (console port $CONSOLEPORT)"
echo "* log file - $LOG_FILE"
procServ --logstamp --logfile="$LOG_FILE" --timefmt="%c" --restrict --ignore="^D^C" --name=IOCLOG --pidfile="$EPICS_ROOT/EPICS_IOCLOG.pid" $CONSOLEPORT $STARTCMD

