#!/bin/bash

SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
MYDIR=$SCRIPTPATH

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $MYDIR/../../../config_env_base.sh
fi

# kill procservs that manage log servers, which in turn terminates the log servers

PIDFILE="$EPICS_ROOT/EPICS_IOCLOG.pid"
if [ -r "$PIDFILE" ]; then
    CSPID=`cat "$PIDFILE"`
    echo "Killing IOC Log server PID: $CSPID"
    kill $CSPID
    rm "$PIDFILE"
else
    echo "IOC Log server is not running (or $PIDFILE not readable)"
fi

