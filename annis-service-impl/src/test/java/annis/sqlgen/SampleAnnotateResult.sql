-- "das" & pos="VVFIN" & #1 . #2 
-- queried on pcc2_plus (only one match)

drop table if exists mymatch;
create temporary table mymatch as (

SELECT DISTINCT
  ARRAY[solutions.id1, solutions.id2] AS key,
  ARRAY[solutions.name1,solutions.name2] AS key_names,
0::integer AS matchstart,
  facts.id AS id,
  facts.text_ref AS text_ref,
  facts.corpus_ref AS corpus_ref,
  facts.toplevel_corpus AS toplevel_corpus,
  facts.node_namespace AS node_namespace,
  facts.node_name AS node_name,
  facts.left AS left,
  facts.right AS right,
  facts.token_index AS token_index,
  facts.is_token AS is_token,
  facts.continuous AS continuous,
  facts.span AS span,
  facts.left_token AS left_token,
  facts.right_token AS right_token,
  facts.pre AS pre,
  facts.post AS post,
  facts.parent AS parent,
  facts.root AS root,
  facts.level AS level,
  facts.id AS component_id,
  facts.edge_type AS edge_type,
  facts.edge_name AS edge_name,
  facts.edge_namespace AS edge_namespace,
  facts.node_annotation_namespace AS node_annotation_namespace,
  facts.node_annotation_name AS node_annotation_name,
  facts.node_annotation_value AS node_annotation_value,
  facts.edge_annotation_namespace AS edge_annotation_namespace,
  facts.edge_annotation_name AS edge_annotation_name,
  facts.edge_annotation_value AS edge_annotation_value,
  corpus.path_name AS path,
  corpus.path_name[1] AS document_name
FROM
  (
    SELECT DISTINCT
      facts1.id AS id1, facts1.node_name AS name1, facts1.text_ref AS text1, facts1.left_token - 5 AS min1, facts1.right_token + 5 AS max1, 
      facts2.id AS id2, facts2.node_name AS name2, facts2.text_ref AS text2, facts2.left_token - 5 AS min2, facts2.right_token + 5 AS max2
    FROM
      facts AS facts1,
      facts AS facts2
    WHERE
      (facts1.sample & B'10000') = B'10000' AND
      (facts2.sample & B'01000') = B'01000' AND
      -- annotations can always only be inside a subcorpus/document AND
      -- artificial node subview AND
      -- artificial node-node_annotation subview AND
      facts1.right_token = facts2.left_token - 1 AND
      facts1.span = 'das' AND
      facts1.text_ref = facts2.text_ref AND
      facts1.toplevel_corpus IN (0) AND
      facts2.node_annotation_name = 'pos' AND
      facts2.node_annotation_value = 'VVFIN' AND
      facts2.toplevel_corpus IN (0)
    
  ) AS solutions,
  facts AS facts,
  corpus
WHERE
  facts.toplevel_corpus IN (0) AND
(
    (
      facts.text_ref = solutions.text1 AND
      (
        facts.left_token <= solutions.max1 AND facts.right_token >= solutions.min1
      )
    ) OR (
      facts.text_ref = solutions.text2 AND
      (
        facts.left_token <= solutions.max2 AND facts.right_token >= solutions.min2
      )
    )
  ) AND
  corpus.id = facts.corpus_ref
ORDER BY key, facts.pre
);

select * from mymatch;