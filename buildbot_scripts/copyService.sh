#!/bin/bash

#conf
export ANNIS_HOME=$1

#code
cp AnnisService/annis.jar $ANNIS_HOME/lib/
cp -f AnnisService/scripts/* $ANNIS_HOME/scripts/
cp -f AnnisService/bin/* $ANNIS_HOME/bin/
