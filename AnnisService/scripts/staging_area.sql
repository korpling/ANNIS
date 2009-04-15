--- ODAG
CREATE TABLE _struct
(
	id 				numeric(38)	NOT NULL,
	text_ref 		numeric(38),
	col_ref			numeric(38),
	doc_ref			numeric(38),
	element_ns		varchar(2000),
	name 			character varying(100) NOT NULL,
	type 			character varying(100) NOT NULL,
	"left" 			integer,
	"right" 		integer,
	token_index		integer,
	cont 			boolean,
	span			character varying(2000)
);

CREATE TABLE _rank
(
	pre				numeric(38)	NOT NULL,
	post			numeric(38)	NOT NULL,
	struct_ref		numeric(38)	NOT NULL,
	parent			numeric(38) NULL,
	dominance		boolean
);

CREATE TABLE _rank_anno
(
	rank_ref		numeric(38)	NOT NULL,
	type			character varying(100),
	edge			character varying(100),
	value			character varying(100)
);

CREATE TABLE _anno
(
	id 				numeric(38)	NOT NULL,
	struct_ref		numeric(38)	NOT NULL,
	col_ref			numeric(38) NOT NULL,
	anno_level		character varying(150)
);

CREATE TABLE _anno_attribute
(
	anno_ref		numeric(38) NOT NULL,
	attribute		character varying(150) NOT NULL,
	value			character varying(1500)
);

CREATE TABLE _text
(
	id 		numeric(38) NOT NULL,
	name	character varying(150) NOT NULL,
	text 	text,
	col_ref	numeric(38) NOT NULL
);

--- Korpusverwaltung
CREATE TABLE _corpus
(
	id 			numeric(38) NOT NULL,
	timestamp_id	numeric(38) NOT	NULL,
	name		character varying(100) NOT NULL,
	pre			numeric(38)	NOT NULL,
	post		numeric(38)	NOT NULL
);

CREATE TABLE _doc_2_corp
(
	doc_id				numeric(38)	NOT NULL,
	corpus_ref			numeric(38)	NOT NULL
);

CREATE TABLE _document
(
	id				numeric(38)	NOT NULL,
	name			character varying(100) NOT NULL
);

--- Meta-Attribute
CREATE TABLE _col_rank
(
	col_ref		numeric(38) NOT NULL,
	pre			numeric(38)	NOT NULL,
	post		numeric(38)	NOT NULL
);

CREATE TABLE _collection
(
	id			numeric(38) NOT NULL,
	type		character varying(100) NOT NULL,
	name		character varying(100) NOT NULL
);

CREATE TABLE _meta_attribute
(
	col_ref		numeric(38) NOT NULL,
	name		character varying(2000)	NOT NULL,
	value		character varying(2000)
);

--- Visualisierung
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
