SELECT 
solution.*,
NULL::varchar AS node_annotation_ns1,
NULL::varchar AS node_annotation_name1,
annotation_category2.namespace AS node_annotation_ns2,
annotation_category2.name AS node_annotation_name2,
NULL::varchar AS node_annotation_ns3,
NULL::varchar AS node_annotation_name3,
c.path_name AS path_name
FROM
(
SELECT 
    facts1.id AS id1,
    facts1.node_anno_category AS cat1,
    min(facts1.node_name) AS node_name1,
    facts2.id AS id2,
    facts2.node_anno_category AS cat2,
    min(facts2.node_name) AS node_name2,
    facts3.id AS id3,
    facts3.node_anno_category AS cat3,
    min(facts3.node_name) AS node_name3,
    min(facts1.toplevel_corpus) AS toplevel_corpus,
    min(facts1.corpus_ref) AS corpus_ref
FROM
  facts_2048 AS facts1,
  facts_2048 AS facts2,
  facts_2048 AS facts3
WHERE
  facts1.corpus_ref = facts2.corpus_ref AND
  facts1.corpus_ref = facts3.corpus_ref AND
  facts1.is_token IS TRUE AND
  facts1.n_sample IS TRUE AND
  facts1.right_token = facts2.left_token - 1 AND
  facts1.text_ref = facts2.text_ref AND
  facts1.toplevel_corpus IN (2048) AND
  facts2.corpus_ref = facts3.corpus_ref AND
  facts2.n_na_sample IS TRUE AND
  facts2.node_annotext LIKE 'pos:NN' AND
  facts2.right_token = facts3.left_token - 1 AND
  facts2.text_ref = facts3.text_ref AND
  facts2.toplevel_corpus IN (2048) AND
  facts3.is_token IS TRUE AND
  facts3.n_sample IS TRUE AND
  facts3.toplevel_corpus IN (2048)
GROUP BY id1, cat1, id2, cat2, id3, cat3
ORDER BY id1, cat1, id2, cat2, id3, cat3
LIMIT 10
OFFSET 0
) AS solution
LEFT JOIN annotation_category AS annotation_category1 ON (solution.toplevel_corpus =  annotation_category1.toplevel_corpus AND solution.cat1 = annotation_category1.id)
LEFT JOIN annotation_category AS annotation_category2 ON (solution.toplevel_corpus =  annotation_category2.toplevel_corpus AND solution.cat2 = annotation_category2.id)
LEFT JOIN annotation_category AS annotation_category3 ON (solution.toplevel_corpus =  annotation_category3.toplevel_corpus AND solution.cat3 = annotation_category3.id),
corpus AS c

WHERE
 c.id = solution.corpus_ref
