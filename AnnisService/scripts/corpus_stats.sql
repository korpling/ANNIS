-- find the top-level corpus
ALTER TABLE _corpus ADD top_level boolean;
UPDATE _corpus SET top_level = 'n';
UPDATE _corpus SET top_level = 'y' WHERE pre = (SELECT min(pre) FROM _corpus);

-- replace foreign key corpus_id from corpus.timestamp to corpus.id
ALTER TABLE _corp_2_viz ADD corpus_ref numeric(38);
UPDATE _corp_2_viz SET corpus_ref = _corpus.id from _corpus WHERE _corpus.top_level = 't';
ALTER TABLE _corp_2_viz DROP corpus_id;

-- do the same with xcorp_viz
ALTER TABLE _xcorp_2_viz ADD corpus_ref numeric(38);
UPDATE _xcorp_2_viz SET corpus_ref = _corpus.id from _corpus WHERE _corpus.top_level = 't';
ALTER TABLE _xcorp_2_viz DROP corpus_id;

-- cache corpus id in struct
ALTER TABLE _struct ADD corp_ref numeric(38);
UPDATE _struct SET corp_ref = _corpus.timestamp_id FROM _doc_2_corp, _corpus WHERE _struct.doc_ref = _doc_2_corp.doc_id AND _doc_2_corp.corpus_ref = _corpus.id;

-- statistics
CREATE TABLE _corpus_stats AS SELECT
    (select id from _corpus where top_level = 't') as corpus_ref,
    
    -- row counts
    (select count(*) from _struct ) as struct,    
    (select count(*) from _anno ) as anno,
    (select count(*) from _anno_attribute ) as anno_attribute,
    (select count(*) from _rank ) as rank,    
    (select count(*) from _rank_anno ) as rank_anno,    
    (select count(*) from _text ) as text,
    (select count(*) from _corpus ) as corpus,    
    (select count(*) from _doc_2_corp ) as doc_2_corp,    
    (select count(*) from _collection ) as collection,    
    (select count(*) from _col_rank ) as col_rank,
    (select count(*) from _meta_attribute) as meta_attribute,
    
    -- # tokens
    (SELECT count(*) FROM (SELECT count(*) FROM _rank WHERE pre = post - 1 GROUP BY struct_ref) AS tmp) as n_tokens,

    -- # root elements
    (SELECT count(*) FROM _rank WHERE level = 0) as n_roots,

    -- max depth
    (SELECT max(level) FROM _rank) as depth,

    -- avg depth
    (select sum(count * level) / sum (count) from (select count(*), level from _rank group by level) as tmp) as avg_level,

    -- avg children per node
    (select avg(count) from (select count(pre) from _rank where parent is not null group by parent) as tmp) as avg_children,

    -- avg copies in rank per node
    (select avg(count) from (select count(pre), struct_ref from _rank group by struct_ref) as tmp) as avg_duplicates
;
