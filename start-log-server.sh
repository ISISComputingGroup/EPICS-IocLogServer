#!/bin/bash

# Path to this script
SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
LOGDIRBLOCK=$SCRIPTPATH

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $LOGDIRBLOCK/../../../config_env_base.sh
fi

CURRWORKINGDIR=`pwd`
cd "$LOGDIRBLOCK/LogServer/target"

java -jar IocLogServer-1.0-SNAPSHOT.jar

# return to previous working directory
cd $CURRWORKINGDIR

