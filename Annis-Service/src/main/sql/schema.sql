-- (modified) source tables
CREATE TABLE corpus
(
	id			numeric(38) PRIMARY KEY,
	name		varchar(100) NOT NULL, -- UNIQUE,
	type		varchar(100) NOT NULL,
	version 	varchar(100),
	pre			numeric(38) NOT NULL UNIQUE,
	post		numeric(38) NOT NULL UNIQUE,
	top_level	boolean NOT NULL	-- true for roots of the corpus forest
);
COMMENT ON COLUMN corpus.id IS 'primary key';
COMMENT ON COLUMN corpus.name IS 'name of the corpus';
COMMENT ON COLUMN corpus.pre IS 'pre-order value';
COMMENT ON COLUMN corpus.post IS 'post-order value';

CREATE TABLE corpus_annotation
(
	corpus_ref	numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
	namespace	varchar(100),
	name		varchar(1000) NOT NULL,
	value		varchar(2000),
	UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';

CREATE TABLE text
(
	id		numeric(38) PRIMARY KEY,
	name	varchar(1000),
	text	text
);
COMMENT ON COLUMN text.id IS 'primary key';
COMMENT ON COLUMN text.name IS 'informational name of the primary data text';
COMMENT ON COLUMN text.text IS 'raw text data';

--CREATE TABLE node
--(
--	id			numeric(38)	PRIMARY KEY,
--	text_ref	numeric(38) NOT NULL REFERENCES text (id) ON DELETE CASCADE,
--	corpus_ref	numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
--	namespace	varchar(100),
--	name		varchar(100) NOT NULL,
--	"left"		integer NOT NULL,
--	"right"		integer NOT NULL,
--	token_index	integer,
--  is_token boolean,
--	continuous	boolean,
--	span		varchar(2000),
--	toplevel_corpus numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
--	left_token	integer NULL,	-- token_index of left-most token in tree under this node
--	right_token	integer	NULL	-- token_index of right-most token in tree under this node
--);
--COMMENT ON COLUMN node.id IS 'primary key';
--COMMENT ON COLUMN node.corpus_ref IS 'foreign key to corpus.id';
--COMMENT ON COLUMN node.toplevel_corpus IS 'foreign key to toplevel corpus.id';
--COMMENT ON COLUMN node.namespace IS 'optional namespace of the node''s name';
--COMMENT ON COLUMN node.name IS 'name of the node';
--COMMENT ON COLUMN node.text_ref IS 'foreign key to text.id';
--COMMENT ON COLUMN node."left" IS 'left text span border (inclusive)';
--COMMENT ON COLUMN node."right" IS 'right text span border (inclusive)';
--COMMENT ON COLUMN node.continuous IS 'true if the span (text_ref, left, right) is gap-free, otherwise false';
--COMMENT ON COLUMN node.token_index IS 'token position if the span (text_ref, left, right) is a token, otherwise NULL';
--COMMENT ON COLUMN node.span IS 'the covered text if the span is a token, otherwise NULL';
--
--CREATE TABLE node_annotation
--(
--	node_ref	numeric(38), -- REFERENCES node (id) ON DELETE CASCADE,
--  toplevel_corpus numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
--	namespace	varchar(150),
--	name		varchar(150) NOT NULL,
--	value		varchar(1500)
--);
--COMMENT ON COLUMN node_annotation.node_ref IS 'foreign key to node.id';
--COMMENT ON COLUMN node_annotation.namespace IS 'optional namespace of annotation key';
--COMMENT ON COLUMN node_annotation.name IS 'annotation key';
--COMMENT ON COLUMN node_annotation.value IS 'annotation value';


CREATE TABLE facts
(
  fid BIGSERIAL PRIMARY KEY,
  id			numeric(38),
	text_ref	numeric(38),
	corpus_ref	numeric(38),
	toplevel_corpus numeric(38),
  node_namespace	varchar(100),
	node_name		varchar(100),
	"left"		integer,
	"right"		integer,
	token_index	integer,
  is_token boolean,
	continuous	boolean,
	span		varchar(2000),
	left_token	integer,
	right_token	integer,

  pre				numeric(38),
	post			numeric(38),
	parent		numeric(38),
	root			boolean,
	level			numeric(38),

  component_id			numeric(38),
	edge_type		char(1),
	edge_namespace	varchar(255),
	edge_name		varchar(255),

	node_annotation_namespace	varchar(150),
	node_annotation_name		varchar(150),
	node_annotation_value		varchar(1500),

  edge_annotation_namespace	varchar(150),
	edge_annotation_name		varchar(150),
	edge_annotation_value		varchar(1500),

  sample bit(5)
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
COMMENT ON COLUMN facts.sample IS 'Bit mask if sample for join of original table [n, n_na, n_r_c, n_r_c_ea, n_r_c_na]';

-- external data
CREATE TABLE extData
(
	id			serial PRIMARY KEY,
	filename	varchar(500) NOT NULL,
	orig_name	varchar(100) NOT NULL,
	branch		varchar(100) NOT NULL,
	mime		varchar(100) NOT NULL,
	comment		varchar(1500) NOT NULL,
	UNIQUE (filename, branch)
   
);


-- stats
CREATE TABLE corpus_stats
(
	name				varchar,
	id					numeric NOT NULL REFERENCES corpus ON DELETE CASCADE,
	corpus				numeric,
	text				numeric,
	node				numeric,
	rank				numeric,
	component			numeric,
	corpus_annotation	numeric,
	node_annotation		numeric,
	edge_annotation		numeric,
	tokens				numeric,
	roots				numeric,
	edges				numeric,
	depth				numeric,
	c_comps				numeric,
	c_edges				numeric,
	d_comps				numeric,
	d_edges				numeric,
	p_comps				numeric,
	p_edges				numeric,
	u_comps				numeric,
	u_edges				numeric,
	avg_level			real,
	avg_children		real,
	avg_duplicates		real
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
  "version" 	varchar(100),
  "namespace"	varchar(100),
  "element"    varchar(4) CHECK (element = 'node' OR element = 'edge'),
  "vis_type"   varchar(100) NOT NULL,
  "display_name"   varchar(100) NOT NULL,
  "order" numeric default '0',
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
  toplevel_corpus bigint NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);
