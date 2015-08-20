#!/usr/bin/python3

import subprocess
import os
import tarfile
import shutil
import argparse
import tempfile
import re
import configparser
import itertools
from urllib.parse import urlparse

def updateEnv(instDir):
	env = os.environ.copy();
	env["ANNIS_HOME"] = instDir;
	return env
	
def getversion(instDir):
	o = subprocess.check_output([os.path.join(instDir, "bin", "annis.sh"), "version"], 
		env=updateEnv(instDir), universal_newlines=True, stderr=subprocess.STDOUT)
	
	for raw in o.split("\n"):
		m = re.compile("^([0-9]+)\.([0-9]+)\.([0-9]+)(-SNAPSHOT)? .*").match(raw)
		if m:
			return m.group(1,2,3)
	return None

def checkDBSchemaVersion(instDir):
	p = subprocess.Popen([os.path.join(instDir, "bin", "annis-admin.sh"), "check-db-schema-version"], env=updateEnv(instDir))
	p.wait()
	return p.returncode == 0

def startService(instDir):
	print("Starting service in " + instDir)
	p = subprocess.Popen([os.path.join(instDir, "bin", "annis-service.sh"), "start"], env=updateEnv(instDir), stdout=subprocess.PIPE)
	p.communicate()
	if p.returncode != 0:
		print("Can't start service in " + instDir)
		exit(2)		

def stopService(instDir):
	p = subprocess.Popen([os.path.join(instDir, "bin", "annis-service.sh"), "stop"], env=updateEnv(instDir), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
	output = p.communicate()
	if p.returncode != 0:
		print("Can't stop service in " + instDir)
		print(output[0].decode("utf-8"))
		exit(3)
	print("Stopped service in " + instDir)
	
def readConfigFile(path):
	with open(path, "r") as f:
		cstr = "[CONFIG]\n" + f.read()
	config = configparser.ConfigParser()
	config.read_string(cstr)
	return config["CONFIG"]

def initArgsFromConfig(config):
	urlRaw = config["datasource.url"].strip()
	if urlRaw.startswith("jdbc:"):
		urlRaw = urlRaw[len("jdbc:"):]
	url = urlparse(urlRaw)
	
	
	# append mandatory elements
	r = ["--host", url.hostname, "--database", url.path[1:],
		"--user", config["datasource.username"].strip(),
		"--password", config["datasource.password"].strip()]
	# optional arguments
	if config["datasource.ssl"] and config["datasource.ssl"].strip().lower() == "true" :
		r.append("--ssl")
	if config["datasource.schema"]:
		r.append("--schema")
		r.append(config["datasource.schema"].strip())
		
	return r
	
def initDatabase(config, instDir):
	
	a = initArgsFromConfig(config)
	a.insert(0, os.path.join(instDir, "bin", "annis-admin.sh"))
	a.insert(1, "init")
	
	p = subprocess.Popen(a, env=updateEnv(instDir), stderr=subprocess.STDOUT, stdout=subprocess.PIPE, universal_newlines=True)
	for l in p.stdout:
		print(l, end="")
	p.communicate()
	if p.returncode != 0:
		print("ERROR: initialization of database returned error code " + str(p.returncode))
		exit(20)
		
def copyDatabase(instDir, oldInstDir, mail):
	
	a = [os.path.join(instDir, "bin", "annis-admin.sh"), "copy", 
		os.path.join(oldInstDir, "conf", "database.properties")]
	
	if mail:
		a.append("--mail")
		a.append(mail)
	
	p = subprocess.Popen(a, env=updateEnv(instDir), stderr=subprocess.STDOUT, 
		stdout=subprocess.PIPE, universal_newlines=True)
	for l in p.stdout:
		print(l, end="")
	p.communicate()
	if p.returncode != 0:
		print("ERROR: copying existing corpora failed: error code " + str(p.returncode))
		exit(30)
		
def cleanupData(instDir):
	
	
	p = subprocess.Popen([os.path.join(instDir, "bin", "annis-admin.sh"), "cleanup-data"], env=updateEnv(instDir), stderr=subprocess.STDOUT, stdout=subprocess.PIPE, universal_newlines=True)
	for l in p.stdout:
		print(l, end="")
	p.communicate()

	
###################
# begin main code #
###################

parser = argparse.ArgumentParser(description="Upgrades an ANNIS service.")
parser.add_argument("dir", help="The directory containing the ANNIS service.")
parser.add_argument("archive", help="The archive file containing the new ANNIS version.")
parser.add_argument("-b", "--backup", help="Perform a backup of the files of the existing installation. This parameter defines also the prefix to use for the name of the backup folder.")
parser.add_argument("-c", "--cleanup-data", action="store_true", help="""This will delete all data files not known to the current instance of ANNIS. 
If you have multiple parallel installations and did not use different values for the annis.external-data-path variable in the conf/annis-service.properties the data files of the other installations will be lost.""")
parser.add_argument("--force-db-update", help="Force an update of the database even is this is not necessary", action="store_true")
parser.add_argument("-m", "--mail", help="Mail adress that should be used for notifications when copying corpora from the existing installation.")
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
copiedCorpora=False

print("Check database schema version")
if args.force_db_update or (not checkDBSchemaVersion(extracted)):
	print("======================================================")
	print("Need to update the database and re-import all corpora.")
	print("This might take a long time!")
	dbconfig = readConfigFile(os.path.join(args.dir, "conf", "database.properties"))
	version = getversion(extracted)
	if version:
		dbconfig["datasource.schema"] = "annis_autoupgrade" + version[0] + version[1] + version[2]
		print("New schema name: " + dbconfig["datasource.schema"])
	print("======================================================")
	initDatabase(dbconfig, extracted)
	copyDatabase(extracted, args.dir, args.mail)
	copiedCorpora = True

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

if copiedCorpora:
	
	if args.cleanup_data:
		cleanupData(args.dir)
	
	print("============================================================")
	print("Finished! Please remember to delete the old database schema.")
else:
	print("=========")
	print("Finished!")
