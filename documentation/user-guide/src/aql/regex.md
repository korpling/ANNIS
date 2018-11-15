# Searching using Regular Expressions

When searching for word forms and annotation values, it is possible to
employ wildcards as placeholders for a variety of characters, using
Regular Expression syntax (see
[here](http://www.regular-expressions.info/) for detailed information).
To search for wildcards use slashes instead of quotation marks to
surround your search term. For example, you can use the **period (`.`)**
to replace any single character:

```
tok=/ca./
```

This finds word forms such as "cat", "can", "car", "cap" etc. It is also
possible to make characters optional by following them with a **question
mark (`?`)**. The following example finds cases of "car" and "cart",
since the "t" is optional:

```
tok=/cart?/
```


It is also possible to specify an arbitrary number of repetitions, with
an **asterisk (`*`)** signifying zero or more occurrences and a **plus
(`+`)** signifying at least one occurrence. For example, the first query
below finds "o", "of", and "off" (since the asterisk means zero or more
times the preceding "f"), while the second finds "of" and "off", since
at least one "f" must be found:

```
tok=/of*/
```

```
tok=/of+/
```  


It is possible to combine these operators with the period operator to
mean any number of occurrences of an arbitrary character. For example,
the query below searches for pos (part-of-speech) annotations that begin
with "VV", corresponding to all forms of lexical verbs (the auxiliaries
"be" and "have" are tagged VB... and VH... respectively). The string
"VV" means that the result must begin with "VV", the period stands for
any character, and the asterisk means that 'any character' can be
repeated zero or more time, as above.

```
pos=/VV.*/
```

This finds both finite verbs ("VVZ", "VVP", "VVD") and non-finite ones
("VV") or gerunds ("VVG"). It is also possible to search for explicit
alternatives by either specifying characters in **square brackets** or
longer strings in **round brackets separated by pipe symbols**. The
first example below finds either "of" or "on" (i.e. "o" followed by
either "f" or "n") while the second example finds lemma annotations that
are either "be" or "have".

```
tok=/o[nf]/
```

```
lemma=/(be|have)/
```  

Finally, negative searches can be used as usual with the exclamation
point, and regular expressions can generally be used also in edge
annotations. For example, if we search for trees (see also [Searching
for Trees](./trees.md)) where a lexical verb dominates another
token with a dependency edge not containing 'obj', we can use a wildcard
to rule out all edges labels containing those letters. This will give us
all non-object dependants of lexical verbs:

```
pos=/VV.*/ & tok & #1 ->dep[func!=/.*obj.*/] #2
```

OR (using a shortcut):

```
pos=/VV.*/ ->dep[func!=/.*obj.*/] tok
```
