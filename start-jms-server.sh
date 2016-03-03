#!/bin/bash

# Path to this script
SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
MYDIRBLOCK=$SCRIPTPATH

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $MYDIRBLOCK/../../../config_env_base.sh
fi

$MYDIRBLOCK/ActiveMQ/bin/activemq start 

