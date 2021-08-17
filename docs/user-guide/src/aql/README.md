# ANNIS Query Language (AQL)

ANNIS comes with its own query language called ANNIS Query Language (AQL).
AQL is based on the concept of searching for annotation attributes and relations between them.

A search is formulated by defining each token, non-terminal node or annotation being searched for as an element. An element can be a token (simply text between quotes: `"dogs"` or else `tok="dogs"`) or an attribute-value pair (such as `tok="dogs"`, or optionally with a namespace: `tiger:cat="PP"`). Note that different corpora can have completely different annotation names and values - these are not specified by ANNIS. Underspecified tokens or nodes in general may be specified using `tok` and `node` respectively.

Once all elements are declared, relations between the elements (or edges) are specified which must hold between them. The elements are referred back to serially using variable numbers, and linguistic operators bind them together, e.g. `#1 > #2` meaning the first element dominates the second in a tree or graph. 
Operators define the possible overlap and adjacency relations between annotation spans, as well as recursive hierarchical relations between nodes. Some operators also allow specific labes to be specified in addition to the operator (see the [operator list](operators.html)). 