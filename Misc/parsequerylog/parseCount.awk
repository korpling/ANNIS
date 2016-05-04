function outputQuery(query, corpus, runtime)
{
#	print "--------------------------------"
#	print "Query: " query
#	print "Runtime: " runtime " ms"
#	print "Corpus: " corpus 
#	print "--------------------------------"
	print corpus "\t" gensub(/\t/, " ", "g", query) "\t" runtime
}

BEGIN {
	query=""
	print "corpora\tquery\ttime (in ms)"
}


# capture a line with complete input in one line
/^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9](.*)function: COUNT, query: (.*), corpus: \[([^\]]+)\], runtime: [0-9]+ ms$/ {
	where = match($0, /^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9](.*)function: COUNT, query: (.*), corpus: \[([^\]]+)\], runtime: ([0-9]+) ms$/, ary)
	if(where)
	{
		query= ""
		outputQuery(ary[2], ary[3], ary[4])
		next
	}
}

# capture incomplete
/^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9](.*)function: COUNT, query: (.*)$/ {
	where = match($0, /^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9](.*)function: COUNT, query: (.*)$/, ary)
	if(where)
	{
		query= ary[2]
		next
	}
}

# capture end of incomplete query
/corpus: \[([^\]]+)\], runtime: ([0-9]+) ms$/ {

	if(query != "" && match($0, /(.+), corpus: \[([^\]]+)\], runtime: ([0-9]+) ms$/,ary))
	{
		outputQuery(query " " ary[1], ary[2], ary[3])
	}
	query = ""
	next
}


{
	if(query != "")
	{
		# append the current line
		query = query " " $0
	}
}

