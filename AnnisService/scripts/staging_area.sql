-- corpora
-- each corpus has a unique name
-- corpora are ordered in a tree
CREATE TABLE _corpus
(
	id 			numeric(38) NOT NULL,		-- primary key
	name		varchar(100) NOT NULL,		-- unique name
	type		varchar(100) NOT NULL,		-- CORPUS, DOCUMENT, SUBDOCUMENT (not used)
	version		varchar(100),				-- version number (not used)
	pre			numeric(38)	NOT NULL,		-- pre and post order of the corpus tree
	post		numeric(38)	NOT NULL
);
CREATE TRIGGER unique_toplevel_corpus_name BEFORE UPDATE ON _corpus FOR EACH ROW EXECUTE PROCEDURE unique_toplevel_corpus_name();

-- corpus annotations
-- unique combinantion of corpus_ref, namespace and name
CREATE TABLE _corpus_annotation
(
	corpus_ref	numeric(38) NOT NULL,		-- foreign key to _corpus.id
	namespace	varchar(100),
	name		varchar(1000) NOT NULL,
	value		varchar(2000)
);

-- source texts
-- stores each source text in its entirety
CREATE TABLE _text
(
	id 		numeric(38) NOT NULL,			-- primary key
	name	varchar(1000),					-- name (not used)
	text 	text							-- text contents (not used)
);

-- nodes in the annotation graph
-- nodes are named
-- are part of a corpus and reference a text
-- cover a span of the text
-- can be tokens (token_index NOT NULL, span NOT NULL)
CREATE TABLE _node
(
	id 				numeric(38)	NOT NULL,	-- primary key
	text_ref 		numeric(38) NOT NULL,	-- foreign key to _text.id
	corpus_ref		numeric(38) NOT NULL,	-- foreign key to _corpus.id
	namespace		varchar(100),			-- namespace (not used)
	name 			varchar(100) NOT NULL,	-- name (not used)
	"left" 			integer NOT NULL,		-- start of covered substring in _text.text (inclusive)
	"right" 		integer NOT NULL,		-- end of covered substring in _text.text (inclusive)
	token_index		integer,				-- token number in _text.text, NULL if node is not a token
	continuous		boolean,				-- true if spanned text in _text.text is continuous (not used)
	span			varchar(2000)			-- for tokens: substring in _text.text (indexed for text search), else: NULL
);

-- connected components of the annotation graph
-- are of a type: Coverage, Dominance, Pointing relation or NULL for root nodes
-- have a name
CREATE TABLE _component
(
	id			numeric(38) NOT NULL,		-- primary key
	type		char(1),					-- edge type: c, d, P, NULL
	namespace	varchar(255),				-- namespace (not used)
	name		varchar(255)
);

-- pre and post order of the annotation graph
-- root nodes: parent IS NULL
-- component and rank together model edges in the annotation graph
CREATE TABLE _rank
(
	pre				numeric(38)	NOT NULL,	-- pre and post order of the annotation ODAG
	post			numeric(38)	NOT NULL,
	node_ref		numeric(38)	NOT NULL,	-- foreign key to _node.id
	component_ref	numeric(38) NOT NULL,	-- foreign key to _component.id
	parent			numeric(38) NULL		-- foreign key to _rank.pre, NULL for root nodes
);

-- node annotations
-- unique combinantion of node_ref, namespace and name
CREATE TABLE _node_annotation
(
	node_ref	numeric(38)	NOT NULL,		-- foreign key to _node.id
	namespace	varchar(150),
	name		varchar(150) NOT NULL,
	value		varchar(1500)
);

-- edge annotations
-- unique combinantion of node_ref, namespace and name
CREATE TABLE _edge_annotation
(
	rank_ref		numeric(38)	NOT NULL,	-- foreign key to _rank.pre
	namespace		varchar(1000),
	name			varchar(1000) NOT NULL,
	value			varchar(1000)
);

--- annotation visualisation (not used)
CREATE TABLE _corp_2_viz
(
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"type_ref"		numeric(38) NOT NULL
);

CREATE TABLE _xcorp_2_viz
(
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"annotation"	character varying(100) NOT NULL,
	"type_ref"		numeric(38) NOT NULL
);
