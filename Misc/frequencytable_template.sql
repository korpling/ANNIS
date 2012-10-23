SELECT v1.span AS value1, a2.val AS value2, v3.span AS value3, count(*) AS "count"
FROM
(
    SELECT 
    facts1.id AS id1, facts1.node_name AS node_name1, corpus1.path_name AS path_name1, facts2.id AS id2, facts2.node_name AS node_name2, corpus2.path_name AS path_name2, facts3.id AS id3, facts3.node_name AS node_name3, corpus3.path_name AS path_name3, facts1.toplevel_corpus
  FROM
    facts AS facts1,
    facts AS facts2,
    facts AS facts3,
    corpus AS corpus1,     corpus AS corpus2,     corpus AS corpus3
  WHERE
    -- annotations can always only be inside a subcorpus/document AND
    -- artificial node subview AND
    -- artificial node-node_annotation subview AND
    facts1.corpus_ref = corpus1.id AND
    facts1.corpus_ref = facts3.corpus_ref AND
    facts1.n_sample IS TRUE AND
    facts1.right_token = facts2.left_token - 1 AND
    facts1.span = 'der' AND
    facts1.text_ref = facts2.text_ref AND
    facts1.toplevel_corpus IN (2) AND
    facts2.corpus_ref = corpus2.id AND
    facts2.n_na_sample IS TRUE AND
    facts2.node_anno_ref= ANY(getAnno(NULL, 'pos', 'NN', NULL, ARRAY[2], 'node')) AND
    facts2.right_token = facts3.left_token - 1 AND
    facts2.text_ref = facts3.text_ref AND
    facts2.toplevel_corpus IN (2) AND
    facts3.corpus_ref = corpus3.id AND
    facts3.is_token IS TRUE AND
    facts3.n_sample IS TRUE AND
    facts3.toplevel_corpus IN (2)
  
  ) AS solutions, 
  facts AS v1,
  facts AS v2,
  facts AS v3,
  annotation_pool AS a2

WHERE
  v1.toplevel_corpus IN (2) AND
  v2.toplevel_corpus IN (2) AND
  v3.toplevel_corpus IN (2) AND
  a2.toplevel_corpus IN (2) AND
  v1.id = solutions.id1 AND
  v2.id = solutions.id2 AND
  v3.id = solutions.id3 AND
  v2.node_anno_ref = a2.id AND
  a2."name" = 'lemma'
  
GROUP BY value1, value2, value3
ORDER BY "count" DESC

