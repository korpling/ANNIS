-- **************************************************************************************************************
-- *****					SQL create script zur Erzeugung des relANNIS Schemas					*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		3.1															*****
-- *****	Datum:		01.04.2009													*****
-- **************************************************************************************************************

-- Dieses Script erzeugt alle für relANNIS nötigen Relationen mit den dazugehörigen Indizes, Achtung, es werden alle Relationen vorher
-- gelöscht
-- Es ist notwendig für den PAULAImporter
-- Änderungen zu http://www2.informatik.hu-berlin.de/mac/macwiki/wiki/DddSqlSchema:
-- Die Relationen Span und Element sind zu Element zusammengefasst
-- Auf das Attribut Element.parent_id wird verzichtet
-- Die Relation bekommt einen zusätzlichen Identifier id, damit ist pre nicht primary key, sonder id

-- Datenbank anlegen:
-- ==================
-- 1)	Einen neuen Benutzer mit Super-User Rechten anlegen.
--		Name:		relANNIS_user 
-- 		Passwort:		relANNIS
--
-- 2) 	Die Datenbank relANNIS anlegen, dafür gibt es das Create-Script relANNIS.dat.
-- 	Die Datenbank kann in einer Shell mit POSTGRES_HOME\psql -U relANNIS_user -d relANNIS -f datei angelegt werden.
-- 	!!! ACHTUNG: Der Postgres-Server verlangt, dass die Datei, bzw. das Verzeichnis in dem diese liegt freigegeben wird
-- 	Unter Windows muss man sie im ganzen Netzwerk freigeben, damit der DB-Server darauf zugreifen kann

--
-- Name: relANNIS; Type: DATABASE; Schema: -; Owner: relANNIS_user
--
--DROP DATABASE "relANNIS";

--CREATE USER "relANNIS_user" WITH PASSWORD 'relANNIS';
--CREATE DATABASE "relANNIS" ENCODING = 'UTF8';
--ALTER DATABASE "relANNIS" OWNER TO "relANNIS_user";
--\connect "relANNIS"

-- ************************************ Erzeugen der Relationen ************************************
-- ===================================== Relation: corpus =====================================
DROP TABLE corpus CASCADE;
CREATE TABLE corpus
(
	id 			numeric(38) NOT NULL,
	name		character varying(100) NOT NULL,	--unique identifier for corpus in database
	"type"		character varying(100) NOT NULL, 	-- type of entry, it can be (DOCUMENT, CORPUS)
	version		character varying(100), 			-- version of imported corpus
	pre			numeric(38)	NOT NULL,				-- Idee: pre könnte sein: id + neuer preWert, das führt dazu, dass kein artifizielles Wurzelelement eingeführt werden muss
	post		numeric(38)	NOT NULL
);
ALTER TABLE corpus ADD CONSTRAINT "PK_corpus" PRIMARY KEY (id);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_pre" UNIQUE(pre);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_post" UNIQUE(post);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_name" UNIQUE(name);
COMMENT ON TABLE corpus IS 'Relation corpus is for the metastrucure and to organize corpus and subcorpus.';
-- ===================================== Relation: meta_attribute =====================================
DROP TABLE corpus_annotation CASCADE;
CREATE TABLE corpus_annotation
(
	corpus_ref	numeric(38)					NOT NULL,
	namespace	character varying(100)		,
	name		character varying(1000)		NOT NULL,
	"value"		character varying(2000)
);
ALTER TABLE corpus_annotation ADD CONSTRAINT "UNIQ_corpus_annotation" UNIQUE (corpus_ref, namespace, name);
ALTER TABLE corpus_annotation ADD CONSTRAINT "FK_corpus_annotation_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus(id);
COMMENT ON TABLE corpus_annotation IS 'The relation meta_attribute stores meta attributes to objects of relation collection';

