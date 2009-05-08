#!/bin/bash

#conf
export ANNIS_HOME=$1

#code
cp AnnisService/annis.jar $ANNIS_HOME/lib/
cp -R AnnisService/scripts/* $ANNIS_HOME/scripts/
cp -R AnnisService/bin/* $ANNIS_HOME/bin/
