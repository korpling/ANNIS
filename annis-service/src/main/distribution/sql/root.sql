-- compute real roots
-- actually, roots of components that are not actual roots should link parent to their parent node (even though it is in another component)

ALTER TABLE _node ADD root boolean;

UPDATE _node n SET root=
(SELECT count(distinct _rank.parent) = 0 FROM _rank WHERE _rank.node_ref = n.id);

-- rank was changed, reanalyze it
ANALYZE _node;
