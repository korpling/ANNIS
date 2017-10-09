#!/usr/bin/python3

import json
import io
from subprocess import call

milestone_id = "55"

import http.client

conn = http.client.HTTPSConnection("api.github.com")

payload = ""

headers = { 'User-Agent': 'python3', 'accept': "application/vnd.github.beta.full+json" }

conn.request("GET", "/repos/korpling/ANNIS/issues?state=closed&milestone=" + milestone_id + "&sort=created", payload, headers)

res = conn.getresponse()
data = res.read()
data_str = data.decode("utf-8")

j = json.loads(data_str)

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
	print("[Fixed Bugs]")
	for t in bugs:
		print(t)
if len(enhancements) > 0:
	print("")
	print("[Enhancements]")
	for t in enhancements:
		print(t)

if len(other) > 0:
	print("")
	print("[Other]")
	for t in other:
		print(t)
		
