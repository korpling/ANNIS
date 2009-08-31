#!/bin/bash

mvn install:install-file\
 -Dfile=AnnisService/annis-rmi-service-1.0.jar\
 -DgroupId=de.hu_berlin.german.korpling.annis\
 -DartifactId=AnnisService\
 -Dversion=2.1-SNAPSHOT\
 -Dpackaging=jar

mvn install:install-file\
 -Dfile=AnnisService/annis-rmi-objects-1.0.jar\
 -DgroupId=de.hu_berlin.german.korpling.annis\
 -DartifactId=AnnisServiceObjects\
 -Dversion=2.1-SNAPSHOT\
 -Dpackaging=jar

# on server for postgres
# mvn deploy:deploy-file -Dfile=/tmp/postgresql-jdbc-8.2-505-copy-20070719.jdbc3.jar -DgroupId=postgresql -DartifactId=postgresql-copy -Dversion=8.2-505.jdbc3 -Dpackaging=jar -DgeneratePom=true -Durl=file:///home/annis/public_html/maven2/ -Did=annis