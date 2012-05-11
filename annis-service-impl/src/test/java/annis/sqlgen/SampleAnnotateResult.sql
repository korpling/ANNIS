-- "das" & pos="VVFIN" & #1 . #2 
-- queried on pcc2 v2 (only one match)

drop table if exists mymatch;
create temporary table mymatch as (

SELECT DISTINCT
  ARRAY[solutions.id1, solutions.id2] AS key,
  0 AS "matchstart",
  solutions.n,
  facts.id AS "id",
  facts.text_ref AS "text_ref",
  facts.corpus_ref AS "corpus_ref",
  facts.toplevel_corpus AS "toplevel_corpus",
  facts.node_namespace AS "node_namespace",
  facts.node_name AS "node_name",
  facts.left AS "left",
  facts.right AS "right",
  facts.token_index AS "token_index",
  facts.is_token AS "is_token",
  facts.continuous AS "continuous",
  facts.span AS "span",
  facts.left_token AS "left_token",
  facts.right_token AS "right_token",
  facts.pre AS "pre",
  facts.post AS "post",
  facts.parent AS "parent",
  facts.root AS "root",
  facts.level AS "level",
  facts.component_id AS "component_id",
  facts.edge_type AS "edge_type",
  facts.edge_name AS "edge_name",
  facts.edge_namespace AS "edge_namespace",
  node_anno."namespace" AS node_annotation_namespace,
  node_anno."name" AS node_annotation_name,
  node_anno."val" AS node_annotation_value,
  edge_anno."namespace" AS edge_annotation_namespace,
  edge_anno."name" AS edge_annotation_name,
  edge_anno."val" AS edge_annotation_value,
  corpus.path_name AS path
FROM
  (
    SELECT row_number() OVER () as n, inn.*    FROM (
      SELECT DISTINCT
        facts1.id AS id1, facts1.text_ref AS text1, facts1.left_token - 5 AS min1, facts1.right_token + 5 AS max1, facts1.corpus_ref AS corpus1, facts1.node_name AS name1, 
        facts2.id AS id2, facts2.text_ref AS text2, facts2.left_token - 5 AS min2, facts2.right_token + 5 AS max2, facts2.corpus_ref AS corpus2, facts2.node_name AS name2
      FROM
        facts AS facts1,
        facts AS facts2
      WHERE
        -- annotations can always only be inside a subcorpus/document AND
        -- artificial node subview AND
        -- artificial node-node_annotation subview AND
        facts1.n_sample IS TRUE AND
        facts1.right_token = facts2.left_token - 1 AND
        facts1.span = 'das' AND
        facts1.text_ref = facts2.text_ref AND
        facts1.toplevel_corpus IN (2) AND
        facts2.n_na_sample IS TRUE AND
        facts2.node_anno_ref= ANY(getAnnoByNameVal('pos', 'VVFIN', ARRAY[2], 'node')) AND
        facts2.toplevel_corpus IN (2)
      ORDER BY id1, id2
      LIMIT 10 OFFSET 0

    ) AS inn
  ) AS solutions,
  facts AS facts
  LEFT OUTER JOIN annotation_pool AS node_anno ON  (facts.node_anno_ref = node_anno.id AND facts.toplevel_corpus = node_anno.toplevel_corpus AND node_anno.toplevel_corpus IN (2))
  LEFT OUTER JOIN annotation_pool AS edge_anno ON (facts.edge_anno_ref = edge_anno.id AND facts.toplevel_corpus = edge_anno.toplevel_corpus AND edge_anno.toplevel_corpus IN (2)),
  corpus
WHERE
  facts.toplevel_corpus IN (2) AND
facts.text_ref IN (solutions.text1, solutions.text2) AND
  (
    facts.left_token <= ANY(ARRAY[solutions.max1, solutions.max2]) AND facts.right_token >= ANY(ARRAY[solutions.min1, solutions.min2])
  ) AND
  corpus.id = facts.corpus_ref
ORDER BY solutions.n, facts.pre

);

select * from mymatch;