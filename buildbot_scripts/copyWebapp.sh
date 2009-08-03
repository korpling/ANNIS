#!/bin/bash

#conf
ANNIS_WEBAPP_HOME=$1

#code
rm -R $ANNIS_WEBAPP_HOME/*
unzip -d $ANNIS_WEBAPP_HOME Annis-web/target/Annis-web.war

chmod -R ug+rw $ANNIS_WEBAPP_HOME/*

