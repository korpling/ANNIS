#!/bin/bash

#conf
export ANNIS_HOME=$1

#code
cp AnnisService/annis.jar $ANNIS_HOME/lib/
cp AnnisService/scripts/* $ANNIS_HOME/scripts/
cp AnnisService/bin/* $ANNIS_HOME/bin/
