# Differences in Compatibility Mode

Previous versions of ANNIS might have a slightly different interpretation of the semantics of AQL.
By choosing the compatibility mode in the search options, you can emulate the old behavior.
The compatibility mode (also called "quirks mode") has the following differences to the standard AQL semantics.

## Indirect Precedence is limited to a maximum distance of 50

Indirect precedence (`.*`) was limited to a maximum distance of 50 in older versions for performance reasons.
E.g. the query `tok .* tok` is effectively `tok .1,50 tok`.
Depending on the text length of the documents, the compatibility mode might return fewer matches than the normal mode.

## Non-reflexive operator is only applied locally

The operands of non-reflexive operators must be different.
In standard AQL, a new match can be only included if the new match is different to all other matches.
In contrast, the compatibility mode only enforces direct non-reflexivity.
E.g. in the query
```
node  & node & node & #1 _o_ #2 & #2 _o_ #3
``` 
the first node must be different from the second node and the second node must be different from the third.
In compatibility mode, it is allowed that `#1` and `#3` reference the same match because there is no 
non-reflexive operator directly between them.
You would need to change the query to
```
node  & node & node & #1 _o_ #2 & #2 _o_ #3 & #1 _o_ #3
``` 
to enforce that these operand are not the same.
The standard mode will not include a match if `#1` and `#3` reference the same match per default.
This does not affect the addition of matches if the operator is reflexive.

## Meta-data constraints are applied to all conjunctions of a disjunction

Meta-data constraints like `meta::doc="mydocumentname"` are applied to all conjunctions of the disjunction.
E.g the query.
```
(pos="NN" | lemma="be" & meta::age="20")
```
effectively becomes
```
(pos="NN" & meta::age="20" | lemma="be" & meta::age="20")
```