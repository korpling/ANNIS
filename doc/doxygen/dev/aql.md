ANNIS Query Language (AQL) {#dev-aql}
====================

[TOC]

Complete List of Operators {#dev-aql-oplist}
=========================

The ANNIS Query Language (AQL) currently includes the following listed operators.

## direct precedence: "." ## {#dev-aql-directprec}

![(Illustration)](A-pred-B.svg)
\verbatim
A & B & #1 . #2
\endverbatim

For non-terminal nodes, precedence is determined by the right most  and left most terminal children.

## indirect precedence: ".*" ## {#dev-aql-indirectprec}

![(Illustration)](A-pred-ind-B.svg)
\verbatim
A & B & #1 .* #2
A & B & #1 .4 #2
A & B & #1 .2,4 #2
\endverbatim

For specific sizes of precedence spans, `.n,m` can be used, e.g. `.3,4` - between 3 and 4 token distance

## direct dominance: ">" ## {#dev-aql-directdom}

![(Illustration)](A-dom-B.svg)
\verbatim
A & B & #1 > #2
A & B & #1 >secedge #2
A & B & #1 >secedge[func="OA"] #2
\endverbatim

A specific edge type may be specifed, e.g.: `>secedge` to find secondary edges.
Edges labels are specified in brackets, e.g. `>[func="OA"]` for an edge with the
function 'object, accusative.

## indirect dominance: ">*" ## {#dev-aql-indirectprec}

![(Illustration)](A-dom-ind-B.svg)
\verbatim
A & B & #1 >* #2
A & B & #1 >2,4 #2
A & B & #1 >secedge[func="OA"] * #2
A & B & #1 >[func="OA"] 2,4 #2
\endverbatim

For specific distance o dominance, `>n,m` can be used, e.g. `>3,4` - dominates with 3 to 4 edges
distance.

## identical coveragee: "_=_" ## {#dev-aql-identcov}

![(Illustration)](A-cov-ident-B.svg)
\verbatim
A & B & #1 _=_ #2
\endverbatim

Applies when two annotation cover the exact same span of tokens.

## inclusion: "_i_" ## {#dev-aql-inclusion}

![(Illustration)](A-cov-incl-B.svg)
\verbatim
A & B & #1 _i_ #2
\endverbatim

Applies when one annotation covers a span identical to or larger than another.

## overlap: "_o_" ## {#dev-aql-overlap}

![(Illustration)](A-cov-over-B.svg)
\verbatim
A & B & #1 _o_ #2
A & B & #1 _ol_ #2
A & B & #1 _or_ #2
\endverbatim

For overlap only on the left or right side, use `_ol_` and `_or_` respectively.

## left aligned: "_l_" ## {#dev-aql-leftalign}

![(Illustration)](A-cov-left-B.svg)
\verbatim
A & B & #1 _l_ #2
\endverbatim

Both elements span an area beginning with the same token.

## right aligned: "_r_" ## {#dev-aql-rightalign}

![(Illustration)](A-cov-right-B.svg)
\verbatim
A & B & #1 _r_ #2
\endverbatim

Both elements span an area ending with the same token.

## directly near: "^" ## {#dev-aql-directnear}

\verbatim
A & B & #1 ^ #2
\endverbatim

Elements are next to each other in any order (A-B or B-A). For non-terminal nodes, adjacency is determined by the right most and left most terminal children.

## indirectly near: "^*" ## {#dev-aql-indirectnear}

\verbatim
A & B & #1 ^* #2
A & B & #1 ^4 #2
A & B & #1 ^2,4 #2
\endverbatim

For specific sizes of proximity spans, `.n,m` can be used, e.g. `.3,4` - between 3 and 4 token distance in either order (A...B or B...A)

## labeled direct pointing relation: "->LABEL" ## {#dev-aql-directpoint}

![(Illustration)](A-point-direct-B.svg)
\verbatim
A & B & #1 ->LABEL #2
A & B & #1 ->LABEL[annotation="VALUE"] #2
\endverbatim

A labeled, directed relationship between two
elements. Annotations can be specified with `->LABEL[annotation="VALUE"]`

## labeled indirect pointing relation: "->LABEL *" ## {#dev-aql-indirectpoint}

![(Illustration)](A-point-ind-B.svg)
\verbatim
A & B & #1 ->LABEL * #2
A & B & #1 ->LABEL 4,7 #2
\endverbatim

An indirect labeled relationship between two elements. The length
of the chain may be specified with `->LABEL n,m` for relation chains of
length `n` to `m`.

## left-most child: ">@l" ## {#dev-aql-leftchild}

![(Illustration)](A-left-B.svg)
\verbatim
A & B & #1 >@l #2
\endverbatim

## right-most child: ">@r" ## {#dev-aql-rightchild}

![(Illustration)](A-right-B.svg)
\verbatim
A & B & #1 >@r #2
\endverbatim

## common parent node: ">$" ## {#dev-aql-commonparent}

![(Illustration)](A-parent-B.svg)
\verbatim
A & B & #1 >$ #2
\endverbatim

## common ancestor node: ">$*" ## {#dev-aql-commonancestor}

![(Illustration)](A-ancestor-B.svg)
\verbatim
A & B & #1 >$* #2
\endverbatim

## arity: "#x:arity=n" ## {#dev-aql-arity}

![(Illustration)](A-arity.svg)
\verbatim
A & #1:arity=2
\endverbatim

Specifies the amount of directly dominated children that the
searched node has.

## length: "#x:length=n" ## {#dev-aql-length}

![(Illustration)](A-length.svg)
\verbatim
A & #1:length=2
\endverbatim

Specifies the length of the span of tokens covered by the node.

## root: "#x:root" ## {#dev-aql-root}

![(Illustration)](A-root.svg)
\verbatim
A & #1:root
\endverbatim

node x is the root of a subgraph (i.e. it is not dominated by any node)
