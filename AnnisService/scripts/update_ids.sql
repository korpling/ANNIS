CREATE TEMPORARY TABLE _max (
    corpus_id numeric(38) NULL,
    corpus_post numeric(38) NULL,
    rank_post numeric(38) NULL,
    component_id numeric(38) NULL,
    node_id numeric(38) NULL,
    text_id numeric(38) NULL
);

INSERT INTO _max VALUES (
    (SELECT max(id) + 1 FROM corpus),
    (SELECT max(post) + 1 FROM corpus),
    (SELECT max(post) + 1 FROM rank), 
    (SELECT max(id) + 1 FROM component),
    (SELECT max(id) + 1 FROM node),
    (SELECT max(id) + 1 FROM text)
);

UPDATE _max SET corpus_id = 0 WHERE corpus_id IS NULL;
UPDATE _max SET corpus_post = 0 WHERE corpus_post IS NULL;
UPDATE _max SET rank_post = 0 WHERE rank_post IS NULL;
UPDATE _max SET component_id = 0 WHERE component_id IS NULL;
UPDATE _max SET node_id = 0 WHERE node_id IS NULL;
UPDATE _max SET text_id = 0 WHERE text_id IS NULL;

UPDATE _node_annotation SET node_ref = node_ref + (SELECT node_id FROM _max);
    
UPDATE _rank SET pre = pre + (SELECT rank_post FROM _max);
UPDATE _rank SET post = post + (SELECT rank_post FROM _max);
UPDATE _rank SET node_ref = node_ref + (SELECT node_id FROM _max);
UPDATE _rank SET parent = parent + (SELECT rank_post FROM _max);
UPDATE _rank SET component_ref = component_ref + (SELECT component_id FROM _max);

UPDATE _component SET id = id + (SELECT component_id FROM _max);

UPDATE _edge_annotation SET rank_ref = rank_ref + (SELECT rank_post FROM _max);
    
UPDATE _node SET id = id + (SELECT node_id FROM _max);
UPDATE _node SET text_ref = text_ref + (SELECT text_id FROM _max);
UPDATE _node SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);
UPDATE _node SET toplevel_corpus = toplevel_corpus + (SELECT corpus_id FROM _max);
    
UPDATE _text SET id = id + (SELECT text_id FROM _max);
    
UPDATE _corpus SET id = id + (SELECT corpus_id FROM _max);
UPDATE _corpus SET pre = pre + (SELECT corpus_post FROM _max);
UPDATE _corpus SET post = post + (SELECT corpus_post FROM _max);

UPDATE _corpus_stats SET id = id + (SELECT corpus_id FROM _max);

UPDATE _corpus_annotation SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);

DROP TABLE _max;
