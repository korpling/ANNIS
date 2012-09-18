#!/bin/bash

#conf
ANNIS_WEBAPP_HOME=$1

#code
rm -Rf $ANNIS_WEBAPP_HOME/*
unzip -d $ANNIS_WEBAPP_HOME annis-gui/target/annis-gui.war

chmod -Rf ug+rw $ANNIS_WEBAPP_HOME/*

