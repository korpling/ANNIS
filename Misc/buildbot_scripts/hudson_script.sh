cd $WORKSPACE

# Stop the AnnisService
chmod u+x buildbot_scripts/stopService.sh
./Misc/buildbot_scripts/stopService.sh /opt/annis/annis3-snapshot

# Copy the the Annis webapp
#/home/annis/tomcat-annis/bin/shutdown.sh
#sleep 5
rm -Rf /home/annis/tomcat-annis/webapps/annis3-snapshot*
cp annis-gui/target/annis-gui.war /home/annis/tomcat-annis/webapps/annis3-snapshot.war
#BUILD_ID=allow_to_run_as_daemon /home/annis/tomcat-annis/bin/startup.sh

# Copy Annis Service
chmod u+x Misc/buildbot_scripts/copyService.sh
Misc/buildbot_scripts/copyService.sh /opt/annis/annis3-snapshot 5713

# copy the original configuration
rm -R /home/annis/annis_snapshot_users/*
cp -R /etc/annis/user_config/ /home/annis/annis_snapshot_users/

# start service
chmod u+x Misc/buildbot_scripts/startService.sh
BUILD_ID=allow_to_run_as_daemon buildbot_scripts/startService.sh /opt/annis/annis3-snapshot/
