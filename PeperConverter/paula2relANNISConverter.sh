#!/bin/bash
PROG_CALL='-classpath bin:lib/org.eclipse.emf.ecore.jar:lib/org.eclipse.emf.common.jar:lib/log4j-1.2.15.jar:lib/de.dataconnector.tuplewriter_noDep.jar:lib/de.corpling.salt.jar:lib/de.corpling.exporter.jar:lib/de.util.timer.jar  de.corpling.peper.impl.PeperConverterImpl'
PARAMS=' -s '$1'::paula::1.0 -t '$2'::relANNIS::3.0'
java $PROG_CALL $PARAMS