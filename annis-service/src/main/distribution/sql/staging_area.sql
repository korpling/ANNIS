-- cleanup in the proper order
DROP TABLE IF EXISTS _media_files;
DROP TABLE IF EXISTS _resolver_vis_map;
DROP TABLE IF EXISTS _corpus_stats;
DROP TABLE IF EXISTS _corpus_annotation;
DROP TABLE IF EXISTS _edge_annotation;
DROP TABLE IF EXISTS _rank;
DROP TABLE IF EXISTS _component;
DROP TABLE IF EXISTS _node_annotation;
DROP TABLE IF EXISTS _node;
DROP TABLE IF EXISTS _corpus;
DROP TABLE IF EXISTS _text;
DROP TABLE IF EXISTS _example_queries;


-- corpora
-- each corpus has a unique name
-- corpora are ordered in a tree

CREATE :tmp TABLE _corpus
(
	id 			integer NOT NULL,		-- primary key
	name		varchar NOT NULL COLLATE "C",		-- unique name
	type		varchar NOT NULL COLLATE "C",		-- CORPUS, DOCUMENT, SUBDOCUMENT (not used)
	version		varchar COLLATE "C",				-- version number (not used)
	pre			integer	NOT NULL,		-- pre and post order of the corpus tree
	post		integer	NOT NULL,
  top_level boolean NOT NULL
);

-- corpus annotations
-- unique combinantion of corpus_ref, namespace and name

CREATE :tmp TABLE _corpus_annotation
(
	corpus_ref	integer NOT NULL,		-- foreign key to _corpus.id
	namespace	varchar,
	name		varchar NOT NULL,
	value		varchar
);

-- source texts
-- stores each source text in its entirety
CREATE :tmp TABLE _text
(
  corpus_ref	integer NOT NULL,		-- foreign key to _corpus.id
	id 		integer NOT NULL,			-- primary key
	name	varchar COLLATE "C",					-- name (not used)
	text 	text							-- text contents (not used)
);

-- nodes in the annotation graph
-- nodes are named
-- are part of a corpus and reference a text
-- cover a span of the text
-- can be tokens (token_index NOT NULL, span NOT NULL)
CREATE :tmp TABLE _node
(
	id 				bigint	NOT NULL,	-- primary key
	text_ref 		integer NOT NULL,	-- foreign key to _text.id
	corpus_ref		integer NOT NULL,	-- foreign key to _corpus.id
	layer		varchar,			--  layer in which the node is contained
	name 			varchar NOT NULL COLLATE "C",	-- name of the node
	"left" 			integer NOT NULL,		-- start of covered substring in _text.text (inclusive)
	"right" 		integer NOT NULL,		-- end of covered substring in _text.text (inclusive)
	token_index		integer,				-- token number in _text.text, NULL if node is not a token
  left_token integer NOT NULL,
  right_token integer NOT NULL,
	seg_index     integer,        -- index of this segment (if it is a segment, i.e. there is some SOrderingRelation connected to this node)
  seg_name    varchar COLLATE "C",      -- segmentation name
	span			varchar,			-- for tokens: substring in _text.text (indexed for text search), else: NULL
  root boolean NOT NULL
);

-- connected components of the annotation graph
-- are of a type: Coverage, Dominance, Pointing relation or NULL for root nodes
-- have a name
CREATE :tmp TABLE _component
(
	id			integer NOT NULL,		-- primary key
	type		char(1),			-- edge type: c, d, P, NULL
	layer	varchar,				-- layer in which the edge is contained
	name		varchar       --The sType of the component, e.g anaphoric for a some kind of pointing relation component

);

-- pre and post order of the annotation graph
-- root nodes: parent IS NULL
-- component and rank together model edges in the annotation graph
CREATE :tmp TABLE _rank
(
  id        bigint NOT NULL,
	pre				integer	NOT NULL,	-- pre and post order of the annotation ODAG
	post			integer	NOT NULL,
	node_ref		bigint	NOT NULL,	-- foreign key to _node.id
	component_ref	bigint NOT NULL,	-- foreign key to _component.id
	parent			bigint NULL,		-- foreign key to _rank.id, NULL for root nodes
  level       integer         -- level of this rank entry (not node!) in the component tree
);

-- node annotations
-- unique combinantion of node_ref, namespace and name
CREATE :tmp TABLE _node_annotation
(
	node_ref	bigint	NOT NULL,		-- foreign key to _node.id
	namespace	varchar,
	name		varchar NOT NULL,
	value		varchar
);


-- edge annotations
-- unique combinantion of node_ref, namespace and name
CREATE :tmp TABLE _edge_annotation
(
	rank_ref		bigint	NOT NULL,	-- foreign key to _rank.id
	namespace		varchar,
	name			varchar NOT NULL,
	value			varchar
);

-- resolver visualization mappings
-- this table is just a subset of resolver_vis_map. It contains all columns needed for copying data from ANNIS format
CREATE :tmp TABLE _resolver_vis_map
(
  "corpus"   varchar, -- the name of the supercorpus
  "version" 	varchar, -- the version of the corpus
  "namespace"	varchar, -- the several layers of the corpus
  "element"    varchar, -- the type of the entry: node | edge
  "vis_type"   varchar NOT NULL, -- the abstract type of visualization: tree, discourse, grid, ...
  "display_name"   varchar NOT NULL, -- the name of the layer which shall be shown for display
  "visibility"  varchar default 'hidden' NOT NULL,
  "order" integer default '0', -- the order of the layers, in which they shall be shown
  "mappings" varchar
);

CREATE :tmp TABLE _media_files
(
  filename  text NOT NULL,
  corpus_ref  bigint NOT NULL,
  mime_type varchar NOT NULL,
  title varchar NOT NULL
);

CREATE :tmp TABLE _example_queries
(
  "example_query" TEXT NOT NULL,
  "description" TEXT NOT NULL
);
