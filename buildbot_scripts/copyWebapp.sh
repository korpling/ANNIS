#!/bin/bash

#conf
ANNIS_WEBAPP_HOME=$1

#code
rm -R $ANNIS_WEBAPP_HOME/*
unzip -d $ANNIS_WEBAPP_HOME annis-gui/target/annis-gui.war

chmod -R ug+rw $ANNIS_WEBAPP_HOME/*

