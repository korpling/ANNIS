-- (modified) source tables

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