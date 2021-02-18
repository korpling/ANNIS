# Searching for Word Forms

To search for word forms in ANNIS, simply select a corpus (in this
example the freely available [GUM corpus](https://corpling.uis.georgetown.edu/gum/)) and enter a search string
between double quotation marks, e.g.:
```
"do"
```

Note that the search is **case sensitive**, so it will not find cases of
capitalized 'Do', for example at the beginning of a sentence. In order
to find both options, you can either look for one form OR the other
using the pipe sign ( `|` ):
```
"do" | "Do"
```

or else you can use [regular expressions](aql-regex.md), which must
be surrounded by slashes ( `/` ) instead of quotation marks:
```
/[Dd]o/
```

To look for a sequence of multiple word forms, enter your search terms
separated by `&` and then specify that the relation between the elements
is one of **precedence**, as signified by the period ( **`.`** )
operator:
```
"do" & "n't" & #1 . #2
```

The expression `#1 . #2` signifies that the first element ("do")
precedes the second element ("n't"). Alternatively, you can also place
the operator directly between the search elements as a **shortcut**. The
following shortcut query is equivalent to the one above:
```
"do" . "n't"`
```
For **indirect precedence** (where other tokens may stand between the
search terms), use the **`.*`** operator:

```
/[Dd]o/ & "n't" & "any" & #1 . #2 & #2 .* #3 
```

**OR using shortcuts:**

```
/[Dd]o/ . "n't" .* "any"
```

The queries above find sequences beginning with the token "Do" or "do",
followed directly by "n't", which must be followed either directly or
indirectly (.\*) by "any". A range of allowed distances can also be
specified numerically as follows:

```
/[Nn]ot/ & "all" & #1 .1,5 #2
```

**OR:**

```
/[Nn]ot/ .1,5 "all"
```

Meaning the two words "not" and "all" may appear at a distance of 1 to 5
tokens. The operator `.*` allows a distance of up to 50 tokens by
default, so searching with `.1,50` is the same as using `.*` instead.
Greater distances (e.g. `.1,100` for 'within 100 tokens') should always
be specified explicitly.

Finally, we can add metadata restrictions to the query, which filter out
documents not matching our definitions. Metadata attributes must be connected to
other non-meta attributes with the `@*` (part-of) operator:

```
"want" & "to" & #1 .1,5 #2
& type="interview" & #1 @* #2 
```

To view metadata for a search result or for a corpus, press the "i" icon
next to it in the result window or in the search form respectively.
