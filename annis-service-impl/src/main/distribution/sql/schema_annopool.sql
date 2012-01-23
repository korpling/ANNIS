-- (modified) source tables
CREATE TABLE corpus
(
  id         bigint PRIMARY KEY,
  name       varchar(100) NOT NULL, -- UNIQUE,
  type       varchar(100) NOT NULL,
  version    varchar(100),
  pre        bigint NOT NULL UNIQUE,
  post       bigint NOT NULL UNIQUE,
  top_level  boolean NOT NULL,  -- true for roots of the corpus forest
  path_name  varchar(100)[]
);
COMMENT ON COLUMN corpus.id IS 'primary key';
COMMENT ON COLUMN corpus.name IS 'name of the corpus';
COMMENT ON COLUMN corpus.pre IS 'pre-order value';
COMMENT ON COLUMN corpus.post IS 'post-order value';
COMMENT ON COLUMN corpus.path_name IS 'path of this corpus in the corpus tree (names)';

CREATE TABLE corpus_annotation
(
  corpus_ref  bigint NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  namespace   varchar(100),
  name        varchar(1000) NOT NULL,
  value       varchar(2000),
  UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';

CREATE TABLE text
(
  id    bigint PRIMARY KEY,
  name  varchar(1000),
  text  text
);
COMMENT ON COLUMN text.id IS 'primary key';
COMMENT ON COLUMN text.name IS 'informational name of the primary data text';
COMMENT ON COLUMN text.text IS 'raw text data';

CREATE TYPE annotype AS ENUM ('node', 'edge');
-- collect all node annotations
CREATE TABLE annotation_pool (
  id bigserial,
  toplevel_corpus bigint REFERENCES corpus(id),
  namespace varchar(150),
  "name" varchar(150),
  val varchar(1500),
  "type" annotype,
  occurences bigint,
  PRIMARY KEY(id),
  UNIQUE(namespace, "name", val, "type", toplevel_corpus)
);

CREATE TABLE facts (
  fid bigserial,
  id bigint,
  text_ref bigint REFERENCES text(id),
  corpus_ref bigint REFERENCES corpus(id),
  toplevel_corpus bigint REFERENCES corpus(id),
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
  node_anno_ref bigint REFERENCES annotation_pool(id),
  edge_anno_ref bigint REFERENCES annotation_pool(id),
  sample bit(5), -- Bit mask if sample for join of original table [n, n_na, n_r_c, n_r_c_ea, n_r_c_na]
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

COMMENT ON COLUMN facts.sample IS 'Bit mask if sample for join of original table [n, n_na, n_r_c, n_r_c_ea, n_r_c_na]';

-- external data
CREATE TABLE extData
(
  id      serial PRIMARY KEY,
  filename  varchar(500) NOT NULL,
  orig_name  varchar(100) NOT NULL,
  branch    varchar(100) NOT NULL,
  mime    varchar(100) NOT NULL,
  comment    varchar(1500) NOT NULL,
  UNIQUE (filename, branch)
   
);


-- stats
CREATE TABLE corpus_stats
(
  name        varchar,
  id          bigint NOT NULL REFERENCES corpus ON DELETE CASCADE,
  corpus        bigint,
  text        bigint,
  node        bigint,
  rank        bigint,
  component      bigint,
  corpus_annotation  bigint,
  node_annotation    bigint,
  edge_annotation    bigint,
  tokens        bigint,
  roots        bigint,
  edges        bigint,
  depth        bigint,
  c_comps        bigint,
  c_edges        bigint,
  d_comps        bigint,
  d_edges        bigint,
  p_comps        bigint,
  p_edges        bigint,
  u_comps        bigint,
  u_edges        bigint,
  avg_level      real,
  avg_children    real,
  avg_duplicates    real
);

CREATE VIEW corpus_info AS SELECT 
  name,
  id, 
  tokens,
  roots,
  depth,
  to_char(avg_level, '990.99') as avg_level,
  to_char(avg_children, '990.99') as avg_children,
  to_char(avg_duplicates, '990.99') as avg_duplicates
FROM 
  corpus_stats;
  

CREATE TABLE resolver_vis_map
(
  "id"   serial PRIMARY KEY,
  "corpus"   varchar(100),
  "version"   varchar(100),
  "namespace"  varchar(100),
  "element"    varchar(4) CHECK (element = 'node' OR element = 'edge'),
  "vis_type"   varchar(100) NOT NULL,
  "display_name"   varchar(100) NOT NULL,
  "order" bigint default '0',
  "mappings" varchar(100),
   UNIQUE (corpus,version,namespace,element,vis_type)              
);
COMMENT ON COLUMN resolver_vis_map.id IS 'primary key';
COMMENT ON COLUMN resolver_vis_map.corpus IS 'the name of the supercorpus, part of foreign key to corpus.name,corpus.version';
COMMENT ON COLUMN resolver_vis_map.version IS 'the version of the corpus, part of foreign key to corpus.name,corpus.version';
COMMENT ON COLUMN resolver_vis_map.namespace IS 'the several layers of the corpus';
COMMENT ON COLUMN resolver_vis_map.element IS 'the type of the entry: node | edge';
COMMENT ON COLUMN resolver_vis_map.vis_type IS 'the abstract type of visualization: tree, discourse, grid, ...';
COMMENT ON COLUMN resolver_vis_map.display_name IS 'the name of the layer which shall be shown for display';
COMMENT ON COLUMN resolver_vis_map.order IS 'the order of the layers, in which they shall be shown';
COMMENT ON COLUMN resolver_vis_map.mappings IS 'which annotations in this corpus correspond to fields expected by the visualization, e.g. the tree visualizer expects a node label, which is called "cat" by default but may be changed using this field';

CREATE TABLE annotations
(
  id bigserial NOT NULL,
  namespace varchar(150),
  "name" varchar(150),
  "value" varchar(1500),
  occurences bigint,
  "type" varchar(10),
  "subtype" char(1),
  edge_namespace varchar(150),
  edge_name varchar(150),
  toplevel_corpus bigint NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);
