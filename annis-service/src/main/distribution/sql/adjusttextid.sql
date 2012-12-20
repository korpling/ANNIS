ALTER TABLE _text ADD corpus_ref integer;

UPDATE _text AS t SET corpus_ref = (
  SELECT corpus_ref FROM _node WHERE text_ref = t.id LIMIT 1
);

DROP TABLE IF EXISTS _textid_min;
CREATE UNLOGGED TABLE _textid_min (
  corpus_ref integer PRIMARY KEY,
  min_id integer
);

INSERT INTO _textid_min(corpus_ref, min_id)
  SELECT corpus_ref, min(id) as min_id FROM _text GROUP BY corpus_ref;

UPDATE _text AS t SET 
  id = id - (SELECT min_id FROM _textid_min AS m WHERE t.corpus_ref = m.corpus_ref)
;

UPDATE _node AS n SET
  text_ref = text_ref - (SELECT min_id FROM _textid_min AS m WHERE n.corpus_ref = m.corpus_ref)
;

DROP TABLE _textid_min;
