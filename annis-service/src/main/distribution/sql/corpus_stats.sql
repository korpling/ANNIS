-- statistics
CREATE TABLE _corpus_stats AS SELECT
	(select name from _corpus where top_level = 't') as name,
	(select id from _corpus where top_level = 't') as id,

   (select count(*) from _text) as text,
 
    -- # tokens
    (SELECT count(*) FROM _node WHERE token_index IS NOT NULL) as tokens,

    -- max corpus id
    (SELECT max(id) FROM _corpus) AS max_corpus_id,

    -- max corpus pre
    (SELECT max(pre) FROM _corpus) AS max_corpus_pre,

    -- max corpus post
    (SELECT max(post) FROM _corpus) AS max_corpus_post,

    (SELECT max(id) FROM _node) AS max_node_id,

    ':path' AS source_path
;
