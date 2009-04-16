#!/bin/bash

#conf
ANNIS_WEBAPP_HOME=$1

#code
rm -R $ANNIS_WEBAPP_HOME/*
unzip -d $ANNIS_WEBAPP_HOME Annis2-web/dist/Annis2-web.war
cp AnnisService/annis-rmi-service-1.0.jar $ANNIS_WEBAPP_HOME/WEB-INF/lib
cp AnnisService/annis-rmi-objects-1.0.jar $ANNIS_WEBAPP_HOME/WEB-INF/lib

chmod -R ug+rw $ANNIS_WEBAPP_HOME/*

