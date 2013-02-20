ANNIS Query Language (AQL) {#dev-aql}
====================

Complete List of Operators
--------------------------

The ANNIS Query Language (AQL) currently includes the following listed operators.

### direct precedence: `.` ###

![(Illustration)](A-pred-B.svg)
\verbatim
A . B
\endverbatim

For non-terminal nodes, precedence is determined by the right most  and left most terminal children.

### indirect precedence: `.*` ###

![(Illustration)](A-pred-ind-B.svg)
\verbatim
A .* B
A .4 B
A .2,4 B
\endverbatim

For specific sizes of precedence spans, `.n,m` can be used, e.g. `.3,4` - between 3 and 4 token distance

### direct dominance: `>` ###

![(Illustration)](A-dom-B.svg)
\verbatim
A > B
A >secedge B
A >secedge[func="OA"] B
\endverbatim

A specific edge type may be specifed, e.g.: `>secedge` to find secondary edges.
Edges labels are specified in brackets, e.g. `>[func="OA"]` for an edge with the
function 'object, accusative.

### indirect dominance: `>*` ###

![(Illustration)](A-dom-ind-B.svg)
\verbatim
A >* B
A >2,4 B
A >secedge[func="OA"] * B
A >[func="OA"] 2,4 B
\endverbatim

For specific distance o dominance, `>n,m` can be used, e.g. `>3,4` - dominates with 3 to 4 edges
distance.

### identical coveragee: `_=_` ###

![(Illustration)](A-cov-ident-B.svg)
\verbatim
A _=_ B
\endverbatim

Applies when two annotation cover the exact same span of tokens.

### inclusion: `_i_` ###

![(Illustration)](A-cov-incl-B.svg)
\verbatim
A _i_ B
\endverbatim

Applies when one annotation covers a span identical to or larger than another.

### overlap: `_o_` ###

![(Illustration)](A-cov-over-B.svg)
\verbatim
A _o_ B
A _ol_ B
A _or_ B
\endverbatim

For overlap only on the left or right side, use `_ol_` and `_or_` respectively.

### left aligned: `_l_` ###

![(Illustration)](A-cov-left-B.svg)
\verbatim
A _l_ B
\endverbatim

Both elements span an area beginning with the same token.

### right aligned: `_r_` ###

![(Illustration)](A-cov-right-B.svg)
\verbatim
A _r_ B
\endverbatim

Both elements span an area ending with the same token.


