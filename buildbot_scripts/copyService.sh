#!/bin/bash

#conf
export ANNIS_HOME=$1

ANNIS_VERSION=2.3.0-SNAPSHOT
DIST_DIR=Annis-Service/annis-service-impl/target/annis-service-impl-$ANNIS_VERSION-distribution/annis-service-impl-$ANNIS_VERSION

#code
cp -Rf $DIST_DIR/lib/* $ANNIS_HOME/lib/
cp -Rf $DIST_DIR/sql/* $ANNIS_HOME/sql/
cp -Rf $DIST_DIR/bin/* $ANNIS_HOME/bin/
