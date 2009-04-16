#!/usr/bin/python

import sys

#conf
context_file_path=sys.argv[1]
path_to_app=sys.argv[1]
context=sys.argv[1]

#code
f = open(context_file_path, 'w')
f.write("""<?xml version="1.0"  encoding="ISO-8859-1"?>
<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.mortbay.org/configure.dtd">

<Configure class="org.mortbay.jetty.webapp.WebAppContext">
<Set name=\"contextPath\">/%s</Set>
<Set name=\"resourceBase\">%s</Set>

</Configure>
""" % (context, path_to_app ))
