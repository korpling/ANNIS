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



INSERT INTO facts_:id
(
  id,
  text_ref,
  corpus_ref,
  toplevel_corpus,
  node_namespace,
  node_name,
  "left",
  "right",
  token_index,
  is_token,
  continuous,
  span,
  left_token,
  right_token,
  seg_name,
  seg_index,
  pre,
  post,
  parent,
  root,
  "level",
  component_id,
  edge_type,
  edge_namespace,
  edge_name,
  node_annotext,
  node_qannotext,
  edge_annotext,
  edge_qannotext,
  n_sample,
  n_na_sample,
  n_r_c_ea_sample,
  n_r_c_sample,
  n_r_c_na_sample
)

SELECT
  *,
  (row_number() OVER (PARTITION BY id) = 1) AS n_sample,
  (row_number() OVER (PARTITION BY id, node_qannotext) = 1) AS n_na_sample,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  edge_qannotext) = 1) AS n_r_c_ea_rownum,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id) = 1) AS n_r_c_rownum,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  node_qannotext) = 1) AS n_r_c_na_rownum
FROM
(
  SELECT
    _node.id AS id,
    _node.text_ref AS text_ref,
    _node.corpus_ref AS corpus_ref,
    _node.toplevel_corpus AS toplevel_corpus,
    _node.namespace AS node_namespace,
    _node.name AS node_name,
    _node."left" AS "left",
    _node."right" AS "right",
    _node.token_index AS token_index,
    (_node.token_index IS NOT NULL AND _node.seg_name IS NULL) AS is_token,
    _node.continuous AS continuous,
    _node.span AS span,
    _node.left_token AS left_token,
    _node.right_token AS right_token,
    _node.seg_name AS seg_name,
    _node.seg_left AS seg_index,

    _rank.pre AS pre,
    _rank.post AS post,
    _rank.parent AS parent,
    _rank.root AS root,
    _rank.level AS level,

    _component.id AS component_id,
    _component.type AS edge_type,
    _component.namespace AS edge_namespace,
    _component.name AS edge_name,

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
    JOIN _rank ON (_rank.node_ref = _node.id)
    JOIN _component ON (_rank.component_ref = _component.id)
    LEFT JOIN _node_annotation ON (_node_annotation.node_ref = _node.id)
    LEFT JOIN _edge_annotation ON (_edge_annotation.rank_ref = _rank.id)
  WHERE
    _node.toplevel_corpus = :id
) as tmp
;
