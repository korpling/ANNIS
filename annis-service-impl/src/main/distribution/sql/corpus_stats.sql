-- statistics
CREATE TABLE _corpus_stats AS SELECT
	(select name from _corpus where top_level = 't') as name,
	(select id from _corpus where top_level = 't') as id,

   (select count(*) from _text) as text,
 
    -- # tokens
    (SELECT count(*) FROM _node WHERE token_index IS NOT NULL) as tokens,

    -- # root elements
    (SELECT count(distinct node_ref) FROM _rank WHERE root) as roots,

    -- max depth
    (SELECT max(level) FROM _rank) as depth,

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
			LEFT JOIN _rank r_parent ON (r_parent.pre = r_this.parent AND r_parent.component_ref = r_this.component_ref)
		GROUP BY n_this.name
		) t
	) AS avg_children,

    -- avg duplicates in rank per node
    (SELECT avg(count - 1) FROM (SELECT count(id) FROM _rank GROUP BY node_ref) t) AS avg_duplicates,

    -- max corpus id
    (SELECT max(id) + 1 FROM _corpus) AS max_corpus_id,

    -- max corpus pre
    (SELECT max(pre) + 1 FROM _corpus) AS max_corpus_pre,

    -- max corpus post
    (SELECT max(post) + 1 FROM _corpus) AS max_corpus_post,

    -- max text id
    (SELECT max(id) + 1 FROM _text) AS max_text_id  
;
