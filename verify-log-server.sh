#!/bin/bash

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
WORKINGDIR="`pwd`"

# Ensure environment is set up
if [ -z "$EPICS_ROOT" ]; then
    . "$SCRIPTDIR/../../../config_env_base.sh"
fi

cd "$SCRIPTDIR/LogServer"

mvn --settings="$SCRIPTDIR/mvn_user_settings.xml" verify
builderr=$?

# return to previous working directory
cd "$WORKINGDIR"

if [ $builderr -ne 0 ]; then
    exit $builderr
fi

