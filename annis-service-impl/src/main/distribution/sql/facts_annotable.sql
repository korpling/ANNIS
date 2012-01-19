--- :id is replaced by code
DROP TABLE IF EXISTS facts_:id;
DROP TABLE IF EXISTS node_anno_:id;
DROP TABLE IF EXISTS edge_anno_:id;

---------------
-- NODE_ANNO --
---------------

CREATE TABLE node_anno_:id
(
  CHECK(toplevel_corpus = :id)
)
INHERITS(node_anno);

INSERT INTO node_anno(toplevel_corpus, namespace, "name", val, occurences)
(
  SELECT :id, namespace, "name", "value", count(*) as occurences
  FROM  _node_annotation
  GROUP BY namespace, "name", "value"
);

-- indexes on node annotations
CREATE INDEX idx__node_anno_name__:id ON node_anno_:id (
  "name" varchar_pattern_ops, val varchar_pattern_ops
);
CREATE INDEX idx__node_anno_namespace__:id ON node_anno_:id (
  namespace varchar_pattern_ops, "name" varchar_pattern_ops, val varchar_pattern_ops
);
CREATE INDEX idx__node_anno_occurences__:id ON node_anno (occurences);

---------------
-- EDGE_ANNO --
---------------


CREATE TABLE edge_anno_:id
(
  CHECK(toplevel_corpus = :id)
)
INHERITS(edge_anno);

INSERT INTO edge_anno(toplevel_corpus, namespace, "name", val, occurences)
(
  SELECT :id, namespace, "name", "value", count(*) as occurences
  FROM  _edge_annotation
  GROUP BY namespace, "name", "value"
);

-- indexes on edge annotations
CREATE INDEX idx__edge_anno_name__:id ON edge_anno_:id (
  "name" varchar_pattern_ops, val varchar_pattern_ops
);
CREATE INDEX idx__edge_anno_namespace__:id ON edge_anno_:id (
  namespace varchar_pattern_ops, "name" varchar_pattern_ops, val varchar_pattern_ops
);
CREATE INDEX idx__edge_anno_occurences__:id ON edge_anno (occurences);

------------
-- FACTS --
-----------

CREATE TABLE facts_:id
(
  -- temporary columns for calculating the sample_*
  n_rownum INTEGER,
  n_na_rownum INTEGER,
  n_r_c_ea_rownum INTEGER,
  n_r_c_rownum INTEGER,
  n_r_c_na_rownum INTEGER
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
  pre,
  post,
  parent,
  root,
  "level",
  component_id,
  edge_type,
  edge_namespace,
  edge_name,
  node_anno,
  edge_anno,
  sample,
  n_rownum,
  n_na_rownum,
  n_r_c_ea_rownum,
  n_r_c_rownum,
  n_r_c_na_rownum
)

SELECT
  *,
  row_number() OVER (PARTITION BY id) AS n_rownum,
  row_number() OVER (PARTITION BY id, node_anno) AS n_na_rownum,
  row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  edge_anno) AS n_r_c_ea_rownum,
  row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id) AS n_r_c_rownum,
  row_number() OVER (PARTITION BY id,
                                  parent,
                                  component_id,
                                  node_anno) AS n_r_c_na_rownum
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
    FALSE AS is_token,
    _node.continuous AS continuous,
    _node.span AS span,
    _node.left_token AS left_token,
    _node.right_token AS right_token,

    _rank.pre AS pre,
    _rank.post AS post,
    _rank.parent AS parent,
    _rank.root AS root,
    _rank.level AS level,

    _component.id AS component_id,
    _component.type AS edge_type,
    _component.namespace AS edge_namespace,
    _component.name AS edge_name,

    (SELECT id FROM node_anno_:id AS na 
      WHERE na.namespace = _node_annotation.namespace
        AND na.name = _node_annotation."name"
        AND na.val = _node_annotation."value"
    ) AS node_anno,
    (SELECT id FROM edge_anno_:id AS ea 
      WHERE ea.namespace = _edge_annotation.namespace
        AND ea.name = _edge_annotation."name"
        AND ea.val = _edge_annotation."value"
    ) AS edge_anno,

    B'00000' AS sample
  FROM
    _node
    JOIN _rank ON (_rank.node_ref = _node.id)
    JOIN _component ON (_rank.component_ref = _component.id)
    LEFT JOIN _node_annotation ON (_node_annotation.node_ref = _node.id)
    LEFT JOIN _edge_annotation ON (_edge_annotation.rank_ref = _rank.pre)
  WHERE
    _node.toplevel_corpus = :id
) as tmp
;
