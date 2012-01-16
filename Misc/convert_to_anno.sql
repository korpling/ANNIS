-- http://www.pgsql.cz/index.php/PostgreSQL_SQL_Tricks#Using_IMMUTABLE_functions_as_hints_for_the_optimizer

DROP TABLE IF EXISTS s_facts;
DROP TABLE IF EXISTS s_node_anno;

-- collect all node annotations
CREATE TABLE s_node_anno (
  id bigserial PRIMARY KEY,
  namespace varchar(150),
  "name" varchar(150),
  val varchar(1500),
  UNIQUE(namespace, "name", val)
);

INSERT INTO s_node_anno(namespace, "name", val)
(
  SELECT DISTINCT node_annotation_namespace, node_annotation_name, node_annotation_value
  FROM facts_2014 
  WHERE (sample & B'01000') = B'01000'
);

-- indexes on node annotations
CREATE INDEX idx_s_node_anno_name ON s_node_anno (
"name" varchar_pattern_ops, val varchar_pattern_ops);
CREATE INDEX idx_s_node_anno_namespace ON s_node_anno (
namespace varchar_pattern_ops, "name" varchar_pattern_ops, val varchar_pattern_ops);

-- copy and adjust facts table
CREATE TABLE s_facts (
  fid bigserial PRIMARY KEY,
  id bigint,
  text_ref bigint,
  corpus_ref bigint,
  toplevel_corpus bigint,
  node_namespace character varying(100),
  node_name character varying(100),
  "left" integer,
  "right" integer,
  token_index integer,
  is_token boolean,
  continuous boolean,
  span character varying(2000),
  left_token integer,
  right_token integer,
  pre bigint, -- pre-order value
  post bigint, -- post-order value
  parent bigint, -- foreign key to rank.pre of the parent node, or NULL for roots
  root boolean,
  "level" bigint,
  component_id bigint, -- component id
  edge_type character(1), -- edge type of this component
  edge_namespace character varying(255), -- optional namespace of the edges’ names
  edge_name character varying(255), -- name of the edges in this component
  node_anno bigint REFERENCES s_node_anno(id),
  edge_annotation_namespace character varying(150), -- optional namespace of annotation key
  edge_annotation_name character varying(150), -- annotation key
  edge_annotation_value character varying(1500), -- annotation value
  sample bit(5) -- Bit mask if sample for join of original table [n, n_na, n_r_c, n_r_c_ea, n_r_c_na]
);
ALTER TABLE s_facts ADD CHECK(toplevel_corpus = 2015);

INSERT INTO s_facts (
  id,
  text_ref,
  corpus_ref,
  toplevel_corpus,
  node_namespace,
  node_name,
  "left",
  "right",
  token_index,
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
  edge_annotation_namespace,
  edge_annotation_name,
  edge_annotation_value,
  sample
)
  SELECT 
    id,
    text_ref,
    corpus_ref,
    2015,
    node_namespace,
    node_name,
    "left",
    "right",
    token_index,
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
    (SELECT id FROM s_node_anno AS na 
      WHERE na.namespace = node_annotation_namespace ů
        AND na.name = node_annotation_name
        AND na.val = node_annotation_value
    ),
    edge_annotation_namespace,
    edge_annotation_name,
    edge_annotation_value,
    sample
  FROM facts_2014
;

-- indices
SELECT 1 FROM simplefactsindex('s_facts');
CREATE INDEX idx__sample_n__2015
  ON s_facts
  USING btree
  ((sample & B'10000'::"bit"));
CREATE INDEX idx__sample_n_na__2015
  ON s_facts
  USING btree
  ((sample & B'01000'::"bit"));
CREATE INDEX idx__sample_n_r_c__2015
  ON s_facts
  USING btree
  ((sample & B'00100'::"bit"));
CREATE INDEX idx__sample_n_r_c_ea__2015
  ON s_facts
  USING btree
  ((sample & B'00010'::"bit"));
CREATE INDEX idx__sample_n_r_c_na__2015
  ON s_facts
  USING btree
  ((sample & B'00001'::"bit"));

VACUUM ANALYZE s_facts;
VACUUM ANALYZE s_node_anno;