#!/bin/bash

mvn install:install-file\
 -Dfile=../AnnisService/annis-rmi-service-1.0.jar\
 -DgroupId=de.hu_berlin.german.korpling.annis\
 -DartifactId=AnnisService\
 -Dversion=2.1-SNAPSHOT\
 -Dpackaging=jar

mvn install:install-file\
 -Dfile=../AnnisService/annis-rmi-objects-1.0.jar\
 -DgroupId=de.hu_berlin.german.korpling.annis\
 -DartifactId=AnnisServiceObjects\
 -Dversion=2.1-SNAPSHOT\
 -Dpackaging=jar
