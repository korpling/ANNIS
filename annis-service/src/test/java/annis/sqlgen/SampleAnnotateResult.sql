-- "das" & pos="VVFIN" & #1 . #2 
-- queried on pcc2 v6 (only one match)

drop table if exists mymatch;
create temporary table mymatch as (

WITH
  matches AS
  (
    (
    SELECT 1 AS n, 2 AS nodeNr,
      facts2.id AS id, facts2.text_ref AS text, facts2.left_token - 5 AS min, facts2.right_token + 5 AS max, facts2.corpus_ref AS corpus
    FROM
      facts_2 AS facts2, corpus AS corpus2
    WHERE
      corpus2.path_name = '{4282, pcc2}' AND
      facts2.corpus_ref = corpus2.id AND
      facts2.salt_id = 'tok_156' AND
      facts2.toplevel_corpus IN ( 2) 
    LIMIT 1
    )
    UNION ALL
    (
    SELECT 1 AS n, 1 AS nodeNr,
      facts1.id AS id, facts1.text_ref AS text, facts1.left_token - 5 AS min, facts1.right_token + 5 AS max, facts1.corpus_ref AS corpus
    FROM
      facts_2 AS facts1, corpus AS corpus1
    WHERE
      corpus1.path_name = '{4282, pcc2}' AND
      facts1.corpus_ref = corpus1.id AND
      facts1.salt_id = 'tok_155' AND
      facts1.toplevel_corpus IN ( 2) 
    LIMIT 1
    )
  ),
  keys AS (
    SELECT n, array_agg(id ORDER BY nodenr ASC) AS "key" FROM matches
    GROUP BY n
  ),
  solutions AS
  (
    SELECT keys.key AS key, matches.n AS n, matches.text, matches.corpus, matches.min, matches.max
    FROM matches, keys
    WHERE keys.n = matches.n
  )
SELECT DISTINCT
  solutions."key",
  0 AS "matchstart",
  solutions.n,
  facts.id AS "id",
  facts.text_ref AS "text_ref",
  facts.corpus_ref AS "corpus_ref",
  facts.toplevel_corpus AS "toplevel_corpus",
  facts.node_namespace AS "node_namespace",
  facts.node_name AS "node_name",
  facts.salt_id AS "salt_id",
  facts.left AS "left",
  facts.right AS "right",
  facts.token_index AS "token_index",
  facts.is_token AS "is_token",
  facts.span AS "span",
  facts.left_token AS "left_token",
  facts.right_token AS "right_token",
  facts.seg_name AS "seg_name",
  facts.seg_index AS "seg_index",
  facts.rank_id AS "rank_id",
  facts.pre AS "pre",
  facts.post AS "post",
  facts.parent AS "parent",
  facts.root AS "root",
  facts.level AS "level",
  facts.component_id AS "component_id",
  facts.edge_type AS "edge_type",
  facts.edge_name AS "edge_name",
  facts.edge_namespace AS "edge_namespace",
  (splitanno(node_qannotext))[1] as node_annotation_namespace,
  (splitanno(node_qannotext))[2] as node_annotation_name,
  (splitanno(node_qannotext))[3] as node_annotation_value,
  (splitanno(edge_qannotext))[1] as edge_annotation_namespace,
  (splitanno(edge_qannotext))[2] as edge_annotation_name,
  (splitanno(edge_qannotext))[3] as edge_annotation_value  ,
  corpus.path_name AS path
FROM
  solutions,
  facts_2 AS facts,
  corpus
WHERE
  facts.toplevel_corpus IN (2) AND
  (facts.left_token <= solutions."max" AND facts.right_token >= solutions."min" AND facts.text_ref = solutions.text AND facts.corpus_ref = solutions.corpus)
 AND
  corpus.id = facts.corpus_ref
ORDER BY solutions.n, facts.edge_name, facts.component_id, facts.pre


);

select * from mymatch;

-------------------------------------
-- alternative: load from CSV file --
-------------------------------------

DROP TABLE IF EXISTS mymatch;
CREATE TEMPORARY TABLE mymatch
(
  key bigint[],
  matchstart integer,
  n bigint,
  id bigint,
  text_ref bigint,
  corpus_ref bigint,
  toplevel_corpus bigint,
  node_namespace character varying,
  node_name character varying,
  "left" integer,
  "right" integer,
  token_index integer,
  is_token boolean,
  span character varying,
  left_token integer,
  right_token integer,
  pre bigint,
  post bigint,
  parent bigint,
  root boolean,
  level bigint,
  component_id bigint,
  edge_type character(1),
  edge_name character varying,
  edge_namespace character varying,
  node_annotation_namespace character varying,
  node_annotation_name character varying,
  node_annotation_value character varying,
  edge_annotation_namespace character varying,
  edge_annotation_name character varying,
  edge_annotation_value character varying,
  path character varying[]
);

COPY mymatch FROM '/home/thomas/annis/annis-service/src/test/java/annis/sqlgen/SampleAnnotateResult.csv' (FORMAT 'csv', HEADER, DELIMITER ';')
