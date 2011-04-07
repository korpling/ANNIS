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

SELECT DISTINCT :id, e.namespace, e.name, e.value, count(e.value) as occurences,
  'edge', c.type, c.namespace, c.name
FROM _edge_annotation as e, _rank as r, _component as c
WHERE e.rank_ref = r.pre AND r.component_ref = c.id
      AND e.name is not null and c.name is not null
GROUP BY e.namespace, e.name, e.value, c.type, c.namespace, c.name
;

SET enable_hashagg = true;