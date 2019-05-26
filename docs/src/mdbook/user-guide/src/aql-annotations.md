# Searching for Annotations

Annotations may be searched for using an annotation name and value. The
names of the annotations vary from corpus to corpus, though many corpora
contain part-of-speech and lemma annotations with the names `pos` and
`lemma` respectively (annotation names are **case sensitive**). For
example, to search for all forms of the verb *be* in the GUM corpus,
simply select the GUM corpus and enter:
```
lemma="be"
```

Negative searches are also possible using `!=` instead of `=`. For negated
tokens (word forms) use the reserved attribute tok. For example:
```
lemma!="be"
```

or:
```
tok!="be" 
```

Metadata attributes can also be negated:

```
lemma="be" @* type!="interview"
```

To only find finite forms of a verb in GUM, use the part-of-speech (pos)
annotation concurrently with lemma, and specify that both the lemma and
pos should apply to the same element. For example for inflected forms of
the verb *give*:

```
lemma="give" & pos=/VV.+/ & #1 _=_ #2
```

OR (using a shortcut):

```
lemma="give" _=_ pos=/VV.+/
```

The regular expression `/VV.+/` means a part of speach that begins with
VV (verb), but has additional characters (.+), such as for past tense
(VVD) or gerund (VVG). The expression `#1 _=_ #2` uses the span identity
operator to specify that the first annotation and the second annotation
apply to exactly the same position in the corpus.

Annotations can also apply to longer spans than a single token: for
example, in GUM, the annotation `entity` signifies the entity type of a
discourse referent. This annotation can also apply to phrases longer
than one token. The following query finds spans containing a discourse
referent who is a person:

```
entity="person"
```

If the corpus contains more than one annotation type named `entity`, a
namespace may be added to disambiguate these annotations (for example,
the entity annotation in the GUM corpus has the namespace `ref:`, so we
can search for `ref:entity="person"`). The namespace may always be
dropped, but if there are multiple annotations with the same name but
different namespaces, dropping the namespace will find all of those
annotations. If you drop the value of the annotation, you can also
search for any corpus positions that have that annotation, without
constraining the value. For example, the following query finds all
annotated entities in the GUM corpus, whether or not they are a person:

```
entity
```

In order to view the span of tokens to which the entity annotation
applies, enter the query and click on "Search", then open the
*referents* layer to view the grid containing the span.

Further operators can test the relationships between potentially
overlapping annotations in spans. For example, the operator `_i_`
examines whether one annotation fully contains the span of another
annotation (the *i* stands for 'includes'):

```
head & infstat="new" & #1 _i_ #2
```

OR (using a shortcut):

```
head _i_ infstat="new"
```

This query finds information structurally new discourse referents
(`infstat="new"`) contained within headings (`head`).