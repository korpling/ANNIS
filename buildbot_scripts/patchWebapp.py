#!/usr/bin/python

import os, sys, re

#conf
path_to_app=sys.argv[1]
rev=sys.argv[2]
user_conf_dir=sys.argv[3]

#code
config_js_path=path_to_app + "/javascript/annis/config.js"
print "patching " + config_js_path  + " (with revision number: " + rev + ")" 
fJS = open(config_js_path, "r")
JSContent = fJS.read();
fJS.close();

JSContent = JSContent.replace("'/Annis-web';", "'/Annis2.2';")
regRev = re.compile("var conf_revision='[^']*';")
JSContent = regRev.sub("var conf_revision='" + rev + " (SVN)';", JSContent)
fJS = open(config_js_path, "w")
fJS.write(JSContent)
fJS.close()

web_xml_path=path_to_app + "/WEB-INF/web.xml"
print "patching " + web_xml_path  + " (user-conf-dir: " + user_conf_dir + ")" 
lineWasFound = False
fWeb = open(web_xml_path, 'r')
WebContent = fWeb.read()
fWeb.close()

fWeb = open(web_xml_path, 'w')
WebContent = WebContent.replace("<param-value>/etc/annis/user_config_dev/</param-value>", "<param-value>%s</param-value>\n" % user_conf_dir)
WebContent = WebContent.replace("<param-value>annis.security.TestSecurityManager</param-value>", "<param-value>annis.security.SimpleSecurityManager</param-value>\n")
WebContent = WebContent.replace("<param-value>rmi://localhost:4711/AnnisService</param-value>", "<param-value>rmi://localhost:4780/AnnisService</param-value>")
WebContent = WebContent.replace("<param-value>rmi://localhost:4711/AnnisResolverService</param-value>", "<param-value>rmi://localhost:4780/AnnisResolverService</param-value>");

fWeb.write(WebContent)
fWeb.close()
