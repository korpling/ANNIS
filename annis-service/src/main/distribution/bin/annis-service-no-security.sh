#!/bin/bash

if [ -z "$ANNIS_HOME" ]; then
        echo Please set the environment variable ANNIS_HOME to the Annis distribution directory.
        exit
fi

export ANNIS_NOSECURITY=true

$ANNIS_HOME/bin/annis-service.sh $1
