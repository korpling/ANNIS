#!/usr/bin/python

import re

# regexp zum bestimmen der interessanten daten im logfile
re_comment = re.compile("([^,]+),... INFO Benchmark - (.*)")
re_query = re.compile(".* INFO Benchmark - query: (.*)")
re_runtime = re.compile(".* INFO QueryExecution - done \((.+) ms\)")
re_matches = re.compile(".* INFO Benchmark - matches found: (.+)")

# count_matches = False
count_matches = True

def parse_log(log):	
	"""datum und kommentar des Test-Runs bestimmen"""
	m = re_comment.search(log[0])
	timestamp = m.group(1)
	comment = m.group(2)
	log = log[1:]

	runs = []

	while len(log) > 0:
		# der aktuelle query
		query = re_query.search(log[0]).group(1)

		# laufzeit
		runtime_1 = re_runtime.search(log[2]).group(1)

		# anzahl suchergebnisse
		matches = re_matches.search(log[3]).group(1)

		if int(matches) > 0 and count_matches:
			runtime_2 = re_runtime.search(log[5]).group(1)
			log = log[6:]

		else:
			runtime_2 = 0
			log = log[4:]

		run = (query, matches, runtime_1, runtime_2)
		runs.append(run)
	
	return (timestamp, comment, runs)

def aggregate_compare(x, y):
	x = x[0]
	y = y[0]
	if x == y:
		return 0
	elif x < y:
		return -1
	else:
		return +1

def aggregate(runs):
	runs.sort(aggregate_compare)

	aggregate = []
	
	query = None
	matches = runtime_1 = runtime_2 = count = 0
	
	for run in runs:
		if run[0] != query: 
			if count != 0:
				run_avg = (query, matches, runtime_1 / count, runtime_2 / count)
				aggregate.append(run_avg)
				runtime_1 = runtime_2 = count = 0
			query = run[0]
			matches = run[1]
		
		runtime_1 += int(run[2])
		runtime_2 += int(run[3])
		count += 1
		
	if count != 0:
		run_avg = (query, matches, runtime_1 / count, runtime_2 / count)
		aggregate.append(run_avg)
		
	return aggregate
	
def print_report(timestamp, comment, runs):
	print "Benchmark von", timestamp
	print "---------------------------------"
	print
	print comment
	print
	for run in runs:
		(query, matches, runtime_1, runtime_2) = run
		print "Query:", query
		print "Matches:", matches
		print "Zeit zum Bestimmen der Matches:", runtime_1, "ms"
		print "Zeit zum Bestimmen der Annotationen:", runtime_2, "ms"
		print
	
def print_runs(runs):
	for run in runs:
		print run
	
if __name__ == "__main__":
	# run_tests
	(timestamp, comment, runs) = parse_log(open("log/benchmark.log", "r").readlines())
	# print_runs(runs)
	runs = aggregate(runs)
	# print_report(timestamp, comment, runs)
	print_runs(runs)
