function cmd()


BEGIN {
  
  pgbench = "sudo -u postgres /usr/lib/postgresql/9.1/bin/pgbench -n -f"
  db = "anniskickstart"
  clear_cache = "sudo echo 3 | tee /proc/sys/vm/drop_caches"
  postgres_restart = "sudo /etc/init.d/postgresql restart"

  FS = "\t"
  counter = 1
}

/^[a-zA-Z0-9_]*.*/ {

  out = "BEGIN;\n"
  
  corpus = $1
  query = $2

  print "new query " counter " ('" query "' on " corpus ")"
    
  # get SQL query
  print "getting SQL query"
  cmd = "annis.sh 'corpus " corpus "' 'sql " query "' | awk -f cleansql.awk"
  sql = ""
  cmd | getline > "cmdout.txt"
  
  out = out sql ";\n"
  out = out "END;\n"  
  
  print "clearing system cache"
  "sync" | getline > "cmdout.txt"
  clear_cache | getline  > "cmdout.txt"
  
  print "restarting PostgreSQL"
  postgres_restart | getline  > "cmdout.txt"
  
  
  print "executing pgbench"  
  print out > "queries/query_" counter ".sql" 
  
  cmd = pgbench " queries/query_" counter ".sql " db " > results/query_" counter ".txt"
  cmd | getline  > "cmdout.txt"
  
  counter = counter+1
}


END {
}
