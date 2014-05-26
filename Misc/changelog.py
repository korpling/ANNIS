#!/usr/bin/python

import json
import io
from subprocess import call

milestone_id = "15"

call(["curl", "-H", "Accept: application/vnd.github.beta.full+json", "-o", "issues.json", "https://api.github.com/repos/korpling/ANNIS/issues?state=closed&milestone=" + milestone_id + "&sort=created"])

f = open("issues.json")
j = json.load(f)

bugs = []
enhancements = []
other = []

for issue in j:
	title = " #{0} {1}".format(issue["number"], issue["title"])
	if len(issue["labels"]) > 0:
		if issue["labels"][0]["name"] == "bug":
			bugs.append(title)
		elif issue["labels"][0]["name"] == "enhancement":
			enhancements.append(title)
		else:
			other.append(title)
	else:
		other.append(title)

if len(bugs) > 0:
	print "[Bugs]"
	for t in bugs:
		print t
if len(enhancements) > 0:
	print ""
	print "[Enhancements]"
	for t in enhancements:
		print t

if len(other) > 0:
	print ""
	print "[Other]"
	for t in other:
		print t
		
