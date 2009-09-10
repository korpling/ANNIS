#!/bin/bash

#conf
export ANNIS_HOME=$1

ANNIS_VERSION=2.1-SNAPSHOT
DIST_DIR=Annis-Service/target/annis-service-$ANNIS_VERSION-distribution.dir/annis-service-$ANNIS_VERSION

#code
rm -f $ANNIS_HOME/lib/*
rm -f $ANNIS_HOME/scripts/*
rm -f $ANNIS_HOME/bin/*
cp $DIST_DIR/lib/* $ANNIS_HOME/lib/
cp -Rf $DIST_DIR/scripts/* $ANNIS_HOME/scripts/
cp -Rf $DIST_DIR/bin/* $ANNIS_HOME/bin/
