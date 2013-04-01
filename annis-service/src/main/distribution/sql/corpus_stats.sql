-- statistics
CREATE TABLE _corpus_stats AS SELECT
	(select name from _corpus where top_level = 't') as name,
	(select id from _corpus where top_level = 't') as id,

   (select count(*) from _text) as text,
 
    -- # tokens
    (SELECT count(*) FROM _node WHERE token_index IS NOT NULL) as tokens,

    -- max corpus id
    (SELECT max(id) + 1 FROM _corpus) AS max_corpus_id,

    -- max corpus pre
    (SELECT max(pre) + 1 FROM _corpus) AS max_corpus_pre,

    -- max corpus post
    (SELECT max(post) + 1 FROM _corpus) AS max_corpus_post,

    NULL::integer AS max_component_id,
    NULL::bigint AS max_node_id,

    ':path' AS source_path
;
