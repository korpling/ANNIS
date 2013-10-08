SELECT v1.span AS value1, a2.val AS value2, v3.span AS value3, count(*) AS "count"
FROM
(
    SELECT 
    facts1.id AS id1, facts2.id AS id2, facts3.id AS id3, facts1.toplevel_corpus
  FROM
    facts_4856 AS facts1,
    facts_4856 AS facts2,
    facts_4856 AS facts3
  WHERE
    -- annotations can always only be inside a subcorpus/document AND
    -- artificial node subview AND
    -- artificial node-node_annotation subview AND
    facts1.corpus_ref = facts3.corpus_ref AND
    facts1.n_sample IS TRUE AND
    facts1.right_token = facts2.left_token - 1 AND
    facts1.span = 'der' AND
    facts1.text_ref = facts2.text_ref AND
    --facts1.toplevel_corpus IN (4856) AND
    facts2.n_na_sample IS TRUE AND
    facts2.node_anno_ref= ANY(getAnno(NULL, 'pos', 'NN', NULL, ARRAY[4856], 'node')) AND
    facts2.right_token = facts3.left_token - 1 AND
    facts2.text_ref = facts3.text_ref AND
    --facts2.toplevel_corpus IN (4856) AND
    facts3.is_token IS TRUE AND
    facts3.n_sample IS TRUE 
    --facts3.toplevel_corpus IN (4856)
  
  ) AS solutions, 
  facts_4856 AS v1,
  facts_4856 AS v2,
  facts_4856 AS v3,
  annotation_pool_4856 AS a2

WHERE
--  v1.toplevel_corpus IN (4856) AND
--  v2.toplevel_corpus IN (4856) AND
--  v3.toplevel_corpus IN (4856) AND
--  a2.toplevel_corpus IN (4856) AND
  v1.toplevel_corpus = solutions.toplevel_corpus AND
  v2.toplevel_corpus = solutions.toplevel_corpus AND
  v3.toplevel_corpus = solutions.toplevel_corpus AND
  v1.id = solutions.id1 AND
  v2.id = solutions.id2 AND
  v3.id = solutions.id3 AND
  v2.node_anno_ref = a2.id AND
  v1.n_sample is true AND
  v2.n_na_sample is true AND
  v3.n_sample is true AND
  a2."name" = 'lemma'  
GROUP BY value1, value2, value3
ORDER BY "count" DESC

--CREATE INDEX idx_ap_valbyname_4856 ON annotation_pool_4856 (id, val, "name");
--DROP index idx_spansample_4856;
--CREATE INDEX idx_spansample_4856 ON facts_4856 (span, text_ref) WHERE n_sample IS TRUE;

--analyze facts_4856;
