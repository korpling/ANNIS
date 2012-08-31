#!/bin/bash

#conf
export ANNIS_HOME=$1

ANNIS_VERSION=3.0.0-SNAPSHOT
DIST_DIR=annis-service-implementation/target/annis-service-implementation-$ANNIS_VERSION-distribution/annis-service-implementation-$ANNIS_VERSION

#code
# delete all old libraries so that they don't get into the classpath
rm -fR $ANNIS_HOME/lib/*
# copy new files
cp -Rf $DIST_DIR/lib/* $ANNIS_HOME/lib/
cp -Rf $DIST_DIR/sql/* $ANNIS_HOME/sql/
cp -Rf $DIST_DIR/bin/* $ANNIS_HOME/bin/
cp $DIST_DIR/conf/annis-service.properties $ANNIS_HOME/conf/
