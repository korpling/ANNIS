CREATE TEMPORARY TABLE _max (
    anno_id numeric(38) NULL,
    rank_pre numeric(38) NULL,
    rank_post numeric(38) NULL,
    struct_id numeric(38) NULL,
    text_id numeric(38) NULL,
    corpus_id numeric(38) NULL,
    corpus_post numeric(38) NULL,
    doc_id numeric(38) NULL,
    col_id numeric(38) NULL,
    col_pre numeric(38) NULL,
    col_post numeric(38) NULL,
    rank_zshg numeric(38) NULL
);

INSERT INTO _max VALUES (
    (SELECT max(id) + 1 FROM anno), 
    (SELECT max(pre) + 1 FROM rank),
    (SELECT max(post) + 1 FROM rank), 
    (SELECT max(id) + 1 FROM struct),
    (SELECT max(id) + 1 FROM text),
    (SELECT max(id) + 1 FROM corpus),
    (SELECT max(post) + 1 FROM corpus),
    (SELECT max(id) + 1 FROM document),
    (SELECT max(id) + 1 FROM collection),
    (SELECT max(pre) + 1 FROM col_rank),
    (SELECT max(post) + 1 FROM col_rank),
    (SELECT max(zshg) + 1 FROM rank)
);

UPDATE _max SET anno_id = 0 WHERE anno_id IS NULL;
UPDATE _max SET rank_pre = 0 WHERE rank_pre IS NULL;
UPDATE _max SET rank_post = 0 WHERE rank_post IS NULL;
UPDATE _max SET struct_id = 0 WHERE struct_id IS NULL;
UPDATE _max SET text_id = 0 WHERE text_id IS NULL;
UPDATE _max SET corpus_id = 0 WHERE corpus_id IS NULL;
UPDATE _max SET corpus_post = 0 WHERE corpus_post IS NULL;
UPDATE _max SET doc_id = 0 WHERE doc_id IS NULL;
UPDATE _max SET col_id = 0 WHERE col_id IS NULL;
UPDATE _max SET col_pre = 0 WHERE col_pre IS NULL;
UPDATE _max SET col_post = 0 WHERE col_post IS NULL;
UPDATE _max SET rank_zshg = 0 WHERE rank_zshg IS NULL;

UPDATE _anno SET id = id + (SELECT anno_id FROM _max);
UPDATE _anno SET struct_ref = struct_ref + (SELECT struct_id FROM _max);
UPDATE _anno SET col_ref = col_ref + (SELECT col_id FROM _max);
    
UPDATE _anno_attribute SET anno_ref = anno_ref + (SELECT anno_id FROM _max);

UPDATE _rank SET pre = pre + (SELECT rank_post FROM _max);
UPDATE _rank SET post = post + (SELECT rank_post FROM _max);
UPDATE _rank SET struct_ref = struct_ref + (SELECT struct_id FROM _max);
UPDATE _rank SET parent = parent + (SELECT rank_post FROM _max);
UPDATE _rank SET zshg = zshg + (SELECT rank_zshg FROM _max);
    
UPDATE _rank_anno SET rank_ref = rank_ref + (SELECT rank_post FROM _max);
    
UPDATE _struct SET id = id + (SELECT struct_id FROM _max);
UPDATE _struct SET text_ref = text_ref + (SELECT text_id FROM _max);
UPDATE _struct SET doc_ref = doc_ref + (SELECT doc_id FROM _max);
UPDATE _struct SET col_ref = col_ref + (SELECT col_id FROM _max);
-- UPDATE _struct SET corp_ref = corp_ref + (SELECT corpus_id FROM _max);
    
UPDATE _text SET id = id + (SELECT text_id FROM _max);
UPDATE _text SET col_ref = col_ref + (SELECT col_id FROM _max);
    
UPDATE _corpus SET id = id + (SELECT corpus_id FROM _max);
UPDATE _corpus SET pre = pre + (SELECT corpus_post FROM _max);
UPDATE _corpus SET post = post + (SELECT corpus_post FROM _max);

UPDATE _corpus_stats SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);

UPDATE _doc_2_corp SET doc_id = doc_id + (SELECT doc_id FROM _max);
UPDATE _doc_2_corp SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);

UPDATE _document SET id = id + (SELECT doc_id FROM _max);

UPDATE _collection SET id = id + (SELECT col_id FROM _max);

UPDATE _col_rank SET pre = pre + (SELECT col_post FROM _max);
UPDATE _col_rank SET post = post + (SELECT col_post FROM _max);
UPDATE _col_rank SET col_ref = col_ref + (SELECT col_id FROM _max);

UPDATE _meta_attribute SET col_ref = col_ref + (SELECT col_id FROM _max);

UPDATE _corp_2_viz SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);
UPDATE _xcorp_2_viz SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);

DROP TABLE _max;
