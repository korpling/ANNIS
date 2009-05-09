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
	(select name from _corpus where top_level = 't') as name,
	(select id from _corpus where top_level = 't') as id,
    
    -- row counts
    (select count(*) from _corpus) as corpus,    
    (select count(*) from _text) as text,
    (select count(*) from _node) as node,    
    (select count(*) from _rank) as rank,
    (select count(*) from _component) as component,
    (select count(*) from _corpus_annotation) as corpus_annotation,
    (select count(*) from _node_annotation) as node_annotation,
    (select count(*) from _edge_annotation) as edge_annotation,    
    
    -- # tokens
    (SELECT count(*) FROM _node WHERE token_index IS NOT NULL) as tokens,

    -- # root elements
    (SELECT count(distinct node_ref) FROM _rank WHERE root) as roots,

    -- max depth
    (SELECT max(level) FROM _rank) as depth,
    
    -- distinct edge types
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'c') AS dictinct_edge_types) as c,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'd') AS dictinct_edge_types) as d,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'p') AS dictinct_edge_types) as p,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type NOT IN ('c', 'p', 'u')) AS dictinct_edge_types) as u,

    -- avg depth
    (select sum(count * level) / sum (count) from (select count(*), level from _rank group by level) as tmp) as avg_level,

    -- avg children per node
    (select avg(count) from (select count(pre) from _rank where parent is not null group by parent) as tmp) as avg_children,

    -- avg copies in rank per node
    (select avg(count) from (select count(pre), node_ref from _rank group by node_ref) as tmp) as avg_duplicates
;
