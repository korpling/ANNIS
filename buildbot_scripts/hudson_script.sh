cd $WORKSPACE

# Stop the AnnisService
chmod u+x buildbot_scripts/stopService.sh
./buildbot_scripts/stopService.sh /opt/annis/trunk

# Copy the the Annis webapp
chmod u+x buildbot_scripts/copyWebapp.sh
buildbot_scripts/copyWebapp.sh /srv/tomcat/Annis-trunk

# Patch webapp
chmod u+x buildbot_scripts/patchWebapp.py
buildbot_scripts/patchWebapp.py /srv/tomcat/Annis-trunk $BUILD_ID /etc/annis/user_config_trunk

# Copy Annis Service
chmod u+x buildbot_scripts/copyService.sh
buildbot_scripts/copyService.sh /opt/annis/trunk

# start service
chmod u+x buildbot_scripts/startService.sh
BUILD_ID=allow_to_run_as_daemon buildbot_scripts/startService.sh /opt/annis/trunk/
