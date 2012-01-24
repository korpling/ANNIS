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

    -- # actual edges
    (SELECT sum(parents) FROM (
		SELECT DISTINCT 
			n_this.name,
			count(DISTINCT r_parent.node_ref) AS parents
		FROM 
			_node n_this 
			JOIN _rank r_this ON (r_this.node_ref = n_this.id)
			LEFT JOIN _rank r_parent ON (r_parent.pre = r_this.parent)
		GROUP BY n_this.name
		) t
	) AS edges,
	
    -- max depth
    (SELECT max(level) FROM _rank) as depth,
    
    -- distinct edge types
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'c') AS dictinct_edge_types) as coverage_components,
    (SELECT count(pre) FROM _rank JOIN _component ON (component_ref = id) WHERE type = 'c') as coverage_edges,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'd') AS dictinct_edge_types) as dominance_components,
    (SELECT count(pre) FROM _rank JOIN _component ON (component_ref = id) WHERE type = 'd') as dominance_edges,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type = 'p') AS dictinct_edge_types) as pointing_components,
    (SELECT count(pre) FROM _rank JOIN _component ON (component_ref = id) WHERE type = 'p') as pointing_edges,
    (SELECT count(*) FROM (SELECT DISTINCT namespace, name FROM _component WHERE type NOT IN ('c', 'd', 'p') OR type IS NULL) AS dictinct_edge_types) as unknown_components,
    (SELECT count(pre) FROM _rank JOIN _component ON (component_ref = id) WHERE type NOT IN ('c', 'd', 'p') OR type IS NULL) as unknown_edges,

    -- avg depth
    (SELECT avg(level) FROM _rank) AS avg_level,

    -- avg children per node
    (SELECT avg(parents) FROM (
		SELECT DISTINCT 
			n_this.name,
			count(DISTINCT r_parent.node_ref) AS parents
		FROM 
			_node n_this 
			JOIN _rank r_this ON (r_this.node_ref = n_this.id)
			LEFT JOIN _rank r_parent ON (r_parent.pre = r_this.parent)
		GROUP BY n_this.name
		) t
	) AS avg_children,

    -- avg duplicates in rank per node
    (SELECT avg(count - 1) FROM (SELECT count(pre) FROM _rank GROUP BY node_ref) t) AS avg_duplicates,

    -- max corpus id
    (SELECT max(id) + 1 FROM _corpus) AS max_corpus_id,

    -- max corpus pre
    (SELECT max(pre) + 1 FROM _corpus) AS max_corpus_pre,

    -- max corpus post
    (SELECT max(post) + 1 FROM _corpus) AS max_corpus_post,

    -- max text id
    (SELECT max(id) + 1 FROM _text) AS max_text_id

    -- the last values where copied in corpus_stats_upd.sql    
;
