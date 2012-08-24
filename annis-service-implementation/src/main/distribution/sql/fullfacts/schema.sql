-- (modified) source tables
CREATE TABLE repository_metadata
(
  name varchar NOT NULL PRIMARY KEY,
  "value" varchar NOT NULL
);


CREATE TABLE corpus
(
  id         bigint PRIMARY KEY,
  name       varchar NOT NULL, -- UNIQUE,
  type       varchar NOT NULL,
  version    varchar,
  pre        bigint NOT NULL UNIQUE,
  post       bigint NOT NULL UNIQUE,
  top_level  boolean NOT NULL,  -- true for roots of the corpus forest
  path_name  varchar[]
);
COMMENT ON COLUMN corpus.id IS 'primary key';
COMMENT ON COLUMN corpus.name IS 'name of the corpus';
COMMENT ON COLUMN corpus.pre IS 'pre-order value';
COMMENT ON COLUMN corpus.post IS 'post-order value';
COMMENT ON COLUMN corpus.path_name IS 'path of this corpus in the corpus tree (names)';

CREATE TABLE corpus_annotation
(
  corpus_ref  bigint NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  namespace   varchar,
  name        varchar NOT NULL,
  value       varchar,
  UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';

CREATE TABLE text
(
  id    bigint PRIMARY KEY,
  name  varchar,
  text  text,
  toplevel_corpus bigint REFERENCES corpus(id)
);
COMMENT ON COLUMN text.id IS 'primary key';
COMMENT ON COLUMN text.name IS 'informational name of the primary data text';
COMMENT ON COLUMN text.text IS 'raw text data';


CREATE TABLE facts
(
  fid BIGSERIAL PRIMARY KEY,
  id      bigint,
  text_ref  bigint,
  corpus_ref  bigint,
  toplevel_corpus bigint,
  node_namespace  varchar,
  node_name    varchar,
  "left"    integer,
  "right"    integer,
  token_index  integer,
  is_token boolean,
  continuous  boolean,
  span    varchar,
  left_token  integer,
  right_token  integer,
  seg_name varchar,
  seg_left integer,
  seg_right integer,

  pre        bigint,
  post      bigint,
  parent    bigint,
  root      boolean,
  level      bigint,
  component_id      bigint,
  edge_type    char(1),
  edge_namespace  varchar,
  edge_name    varchar,

  node_annotation_namespace  varchar,
  node_annotation_name    varchar,
  node_annotation_value    varchar,

  edge_annotation_namespace  varchar,
  edge_annotation_name    varchar,
  edge_annotation_value    varchar,

  n_sample boolean,
  n_na_sample boolean,
  n_r_c_ea_sample boolean,
  n_r_c_sample boolean,
  n_r_c_na_sample boolean
);

-- from component
COMMENT ON COLUMN facts.component_id IS 'component id';
COMMENT ON COLUMN facts.edge_type IS 'edge type of this component';
COMMENT ON COLUMN facts.edge_namespace IS 'optional namespace of the edges’ names';
COMMENT ON COLUMN facts.edge_name IS 'name of the edges in this component';
-- from rank
COMMENT ON COLUMN facts.pre IS 'pre-order value';
COMMENT ON COLUMN facts.post IS 'post-order value';
COMMENT ON COLUMN facts.parent IS 'foreign key to rank.pre of the parent node, or NULL for roots';
-- from edge_annotation
COMMENT ON COLUMN facts.edge_annotation_namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN facts.edge_annotation_name IS 'annotation key';
COMMENT ON COLUMN facts.edge_annotation_value IS 'annotation value';

CREATE TABLE media_files
(
  file  bytea NOT NULL,
  corpus_ref  bigint NOT NULL REFERENCES corpus(id) ON DELETE CASCADE,
  bytes bigint NOT NULL,
  mime_type varchar NOT NULL,
  title varchar NOT NULL,
  UNIQUE (corpus_ref, title)
);

-- stats
CREATE TABLE corpus_stats
(
  name        varchar,
  id          bigint NOT NULL REFERENCES corpus ON DELETE CASCADE,
  text        bigint,
  tokens        bigint,
  max_corpus_id bigint  NULL,
  max_corpus_pre bigint NULL,
  max_corpus_post bigint NULL,
  max_text_id bigint NULL,
  max_component_id bigint NULL,
  max_node_id bigint NULL, 
  source_path varchar -- original path to the folder containing the relANNIS sources
);


CREATE VIEW corpus_info AS SELECT 
  name,
  id, 
  text,
  tokens,
  source_path
FROM 
  corpus_stats;
  

CREATE TABLE resolver_vis_map
(
  "id"   serial PRIMARY KEY,
  "corpus"   varchar,
  "version"   varchar,
  "namespace"  varchar,
  "element"    varchar CHECK (element = 'node' OR element = 'edge'),
  "vis_type"   varchar NOT NULL,
  "display_name"   varchar NOT NULL,
  "visibility"    varchar NOT NULL CHECK (visibility = 'permanent' OR visibility = 'yes' OR visibility = 'no'),
  "order" bigint default '0',
  "mappings" varchar,
   UNIQUE (corpus,version,namespace,element,vis_type)              
);
COMMENT ON COLUMN resolver_vis_map.id IS 'primary key';
COMMENT ON COLUMN resolver_vis_map.corpus IS 'the name of the supercorpus, part of foreign key to corpus.name,corpus.version';
COMMENT ON COLUMN resolver_vis_map.version IS 'the version of the corpus, part of foreign key to corpus.name,corpus.version';
COMMENT ON COLUMN resolver_vis_map.namespace IS 'the several layers of the corpus';
COMMENT ON COLUMN resolver_vis_map.element IS 'the type of the entry: node | edge';
COMMENT ON COLUMN resolver_vis_map.vis_type IS 'the abstract type of visualization: tree, discourse, grid, ...';
COMMENT ON COLUMN resolver_vis_map.display_name IS 'the name of the layer which shall be shown for display';
COMMENT ON COLUMN resolver_vis_map.visibility IS 'defines the visibility state of a corpus: permanent: is always shown and can not be toggled, yes: is shown and can be toggled, no: is not shown can be toggled';
COMMENT ON COLUMN resolver_vis_map.order IS 'the order of the layers, in which they shall be shown';
COMMENT ON COLUMN resolver_vis_map.mappings IS 'which annotations in this corpus correspond to fields expected by the visualization, e.g. the tree visualizer expects a node label, which is called "cat" by default but may be changed using this field';

CREATE TABLE annotations
(
  id bigserial NOT NULL,
  namespace varchar,
  "name" varchar,
  "value" varchar,
  occurences bigint,
  "type" varchar,
  "subtype" char(1),
  edge_namespace varchar,
  edge_name varchar,
  toplevel_corpus bigint NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);
