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
  + COALESCE((SELECT  max_node_id FROM corpus_stats WHERE id = :id), 0);
    
UPDATE _rank
SET 
 pre = pre + COALESCE((SELECT max_rank_post FROM corpus_stats WHERE id = :id),0),
 post = post + COALESCE((SELECT max_rank_post FROM corpus_stats WHERE id = :id), 0),
 node_ref = (SELECT "new" FROM _nodeidmapping WHERE "old" = node_ref) 
            + COALESCE((SELECT max_node_id FROM corpus_stats WHERE id = :id), 0),
 parent = parent + COALESCE((SELECT max_rank_post FROM corpus_stats WHERE id = :id), 0),
 component_ref = component_ref + COALESCE((SELECT max_component_id FROM corpus_stats WHERE id = :id),0);

UPDATE _component SET id = id + COALESCE((SELECT max_component_id FROM corpus_stats WHERE id = :id),0);

UPDATE _edge_annotation SET rank_ref = rank_ref + COALESCE((SELECT  max_rank_post FROM corpus_stats WHERE id = :id), 0);
    
UPDATE _node
SET 
 id = (SELECT "new" FROM _nodeidmapping WHERE "old" = id) 
      + COALESCE((SELECT max_node_id FROM corpus_stats WHERE id = :id),0),
 text_ref = text_ref + COALESCE((SELECT  max_text_id FROM corpus_stats WHERE id = :id),0),
 corpus_ref = corpus_ref + COALESCE((SELECT max_corpus_id FROM corpus_stats WHERE id = :id),0),
 toplevel_corpus = toplevel_corpus + COALESCE((SELECT max_corpus_id FROM corpus_stats WHERE id = :id),0);
    
UPDATE _text SET id = id + COALESCE((SELECT  max_text_id FROM corpus_stats WHERE id = :id),0);
    
UPDATE _corpus
SET 
 id = id + COALESCE((SELECT max_corpus_id FROM corpus_stats WHERE id = :id),0),
 pre = pre + COALESCE((SELECT max_corpus_post FROM corpus_stats WHERE id = :id),0),
 post = post + COALESCE((SELECT max_corpus_post FROM corpus_stats WHERE id = :id),0);

UPDATE _corpus_annotation SET corpus_ref = corpus_ref + COALESCE((SELECT max_corpus_id FROM corpus_stats WHERE id = :id),0);

DROP TABLE _nodeidmapping;