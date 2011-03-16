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
  subtype
)

SELECT :id, namespace, name, value, count(value) as occurences, 
  'node', 'n'
FROM _node_annotation
WHERE
name is not null
GROUP BY namespace, name, value;

SET enable_hashagg = true;