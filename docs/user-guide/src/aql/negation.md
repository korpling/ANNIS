# Operator Negation

## Negation with assumption that all annotations exists

Sometimes, you need to find instances in a corpus where a relation is not true between to annotations.
For example, you can search for sentences with a noun and a verb like this:

```
cat="ROOT"
& pos=/N.*/ . pos=/V.*/ 
& #1 >* #2 & #1 >* #3
```

We search for three nodes: the root node, a noun and a verb, and specify that both nodes should be dominated by the root node.
If we now want to get all the instances, where both nodes form a conjunction, we could add another constraint on the noun and verb node using a dependency relation.

```
cat="ROOT"
& pos=/N.*/ . pos=/V.*/ 
& #1 >* #2 & #1 >* #3
& #2 ->dep[func="conj"] #3
```

If we instead want to get all instances where there is **no such dependency relation between the two nodes**, we can use the negated operator `!->`.

```
cat="ROOT"
& pos=/N.*/ . pos=/V.*/ 
& #1 >* #2 & #1 >* #3
& #2 !->dep[func="conj"] #3
```

We can negate all binary operators by adding the `!` prefix to them.
Per default, these operators only act as a filter mechanism, the annotations they refer to still have to exist and have to be bound by other non-negated operators.
In our example, both tokens (the noun defined by `pos/N.*/` and the verb defined by `pos=/V.*/`) have to exist.

## Negation without the assumption of existence

If we want to allow that e.g. the verb might not exist, we have to mark it as **optional** in the AQL query by adding a `?` after the annotation definition (`pos=/V.*/` becomes `pos=/V.*/?`).
For example, to search for root segments without any verb, we can combine the negated dominance operator with an optional verb:
```
cat="ROOT" !>* pos=/V.*/?
```

Since optional annotations might not exist, they are not part of the match result, and they are also not highlighted.

Negated operators must have at least one operand which are non-optional, so 
```
pos="DT"? & "amazing" & #1 !. #2
```
is fine and finds all occurrences of "amazing" without a determiner before it (even if "amazing" is at the beginning of the text), but `pos="DT"? & "amazing"? & #1 !. #2` would be invalid because both operands are optional.
Also, you can only use optional operands for negated operators.