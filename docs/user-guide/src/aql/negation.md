# Operator Negation

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
These operators only act as a filter mechanism, the annotations they refer to still have to exist and have to be bound by other non-negated operators.

