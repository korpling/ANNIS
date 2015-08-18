#!/usr/bin/python3

import sh
from sh import ErrorReturnCode
import os
import tarfile
import shutil
import argparse
import tempfile
import re

def parseversion(raw):
	m = re.compile("^([0-9]+)\.([0-9]+)\.([0-9]+)(-SNAPSHOT)? .*").match(raw)
	if m:
		return m.group(1,2,3)
	else:
		return None
		

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

origenv = os.environ.copy();
origenv["ANNIS_HOME"] = args.dir;
extractedenv = os.environ.copy();
extractedenv["ANNIS_HOME"] = extracted;

# check if we can update without any database migration
adminCMD = sh.Command(os.path.join(args.dir, "bin", "annis-admin.sh"))
try: adminCMD("check-db-schema-version", _env=extractedenv)
except ErrorReturnCode:
	print("Can't update service automatically since the new version has a different database scheme!")
	exit(10)
	
serviceCMD = sh.Command(os.path.join(args.dir, "bin", "annis-service.sh"))

try: startupresult = serviceCMD("stop", _env=origenv)
except ErrorReturnCode:
	print(startupresult)
	exit(3)
print(startupresult)

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

try:
	startupresult = serviceCMD("start", _env=origenv, _bg=True)
	startupresult.wait()
except ErrorReturnCode:
	print(startupresult)
	exit(2)
print(startupresult)
