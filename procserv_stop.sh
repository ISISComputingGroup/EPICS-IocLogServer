#!/bin/bash

SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
MYDIR=$SCRIPTPATH

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $MYDIR/../../../config_env_base.sh
fi

# kill procservs that manage log servers, which in turn terminates the log servers

if [ -r $EPICS_ROOT/EPICS_JMS.pid ]; then
    CSPID=`cat $EPICS_ROOT/EPICS_JMS.pid`
    echo "Killing JMS server PID $CSPID"
    kill $CSPID
    rm $EPICS_ROOT/EPICS_JMS.pid
else
    echo "JMS server is not running (or $EPICS_ROOT/EPICS_JMS.pid not readable)"
fi

if [ -r $EPICS_ROOT/EPICS_IOCLOG.pid ]; then
    CSPID=`cat $EPICS_ROOT/EPICS_IOCLOG.pid`
    echo "Killing JMS server PID $CSPID"
    kill $CSPID
    rm $EPICS_ROOT/EPICS_IOCLOG.pid
else
    echo "IOC Log server is not running (or $EPICS_ROOT/EPICS_IOCLOG.pid not readable)"
fi

