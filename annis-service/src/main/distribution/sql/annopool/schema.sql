-- (modified) source tables

DROP TABLE IF EXISTS repository_metadata CASCADE;
CREATE TABLE repository_metadata
(
  name varchar NOT NULL PRIMARY KEY,
  "value" varchar NOT NULL
);

DROP TABLE IF EXISTS corpus CASCADE;
CREATE TABLE corpus
(
  id         integer PRIMARY KEY,
  name       varchar NOT NULL, -- UNIQUE,
  type       varchar NOT NULL,
  version    varchar,
  pre        integer NOT NULL UNIQUE,
  post       integer NOT NULL UNIQUE,
  top_level  boolean NOT NULL,  -- true for roots of the corpus forest
  path_name  varchar[]
);
COMMENT ON COLUMN corpus.id IS 'primary key';
COMMENT ON COLUMN corpus.name IS 'name of the corpus';
COMMENT ON COLUMN corpus.pre IS 'pre-order value';
COMMENT ON COLUMN corpus.post IS 'post-order value';
COMMENT ON COLUMN corpus.path_name IS 'path of this corpus in the corpus tree (names)';

DROP TABLE IF EXISTS corpus_annotation CASCADE;
CREATE TABLE corpus_annotation
(
  corpus_ref  integer NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  namespace   varchar,
  name        varchar NOT NULL,
  value       varchar,
  UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';

DROP TABLE IF EXISTS text CASCADE;
CREATE TABLE text
(
  corpus_ref integer REFERENCES corpus(id), 
  id    integer,
  name  varchar,
  text  text,
  toplevel_corpus integer REFERENCES corpus(id),
  PRIMARY KEY(corpus_ref, id)
);
COMMENT ON COLUMN text.id IS 'primary key';
COMMENT ON COLUMN text.name IS 'informational name of the primary data text';
COMMENT ON COLUMN text.text IS 'raw text data';

DROP TYPE IF EXISTS annotype CASCADE;
CREATE TYPE annotype AS ENUM ('node', 'edge', 'segmentation');
-- collect all node annotations
DROP TABLE IF EXISTS annotation_pool CASCADE;
CREATE TABLE annotation_pool (
  id bigserial,
  toplevel_corpus integer REFERENCES corpus(id),
  namespace varchar,
  "name" varchar,
  val varchar,
  "type" annotype,
  occurences bigint,
  PRIMARY KEY(id),
  UNIQUE(namespace, "name", val, "type", toplevel_corpus)
);

DROP TABLE IF EXISTS facts CASCADE;
CREATE TABLE facts (
  fid bigserial,
  id bigint,
  text_ref integer,
  corpus_ref integer REFERENCES corpus(id),
  toplevel_corpus integer REFERENCES corpus(id),
  node_namespace varchar,
  node_name varchar,
  "left" integer,
  "right" integer,
  token_index integer,
  is_token boolean,
  continuous boolean,
  span varchar,
  left_token integer,
  right_token integer,
  seg_name varchar,
  seg_index integer,
  pre integer, -- pre-order value
  post integer, -- post-order value
  parent integer, -- foreign key to rank.pre of the parent node, or NULL for roots
  root boolean,
  "level" integer,
  component_id integer, -- component id
  edge_type character(1), -- edge type of this component
  edge_namespace varchar, -- optional namespace of the edges’ names
  edge_name varchar, -- name of the edges in this component
  node_anno_ref bigint REFERENCES annotation_pool(id),
  edge_anno_ref bigint REFERENCES annotation_pool(id),
  n_sample boolean,
  n_na_sample boolean,
  n_r_c_ea_sample boolean,
  n_r_c_sample boolean,
  n_r_c_na_sample boolean,
  PRIMARY KEY (fid)
);

COMMENT ON COLUMN facts.component_id IS 'component id';
COMMENT ON COLUMN facts.edge_type IS 'edge type of this component';
COMMENT ON COLUMN facts.edge_namespace IS 'optional namespace of the edges’ names';
COMMENT ON COLUMN facts.edge_name IS 'name of the edges in this component';
-- from rank
COMMENT ON COLUMN facts.pre IS 'pre-order value';
COMMENT ON COLUMN facts.post IS 'post-order value';
COMMENT ON COLUMN facts.parent IS 'foreign key to rank.pre of the parent node, or NULL for roots';

-- from component
COMMENT ON COLUMN facts.component_id IS 'component id';
COMMENT ON COLUMN facts.edge_type IS 'edge type of this component';
COMMENT ON COLUMN facts.edge_namespace IS 'optional namespace of the edges’ names';
COMMENT ON COLUMN facts.edge_name IS 'name of the edges in this component';

DROP TABLE IF EXISTS media_files CASCADE;
CREATE TABLE media_files
(
  file  bytea NOT NULL,
  corpus_ref  integer NOT NULL REFERENCES corpus(id) ON DELETE CASCADE,
  bytes bigint NOT NULL,
  mime_type varchar NOT NULL,
  title varchar NOT NULL,
  UNIQUE (corpus_ref, title)
);


-- stats
DROP TABLE IF EXISTS corpus_stats CASCADE;
CREATE TABLE corpus_stats
(
  name        varchar,
  id          integer NOT NULL REFERENCES corpus ON DELETE CASCADE,
  text        integer,
  tokens        bigint,
  max_corpus_id integer  NULL,
  max_corpus_pre integer NULL,
  max_corpus_post integer NULL,
  max_component_id integer NULL,
  max_node_id bigint NULL, 
  source_path varchar -- original path to the folder containing the relANNIS sources
);


DROP VIEW IF EXISTS corpus_info CASCADE;
CREATE VIEW corpus_info AS SELECT 
  name,
  id, 
  text,
  tokens,
  source_path
FROM 
  corpus_stats;
  
DROP TYPE IF EXISTS resolver_visibility CASCADE;
CREATE TYPE resolver_visibility AS ENUM (
  'permanent', 
  'visible',
  'hidden',
  'removed',
  'preloaded'
);

DROP TABLE IF EXISTS resolver_vis_map CASCADE;
CREATE TABLE resolver_vis_map
(
  "id"   serial PRIMARY KEY,
  "corpus"   varchar,
  "version"   varchar,
  "namespace"  varchar,
  "element"    varchar CHECK (element = 'node' OR element = 'edge'),
  "vis_type"   varchar NOT NULL,
  "display_name"   varchar NOT NULL,
  "visibility"     resolver_visibility NOT NULL DEFAULT 'hidden',
  "order" integer default '0',
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
COMMENT ON COLUMN resolver_vis_map.visibility IS 'defines the visibility state of a corpus: permanent: is always shown and can not be toggled, visible: is shown and can be toggled, hidden: is not shown can be toggled';
COMMENT ON COLUMN resolver_vis_map.order IS 'the order of the layers, in which they shall be shown';
COMMENT ON COLUMN resolver_vis_map.mappings IS 'which annotations in this corpus correspond to fields expected by the visualization, e.g. the tree visualizer expects a node label, which is called "cat" by default but may be changed using this field';

DROP TABLE IF EXISTS annotations CASCADE;
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
  toplevel_corpus integer NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_config CASCADE;
CREATE TABLE user_config
(
  id varchar NOT NULL,
  config json,
  PRIMARY KEY(id)
);
