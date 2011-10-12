BEGIN {
  FS = "\t"
}

{
  out = "BEGIN;\n"
  
  corpus $1
  query = $2
  # get SQL query
  cmd = "annis.sh 'corpus " corpus "' 'sql " query "' | awk -f cleansql.awk"
  sql = ""
  cmd | getline sql
  print "SQL: " sql
  
  out = "END;\n"  
}

END {
}
