-- "das" & pos="VVFIN" & #1 . #2 
-- queried on pcc2 v2 (only one match)

drop table if exists mymatch;
create temporary table mymatch as (

  WITH
    matchesRaw AS
    (
        SELECT row_number() OVER () as n, inn.*
      FROM (
        SELECT DISTINCT
          facts1.id AS id1, facts1.text_ref AS text1, facts1.left_token - 5 AS min1, facts1.right_token + 5 AS max1, facts1.corpus_ref AS corpus1, facts1.node_name AS name1, 
          facts2.id AS id2, facts2.text_ref AS text2, facts2.left_token - 5 AS min2, facts2.right_token + 5 AS max2, facts2.corpus_ref AS corpus2, facts2.node_name AS name2
        FROM
          facts_2 AS facts1,
          facts_2 AS facts2
        WHERE
          facts1.corpus_ref = facts2.corpus_ref AND
          facts1.n_sample IS TRUE AND
          facts1.right_token = facts2.left_token - 1 AND
          facts1.span = 'das' AND
          facts1.text_ref = facts2.text_ref AND
          facts1.toplevel_corpus IN (2) AND
          facts2.n_na_sample IS TRUE AND
          facts2.node_anno_ref= ANY(getAnno(NULL, 'pos', 'VVFIN', NULL, ARRAY[2], 'node')) AND
          facts2.toplevel_corpus IN (2)
        ORDER BY id1, id2
        LIMIT 10
  OFFSET 0

      ) AS inn

    ),
    matches AS
    (
      SELECT
        n AS n,
        1 AS nodeNr,
        id1 AS id,
        text1 AS "text",
        min1 AS min,
        max1 AS max,
        corpus1 AS corpus
      FROM matchesRaw

      UNION ALL

      SELECT
        n AS n,
        2 AS nodeNr,
        id2 AS id,
        text2 AS "text",
        min2 AS min,
        max2 AS max,
        corpus2 AS corpus
      FROM matchesRaw

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
    facts.left AS "left",
    facts.right AS "right",
    facts.token_index AS "token_index",
    facts.is_token AS "is_token",
    facts.continuous AS "continuous",
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
    node_anno."namespace" AS node_annotation_namespace,
    node_anno."name" AS node_annotation_name,
    node_anno."val" AS node_annotation_value,
    edge_anno."namespace" AS edge_annotation_namespace,
    edge_anno."name" AS edge_annotation_name,
    edge_anno."val" AS edge_annotation_value  ,
    corpus.path_name AS path
  FROM
    solutions,
    facts_2 AS facts
    LEFT OUTER JOIN annotation_pool AS node_anno ON  (facts.node_anno_ref = node_anno.id AND facts.toplevel_corpus = node_anno.toplevel_corpus AND node_anno.toplevel_corpus IN (2))
    LEFT OUTER JOIN annotation_pool AS edge_anno ON (facts.edge_anno_ref = edge_anno.id AND facts.toplevel_corpus = edge_anno.toplevel_corpus AND edge_anno.toplevel_corpus IN (2)),
    corpus
  WHERE
    facts.toplevel_corpus IN (2) AND
    (facts.left_token <= solutions."max" AND facts.right_token >= solutions."min" AND facts.text_ref = solutions.text AND facts.corpus_ref = solutions.corpus)
   AND
    corpus.id = facts.corpus_ref
  ORDER BY solutions.n, facts.component_id, facts.pre
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
  continuous boolean,
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