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

-- statistics
CREATE TABLE _corpus_stats AS SELECT
    (select id from _corpus where top_level = 't') as corpus_ref,
    
    -- row counts
    (select count(*) from _node ) as node,    
    (select count(*) from _node_annotation ) as annotation,
    (select count(*) from _rank ) as rank,    
    (select count(*) from _edge_annotation ) as edge_annotation,    
    (select count(*) from _text ) as text,
    (select count(*) from _corpus ) as corpus,    
    (select count(*) from _corpus_annotation) as corpus_annotation,
    
    -- # tokens
    (SELECT count(*) FROM (SELECT count(*) FROM _rank WHERE pre = post - 1 GROUP BY node_ref) AS tmp) as n_tokens,

    -- # root elements
    (SELECT count(*) FROM _rank WHERE level = 0) as n_roots,

    -- max depth
    (SELECT max(level) FROM _rank) as depth,

    -- avg depth
    (select sum(count * level) / sum (count) from (select count(*), level from _rank group by level) as tmp) as avg_level,

    -- avg children per node
    (select avg(count) from (select count(pre) from _rank where parent is not null group by parent) as tmp) as avg_children,

    -- avg copies in rank per node
    (select avg(count) from (select count(pre), node_ref from _rank group by node_ref) as tmp) as avg_duplicates
;
