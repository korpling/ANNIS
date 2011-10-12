BEGIN {
  issql=0
  iscomment=0
}

/^SELECT.*/ {
  issql=1
}

/^Time: .* ms$/ {
  issql=0
}

/^[ \t]*--.*$/ {
  iscomment=1
}

{
  
  if(issql && !iscomment)
  {
    printf("%s ", $0)
  }

  # reset comment state
  iscomment=0
}

END {
  print ""
}
