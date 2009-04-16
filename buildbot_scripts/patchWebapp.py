#!/usr/bin/python

import os, sys

#conf
path_to_app=sys.argv[1]
rev=sys.argv[2]
user_conf_dir=sys.argv[3]

#code
print "patching config.js (with revision number:" + rev + ")" 
fJS = open(path_to_app + "/javascript/annis/config.js", 'rw')
for s in fJS.xreadlines():
  fJS.write(s.replace("${SVN_REVISION}", rev))
fJS.close()

print "patching web.xml (user-conf-dir:" + user_conf_dir + ")" 
lineWasFound = false
fWeb = open(path_to_app + "/WEB-INF/web.xml", 'rw')
for s in fWeb.xreadlines():
  if lineWasFound:  
    fWeb.write(s)
    stripped=s.strip()
    if stripped == "<param-name>config_path</param-name>":
      lineWasFound = true
  else:
    lineWasFound = false
    fWeb.write("<param-value>%s</param-value>" % user_conf_dir)
fWeb.close()
