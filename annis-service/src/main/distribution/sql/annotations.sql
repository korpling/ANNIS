--- :id is replaced by code

SET enable_hashagg = false;

DROP TABLE IF EXISTS annotations_:id;

CREATE TABLE annotations_:id
(
  -- check constraints
  CHECK(toplevel_corpus = :id)
)
INHERITS (annotations);

INSERT INTO annotations_:id
(
  toplevel_corpus,
	namespace,
  name,
  value,
  occurences,
  "type",
  subtype,
  edge_namespace,
  edge_name
)
SELECT :id, namespace, name, value, count(value) as occurences, 
  'node', 'n', NULL, NULL
FROM _node_annotation
WHERE
name is not null
GROUP BY namespace, name, value

UNION ALL

SELECT DISTINCT :id, e.namespace, e.name, e.value, count(r.id) as occurences,
  'edge', c.type, c.namespace, c.name
FROM _rank as r JOIN _component as c ON (r.component_ref = c.id) LEFT OUTER JOIN _edge_annotation as e ON (e.rank_ref = r.id)
WHERE 
      c.name IS NOT NULL
GROUP BY e.namespace, e.name, e.value, c.type, c.namespace, c.name

UNION ALL

SELECT DISTINCT :id, NULL as node_namespace, n.seg_name, NULL AS VALUE, count(n.seg_name) AS occurences,
  'segmentation', NULL AS sub_type, NULL AS edge_namespace, NULL AS edge_name
FROM _node AS n
WHERE n.seg_name IS NOT NULL
GROUP BY(n.seg_name)
;



SET enable_hashagg = true;