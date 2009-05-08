#!/bin/bash

#conf
export ANNIS_HOME=$1

#code
cp AnnisService/annis.jar $ANNIS_HOME/lib/
cp -Rf AnnisService/scripts/* $ANNIS_HOME/scripts/
cp -Rf AnnisService/bin/* $ANNIS_HOME/bin/
