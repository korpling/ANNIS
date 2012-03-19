DROP TABLE IF EXISTS _nodeidmapping;
CREATE UNLOGGED TABLE _nodeidmapping 
(
  "old" bigint, "new" bigint,
  PRIMARY KEY ("old"),
  UNIQUE("new")
);

INSERT INTO _nodeidmapping("old", "new")
SELECT id AS "old", row_number() OVER () AS "new"
FROM
(
  SELECT n.id AS id
  FROM _node AS n, _corpus AS c, _text as t
  WHERE
    n.corpus_ref = c.id AND n.text_ref = t.id
  ORDER BY c.name, t.name, n.left_token, n."name"
) as ordered;

UPDATE _node_annotation SET node_ref = 
  (SELECT "new" FROM _nodeidmapping WHERE "old" = node_ref) 
  + (SELECT  max_node_id FROM corpus_stats WHERE id = :id);
    
UPDATE _rank
SET 
 pre = pre + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 post = post + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 node_ref = (SELECT "new" FROM _nodeidmapping WHERE "old" = node_ref) 
            + (SELECT max_node_id FROM corpus_stats WHERE id = :id),
 parent = parent + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 component_ref = component_ref + (SELECT max_component_id FROM corpus_stats WHERE id = :id);

UPDATE _component SET id = id + (SELECT max_component_id FROM corpus_stats WHERE id = :id);

UPDATE _edge_annotation SET rank_ref = rank_ref + (SELECT  max_rank_post FROM corpus_stats WHERE id = :id);
    
UPDATE _node
SET 
 id = (SELECT "new" FROM _nodeidmapping WHERE "old" = id) 
      + (SELECT max_node_id FROM corpus_stats WHERE id = :id),
 text_ref = text_ref + (SELECT  max_text_id FROM corpus_stats WHERE id = :id),
 corpus_ref = corpus_ref + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id),
 toplevel_corpus = toplevel_corpus + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id);
    
UPDATE _text SET id = id + (SELECT  max_text_id FROM corpus_stats WHERE id = :id);
    
UPDATE _corpus
SET 
 id = id + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id),
 pre = pre + (SELECT max_corpus_post FROM corpus_stats WHERE id = :id),
 post = post + (SELECT max_corpus_post FROM corpus_stats WHERE id = :id);

UPDATE _corpus_annotation SET corpus_ref = corpus_ref + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id);

DROP TABLE _nodeidmapping;