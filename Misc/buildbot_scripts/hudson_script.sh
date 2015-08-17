cd $WORKSPACE

# Stop the old web application
rm -Rf /home/annis/tomcat-annis/webapps/annis3-snapshot*

# copy the original user configuration
rm -R /home/annis/annis_snapshot_users/*
cp -R /etc/annis/user_config/ /home/annis/annis_snapshot_users/

# deploy new service (including restart)
BUILD_ID=allow_to_run_as_daemon Misc/upgrade_service.py /opt/annis/annis3-snapshot annis-service/target/annis-service-*-distribution.tar.gz

# deploy the new web application
cp annis-gui/target/annis-gui.war /home/annis/tomcat-annis/webapps/annis3-snapshot.war
