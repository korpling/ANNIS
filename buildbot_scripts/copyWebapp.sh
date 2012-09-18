#!/bin/bash

#conf
ANNIS_WEBAPP_HOME=$1

#code
rm -Rf $ANNIS_WEBAPP_HOME/META-INF/*
rm -Rf $ANNIS_WEBAPP_HOME/THIRD-PARTY/*
rm -Rf $ANNIS_WEBAPP_HOME/VAADIN/*
rm -Rf $ANNIS_WEBAPP_HOME/WEB-INF/*
rm -Rf $ANNIS_WEBAPP_HOME/annis.gui.widgets.gwt.AnnisWidgetSet/*
rm -Rf $ANNIS_WEBAPP_HOME/jquery/*
rm -Rf $ANNIS_WEBAPP_HOME/tutorial/*
rm -f $ANNIS_WEBAPP_HOME/CHANGELOG
rm -f $ANNIS_WEBAPP_HOME/LICENSE
rm -f $ANNIS_WEBAPP_HOME/NOTICE

unzip -d $ANNIS_WEBAPP_HOME annis-gui/target/annis-gui.war

chmod -Rf ug+rw $ANNIS_WEBAPP_HOME/*

# always succeed
exit 0
