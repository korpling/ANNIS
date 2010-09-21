DROP TABLE IF EXISTS node_:id;
CREATE TABLE node_:id
(
  CHECK(toplevel_corpus = :id),
  PRIMARY KEY(id)
)
INHERITS (node);

INSERT INTO node_:id
SELECT
  id,
  text_ref,
  corpus_ref,
  namespace,
  name,
  "left",
  "right",
  token_index,
  FALSE AS is_token,
  continuous,
  span,
  toplevel_corpus,
  left_token,
  right_token
FROM _node WHERE toplevel_corpus = :id;
UPDATE node_:id SET is_token=true WHERE token_index IS NOT NULL;