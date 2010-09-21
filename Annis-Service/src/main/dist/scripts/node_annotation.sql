DROP TABLE IF EXISTS node_annotation_:id;
CREATE TABLE node_annotation_:id
(
  CHECK(toplevel_corpus = :id),
  FOREIGN KEY (node_ref) REFERENCES node_:id
)
INHERITS ( node_annotation) ;

INSERT INTO node_annotation_:id
SELECT a.node_ref, n.toplevel_corpus, a.namespace, a.name, a.value
FROM _node_annotation as a, _node as n
WHERE toplevel_corpus = :id AND a.node_ref = n.id;