-- ===================================== Relation: text =====================================
DROP TABLE text CASCADE;
CREATE TABLE text
(
	id 				numeric(38) NOT NULL,
	name			character varying(1000),
	"text" 			text
);
ALTER TABLE text ADD CONSTRAINT "PK_text" PRIMARY KEY (id);
COMMENT ON TABLE text IS 'Relation text stores the primary text wich has to be tated.';
-- ===================================== Relation: node ====================================
DROP TABLE node CASCADE;
CREATE TABLE node
(
	id 				numeric(38)	NOT NULL,
	text_ref 		numeric(38) NOT NULL,
	corpus_ref		numeric(38) NOT NULL,					-- dient der Identifikation von Strukturelementen, die sich auf einen Text/ Dokument(nicht spezifiziert) beziehen
	namespace		character varying(100),					-- Namensraum des Typ-Attributs (exmeralda, tiger, urml, mmax...)
	"name"			character varying(100) NOT NULL,		-- genauere Bezeichnung aus PAULA heraus, bspw. primmarkSeg
	"left" 			integer NOT NULL,
	"right" 		integer NOT NULL,
	"token_index"	numeric(38),											-- speichert die Reihenfolge von Tupeln mit name= TOKEN, sonst NULL
	continuous 		boolean,
	"span"			text
);
ALTER TABLE node ADD CONSTRAINT "PK_node" PRIMARY KEY (id);
ALTER TABLE node ADD CONSTRAINT "FK_node_2_text" FOREIGN KEY (text_ref) REFERENCES text(id);
ALTER TABLE node ADD CONSTRAINT "FK_node_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus(id);
COMMENT ON TABLE node IS 'Relation node stores a atructure wich is build over primary data. This structure is used to annotate it.';
CREATE INDEX IDX_NODE_ORDER on node("token_index");
CREATE INDEX IDX_NODE_CORP_REF on node("corpus_ref");
-- ===================================== Relation: edge =====================================
DROP TABLE "edge" CASCADE;
CREATE TABLE "edge"
(
	pre					numeric(38)	NOT NULL,
	post				numeric(38)	NOT NULL,
	node_ref			numeric(38)	NOT NULL,
	component_ref		numeric(38) NOT NULL,
	parent				numeric(38)
);
ALTER TABLE "edge" ADD CONSTRAINT "PK_edge" PRIMARY KEY (pre);
ALTER TABLE "edge" ADD CONSTRAINT "FK_edge_2_node" FOREIGN KEY (node_ref) REFERENCES node(id);
ALTER TABLE "edge" ADD CONSTRAINT "FK_edge_2_rt" FOREIGN KEY (component_ref) REFERENCES component(id);
ALTER TABLE "edge" ADD CONSTRAINT "FK_edge_2_edge" FOREIGN KEY (parent) REFERENCES edge(pre);
ALTER TABLE "edge" ADD CONSTRAINT "KEY_edge_pre" UNIQUE(pre);
ALTER TABLE "edge" ADD CONSTRAINT "KEY_edge_post" UNIQUE(post);
COMMENT ON TABLE "edge" IS 'The Relation edge builds the structure of nodes. It relates the tuple of node with eaxh other. The flag dominance means, this edge is an dominance-edge or a non-dominance-edge.';
CREATE INDEX IDX_edge_node_ref ON "edge"(node_ref);

-- ===================================== Relation: component =====================================
DROP TABLE component CASCADE; 
CREATE TABLE component
(
	id 					numeric(38) NOT NULL,
	"type"				character (1), 				-- stores super connected component types like (d= dominance relation, p= pointing relation, c= coverage relation)
	namespace			character varying(100),
	name				character varying(100)	 	-- stores sub connected component types, these types are partitions of super connected component types (examples are edge or secedge from tiger)
);
COMMENT ON TABLE component IS 'Relation component contains parts of relation edge. It describes the kind of an edge.';
ALTER TABLE component ADD CONSTRAINT "PK_component" PRIMARY KEY (id);
-- ===================================== Relation: edge_annotation =====================================
DROP TABLE edge_annotation CASCADE;
CREATE TABLE edge_annotation
(
	edge_ref		numeric(38)	NOT NULL,
	namespace		character varying(1000),
	"name"			character varying(1000) NOT NULL,
	"value"			character varying(1000)
);
ALTER TABLE edge_annotation ADD CONSTRAINT "UNIQ_edge_annotation" UNIQUE (edge_ref, namespace, name);
ALTER TABLE edge_annotation ADD CONSTRAINT "FK_edge_anno_2_edge" FOREIGN KEY (edge_ref) REFERENCES "edge"(pre);
COMMENT ON TABLE edge_annotation IS 'Relation edge_annotation is an edge annotation for tuples in relation edge.';
CREATE INDEX IDX_edge_anno_edge_ref ON edge_annotation(edge_ref);
-- ===================================== Relation: anno =====================================
DROP TABLE node_annotation CASCADE;
CREATE TABLE node_annotation
(
	id 					numeric(38),
	node_ref		numeric(38)	NOT NULL,
	namespace		character varying(150),		-- Typensatz (urml, exmeralda, tiger)
	name		character varying(150)		NOT NULL,
	"value"		character varying(1500)
);
ALTER TABLE node_annotation ADD CONSTRAINT "UNIQ_anno" UNIQUE (node_ref, namespace, name);
ALTER TABLE node_annotation ADD CONSTRAINT "FK_anno_2_node" FOREIGN KEY (node_ref) REFERENCES node(id);
COMMENT ON TABLE node_annotation IS 'Relation node_annotation contains all annotations over a structural node.';

-- ===================================== Relation: anno_attribute =====================================
-- ************************************ Ende Erzeugen der Relationen ************************************