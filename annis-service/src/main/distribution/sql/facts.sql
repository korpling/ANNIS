--- :id is replaced by code
DROP TABLE IF EXISTS facts_:id;

------------
-- FACTS --
-----------

CREATE TABLE facts_:id
(
  -- temporary columns for calculating the sample_*
  PRIMARY KEY(fid),
  -- check constraints
  CHECK(toplevel_corpus = :id)
)
INHERITS (facts);

ALTER TABLE facts_:id ALTER COLUMN span SET STATISTICS :stat_target;
ALTER TABLE facts_:id ALTER COLUMN node_annotext SET STATISTICS :stat_target;
ALTER TABLE facts_:id ALTER COLUMN node_qannotext SET STATISTICS :stat_target;

INSERT INTO facts_:id
(
  id,
  text_ref,
  corpus_ref,
  toplevel_corpus,
  node_namespace,
  node_name,
  salt_id,
  "left",
  "right",
  token_index,
  is_token,
  span,
  left_token,
  right_token,
  seg_name,
  seg_index,
  rank_id,
  pre,
  post,
  parent,
  root,
  "level",
  component_id,
  edge_type,
  edge_namespace,
  edge_name,
  node_anno_category,
  node_annotext,
  node_qannotext,
  edge_annotext,
  edge_qannotext,
  n_sample,
  n_na_sample
)

SELECT
  *,
  (row_number() OVER (PARTITION BY id) = 1) AS n_sample,
  (row_number() OVER (PARTITION BY id, node_qannotext) = 1) AS n_na_sample
FROM
(
  SELECT
    ((SELECT "new" FROM _nodeidmapping WHERE "old" = _node.id LIMIT 1) + :offset_node_id)AS id,
    _node.text_ref AS text_ref,
    (_node.corpus_ref + :offset_corpus_id) AS corpus_ref,
    :id AS toplevel_corpus,
    _node.layer AS node_namespace,
    _node.name AS node_name,
    _node.name AS salt_id,
    _node."left" AS "left",
    _node."right" AS "right",
    _node.token_index AS token_index,
    (_node.token_index IS NOT NULL AND _node.seg_name IS NULL) AS is_token,
    _node.span AS span,
    _node.left_token AS left_token,
    _node.right_token AS right_token,
    _node.seg_name AS seg_name,
    _node.seg_index AS seg_index,

    _rank.id AS rank_id,
    _rank.pre AS pre,
    _rank.post AS post,
    _rank.parent AS parent,
    _node.root AS root,
    _rank.level AS level,

    _component.id  AS component_id,
    _component.type AS edge_type,
    _component.layer AS edge_namespace,
    _component.name AS edge_name,
    annotation_category.id AS node_anno_category,
    (
      CASE WHEN _node_annotation.name IS NULL THEN NULL
      ELSE concat(_node_annotation.name, ':', _node_annotation.value)
      END
    ) AS node_annotext,
    (
      CASE WHEN _node_annotation.name IS NULL THEN NULL
      ELSE concat(_node_annotation.namespace,':', _node_annotation.name, ':', _node_annotation.value)
      END
    ) AS node_qannotext,

    (
      CASE WHEN _edge_annotation.name IS NULL THEN NULL
      ELSE concat(_edge_annotation.name, ':', _edge_annotation.value)
      END
    ) AS edge_annotext,
    (
      CASE WHEN _edge_annotation.name IS NULL THEN NULL
      ELSE concat(_edge_annotation.namespace,':', _edge_annotation.name, ':', _edge_annotation.value)
      END
    ) AS edge_qannotext
  FROM
    _node
    LEFT JOIN _node_annotation ON (_node_annotation.node_ref = _node.id)
    LEFT JOIN _rank ON (_rank.node_ref = _node.id)
    LEFT JOIN _component ON (_rank.component_ref = _component.id)
    LEFT JOIN _edge_annotation ON (_edge_annotation.rank_ref = _rank.id)
    LEFT JOIN annotation_category ON (
      annotation_category."name" = _node_annotation."name" 
      AND annotation_category.namespace IS NOT DISTINCT FROM _node_annotation.namespace
      AND annotation_category.toplevel_corpus = :id
    )
) as tmp
ORDER BY corpus_ref, n_sample, is_token
;
