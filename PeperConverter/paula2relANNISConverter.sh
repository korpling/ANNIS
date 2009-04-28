#!/bin/bash
PROG_CALL='-classpath bin:lib/ext/org.eclipse.emf.ecore.jar:lib/ext/org.eclipse.emf.common.jar:lib/ext/log4j-1.2.15.jar:lib/de.dataconnector.tuplewriter_noDep.jar:lib/de.util.graph.jar:lib/de.corpling.salt.jar:lib/de.util.timer.jar:modules/de.corpling.modules.paula10.jar:modules/de.corpling.peper.modules.dot.jar:modules/de.corpling.peper.modules.relANNIS.jar  de.corpling.peper.impl.PeperConverterImpl'
PARAMS=' -s '$1'::paula::1.0 -t '$2'::relANNIS::3.1'
java $PROG_CALL $PARAMS