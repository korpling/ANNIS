--- :id is replaced by code
DROP TABLE IF EXISTS facts_:id;
DROP TABLE IF EXISTS node_anno_:id;
DROP TABLE IF EXISTS edge_anno_:id;

---------------
-- NODE_ANNO --
---------------

CREATE TABLE annotation_pool_:id
(
  PRIMARY KEY(id),
  CHECK(toplevel_corpus = :id)
)
INHERITS(annotation_pool);

INSERT INTO annotation_pool_:id(toplevel_corpus, namespace, "name", val, "type", occurences)
(
  SELECT :id, namespace, "name", "value", 'node', count(*) as occurences
  FROM  _node_annotation
  GROUP BY namespace, "name", "value"
);

---------------
-- EDGE_ANNO --
---------------


INSERT INTO annotation_pool_:id(toplevel_corpus, namespace, "name", val, "type", occurences)
(
  SELECT :id, namespace, "name", "value", 'edge', count(*) as occurences
  FROM  _edge_annotation
  GROUP BY namespace, "name", "value"
);

------------------
-- ANNO INDEXES --
------------------


-- indexes on node annotations
CREATE INDEX idx__node_annotation_pool_name__:id ON annotation_pool_:id (
  "name" varchar_pattern_ops, val varchar_pattern_ops
) WHERE "type" = 'node';
CREATE INDEX idx__node_annotation_pool_namespace__:id ON annotation_pool_:id (
  namespace varchar_pattern_ops, "name" varchar_pattern_ops, val varchar_pattern_ops
) WHERE "type" = 'node';
CREATE INDEX idx__node_annotation_pool_occurences__:id ON annotation_pool_:id (occurences) 
  WHERE "type" = 'node';


-- indexes on edge annotations
CREATE INDEX idx__edge_annotation_pool_name__:id ON annotation_pool_:id (
  "name" varchar_pattern_ops, val varchar_pattern_ops
) WHERE "type" = 'edge';
CREATE INDEX idx__edge_annotation_pool_namespace__:id ON annotation_pool_:id (
  namespace varchar_pattern_ops, "name" varchar_pattern_ops, val varchar_pattern_ops
) WHERE "type" = 'edge';
CREATE INDEX idx__edge_annotation_pool_occurences__:id ON annotation_pool_:id (occurences)
  WHERE "type" = 'edge';

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
  seg_left,
  seg_right,
  pre,
  post,
  parent,
  root,
  "level",
  component_id,
  edge_type,
  edge_namespace,
  edge_name,
  node_anno_ref,
  edge_anno_ref,
  n_sample,
  n_na_sample,
  n_r_c_ea_sample,
  n_r_c_sample,
  n_r_c_na_sample
)

SELECT
  *,
  (row_number() OVER (PARTITION BY id) = 1) AS n_sample,
  (row_number() OVER (PARTITION BY id, node_anno_ref) = 1) AS n_na_sample,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  edge_anno_ref) = 1) AS n_r_c_ea_rownum,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id) = 1) AS n_r_c_rownum,
  (row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  node_anno_ref) = 1) AS n_r_c_na_rownum
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
    (_node.token_index IS NOT NULL) AS is_token,
    _node.continuous AS continuous,
    _node.span AS span,
    _node.left_token AS left_token,
    _node.right_token AS right_token,
    _node.seg_name AS seg_name,
    _node.seg_left AS seg_left,
    _node.seg_right AS seg_right,

    _rank.pre AS pre,
    _rank.post AS post,
    _rank.parent AS parent,
    _rank.root AS root,
    _rank.level AS level,

    _component.id AS component_id,
    _component.type AS edge_type,
    _component.namespace AS edge_namespace,
    _component.name AS edge_name,

    (SELECT id FROM annotation_pool_:id AS na 
      WHERE na.namespace = _node_annotation.namespace
        AND na."name" = _node_annotation."name"
        AND na.val = _node_annotation."value"
        AND na."type" = 'node'
    ) AS node_anno_ref,
    (SELECT id FROM annotation_pool_:id AS ea 
      WHERE ea.namespace = _edge_annotation.namespace
        AND ea."name" = _edge_annotation."name"
        AND ea.val = _edge_annotation."value"
        AND ea."type" = 'edge'
    ) AS edge_anno_ref
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
