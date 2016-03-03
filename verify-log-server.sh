#!/bin/bash

SCRIPT=$(readlink -f ${BASH_SOURCE[0]})
SCRIPTPATH=`dirname "$SCRIPT"`
MYDIRBLOCK=$SCRIPTPATH
CURRWORKINGDIR=`pwd`

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . $MYDIRBLOCK/../../../config_env_base.sh
fi

cd "$MYDIRBLOCK/LogServer"

mvn --settings="$MYDIRBLOCK/mvn_user_settings.xml" verify
builderr=$?

# return to previous working directory
cd "$CURRWORKINGDIR"

if [ $builderr != 0 ]; then
    exit $builderr
fi

