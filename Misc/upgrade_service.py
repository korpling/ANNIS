#!/usr/bin/python3

import subprocess
import os
import tarfile
import shutil
import argparse
import tempfile
import re

def updateEnv(instDir):
	env = os.environ.copy();
	env["ANNIS_HOME"] = instDir;
	return env

def checkDBSchemaVersion(instDir):
	p = subprocess.Popen([os.path.join(args.dir, "bin", "annis-admin.sh"), "check-db-schema-version"], env=updateEnv(instDir))
	p.wait()
	if p.returncode != 0:
		print("Can't update service automatically since the new version has a different database scheme!")
		exit(10)

def startService(instDir):
	print("Starting service in " + instDir)
	p = subprocess.Popen([os.path.join(args.dir, "bin", "annis-service.sh"), "start"], env=updateEnv(instDir), stdout=subprocess.PIPE)
	p.communicate()
	if p.returncode != 0:
		print("Can't start service in " + instDir)
		exit(2)		

def stopService(instDir):
	p = subprocess.Popen([os.path.join(args.dir, "bin", "annis-service.sh"), "stop"], env=updateEnv(instDir), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
	output = p.communicate()
	if p.returncode != 0:
		print("Can't stop service in " + instDir)
		print(output[0].decode("utf-8"))
		exit(3)
	print("Stopped service in " + instDir)
	
###################
# begin main code #
###################

parser = argparse.ArgumentParser(description="Upgrades a ANNIS service.")
parser.add_argument("dir", help="The directory containing the ANNIS service.")
parser.add_argument("archive", help="The archive file containing the new ANNIS version.")
parser.add_argument("-b", "--backup", help="Perform a backup of already deployed ANNIS instances. This parameter defines also the prefix to use to name the folders.")
args = parser.parse_args()

args.dir = os.path.normpath(args.dir)

tmp = tempfile.mkdtemp(prefix="annisservice-upgrade-")

print("Extracting the distribution archive to " + tmp)
shutil.unpack_archive(args.archive, tmp)

# find the actual toplevel directory
extracted = tmp
for root, dirs, files in os.walk(tmp):
	if extracted == tmp:
		for d in dirs:
			if d.startswith("annis-service"):
				extracted = os.path.join(root, d)
				break

origconf = os.path.join(args.dir, "conf")
newconf = os.path.join(extracted, "conf")

print("Copying the config files.")

shutil.copy2(os.path.join(origconf, "database.properties"), os.path.join(newconf, "database.properties"))
shutil.copy2(os.path.join(origconf, "annis-service.properties"), os.path.join(newconf, "annis-service.properties"))

# check if we can update without any database migration
print("Check database schema version")
checkDBSchemaVersion(extracted)

stopService(args.dir)

if args.backup:
	backup = args.backup + "_" + os.path.basename(args.dir)
	backupPath = os.path.join(os.path.dirname(args.dir), backup)
	print ("Moving up old installation files to " + backupPath)
	if os.path.exists(backupPath):
		shutil.rmtree(backupPath)
	shutil.move(args.dir, backupPath)
else:
	print("Removing old installation files.")
	shutil.rmtree(args.dir)

print("Copying new version to old location.")
shutil.move(extracted, args.dir)
shutil.rmtree(tmp)

startService(args.dir)
