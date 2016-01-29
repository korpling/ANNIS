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
  version    varchar COLLATE "C",
  pre        integer NOT NULL UNIQUE,
  post       integer NOT NULL UNIQUE,
  top_level  boolean NOT NULL,  -- true for roots of the corpus forest
  path_name  varchar[] COLLATE "C"
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
  namespace   varchar COLLATE "C",
  name        varchar COLLATE "C" NOT NULL,
  value       varchar COLLATE "C",
  UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';

DROP TABLE IF EXISTS text CASCADE;
CREATE TABLE text
(
  corpus_ref integer REFERENCES corpus(id) ON DELETE CASCADE,
  id    integer,
  name  varchar COLLATE "C",
  text  text COLLATE "C",
  toplevel_corpus integer REFERENCES corpus(id) ON DELETE CASCADE,
  PRIMARY KEY(corpus_ref, id)
);
COMMENT ON COLUMN text.id IS 'primary key';
COMMENT ON COLUMN text.name IS 'informational name of the primary data text';
COMMENT ON COLUMN text.text IS 'raw text data';

DROP TABLE IF EXISTS annotation_category CASCADE;
CREATE TABLE annotation_category
(
  id SERIAL,
  namespace character varying COLLATE "C",
  name character varying COLLATE "C" NOT NULL,
  toplevel_corpus integer NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (toplevel_corpus) REFERENCES corpus (id) ON DELETE CASCADE,
  UNIQUE (namespace, name, toplevel_corpus)
);

-- Create the parent facts table definition. Note that the order of the columns can be important.
-- When a plan uses a "Materialize" node in a MergeJoin the complete tuple (containing all columns/attributes) will be used
-- and any join needs to go through each attribute before the attribute it is interested in.
-- If the attribute is at the end of the tuple this will take much longer. Thus columns
-- which are more likely to be used in a (Merge) join should be listed first.
DROP TABLE IF EXISTS facts CASCADE;
CREATE TABLE facts (
  corpus_ref integer REFERENCES corpus(id) ON DELETE CASCADE,
  id bigint,
  text_ref integer,
  left_token integer,
  right_token integer,
  seg_index integer,
  component_id integer, -- component id
  rank_id bigint,
  pre integer, -- pre-order value
  post integer, -- post-order value
  parent integer, -- foreign key to rank.pre of the parent node, or NULL for roots
  "level" integer,
  node_anno_category INTEGER REFERENCES annotation_category(id),
  node_annotext varchar COLLATE "C", -- the combined name and value of the annotation, separated by ":"
  span varchar COLLATE "C",
  node_qannotext varchar COLLATE "C", -- the combined qualified name (with namespace) of the annotation, separated by ":"
  node_namespace varchar COLLATE "C",
  node_name varchar COLLATE "C",
  salt_id varchar COLLATE "C",
  "left" integer,
  "right" integer,
  token_index integer,
  is_token boolean,
  seg_name varchar COLLATE "C",
  root boolean,
  edge_type character(1) COLLATE "C", -- edge type of this component
  edge_namespace varchar COLLATE "C", -- optional namespace of the edges’ names
  edge_name varchar COLLATE "C", -- name of the edges in this component
  edge_annotext varchar COLLATE "C", -- the combined name and value of the annotation, separated by ":"
  edge_qannotext varchar COLLATE "C", -- the combined qualified name (with namespace) of the annotation, separated by ":"
  n_sample boolean,
  n_na_sample boolean,
  toplevel_corpus integer REFERENCES corpus(id) ON DELETE CASCADE,
  fid bigserial,
  PRIMARY KEY (fid),
  -- additional check constraints
  CHECK(left_token <= right_token),
  CHECK(pre <= post)
-- this check causes problems with some corpora that were created with a buggy version of the Pepepr ANNIS Exporter
--  CHECK("left" <= "right")
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
  filename  text COLLATE "C" NOT NULL,
  corpus_ref  integer NOT NULL REFERENCES corpus(id) ON DELETE CASCADE,
  mime_type varchar COLLATE "C" NOT NULL,
  title varchar COLLATE "C" NOT NULL,
  UNIQUE (corpus_ref, title)
);

DROP TABLE IF EXISTS corpus_alias CASCADE;
CREATE TABLE corpus_alias
(
  alias text COLLATE "C",
  corpus_ref bigint references corpus(id) ON DELETE CASCADE,
   PRIMARY KEY (alias, corpus_ref)
);

DROP TABLE IF EXISTS url_shortener CASCADE;
CREATE  TABLE url_shortener 
(
  id UUID PRIMARY KEY,
	"owner" varchar,
  created timestamp with time zone,
  url varchar
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
  max_node_id bigint NULL,
  source_path varchar COLLATE "C" -- original path to the folder containing the ANNIS format sources
);


DROP VIEW IF EXISTS corpus_info CASCADE;
CREATE VIEW corpus_info AS 
SELECT min(corpus_stats.name::text) AS name,
    corpus_stats.id,
    min(corpus_stats.text) AS text,
    min(corpus_stats.tokens) AS tokens,
    min(corpus_stats.source_path::text) AS source_path,
    array_remove(array_agg(a.alias), NULL) AS alias
FROM corpus_stats LEFT JOIN corpus_alias AS a ON (corpus_stats.id = a.corpus_ref)
GROUP BY corpus_stats.id;

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
  "corpus"   varchar COLLATE "C",
  "version"   varchar COLLATE "C",
  "namespace"  varchar COLLATE "C",
  "element"    varchar COLLATE "C" CHECK (element = 'node' OR element = 'edge'),
  "vis_type"   varchar COLLATE "C" NOT NULL,
  "display_name"   varchar COLLATE "C" NOT NULL,
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
  namespace varchar COLLATE "C",
  "name" varchar COLLATE "C",
  "value" varchar COLLATE "C",
  occurences bigint,
  "type" varchar COLLATE "C",
  "subtype" char(1),
  edge_namespace varchar COLLATE "C",
  edge_name varchar COLLATE "C",
  toplevel_corpus integer NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_config CASCADE;
CREATE TABLE user_config
(
  id varchar NOT NULL,
  config varchar, -- (should be json)
  PRIMARY KEY(id)
);

--CREATE TYPE ops AS ENUM ('.','.*','>','->','_i_');
DROP TABLE IF EXISTS example_queries;
CREATE TABLE example_queries
(
  "id" serial PRIMARY KEY,
  "example_query" TEXT COLLATE "C" NOT NULL,
  "description" TEXT COLLATE "C" NOT NULL,
  "type" TEXT COLLATE "C" NOT NULL,
  "nodes" INTEGER NOT NULL,
  "used_ops" TEXT[] COLLATE "C" NOT NULL,
  "corpus_ref" integer NOT NULL REFERENCES corpus (id) ON DELETE CASCADE
);


-- HACK: add a custom operator which is the same as "=" for integers but always
-- returns 0.995 as join selectivity. See the description
-- of "annis.hack_operator_same_span" in conf/develop.properties or the
-- comments in DefaultWhereClauseGenerator#addSameSpanConditions(...)
-- for details.
DROP OPERATOR IF EXISTS ^=^ (integer, integer);
CREATE OPERATOR ^=^ (
 PROCEDURE= int4eq,
 LEFTARG = integer,
 RIGHTARG = integer,
 JOIN = 'nlikejoinsel',
 MERGES
);