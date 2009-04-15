-- **************************************************************************************************************
-- *****					SQL create script zur Erzeugung des relANNIS Schemas					*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		3.0															*****
-- *****	Datum:		26.06.2008													*****
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
--ALTER DATABASE "relANNIS" OWNER TO "relANNIS";
--\connect "relANNIS"

-- ************************************ Erzeugen der Relationen ************************************
-- ===================================== Relation: corpus =====================================
DROP TABLE corpus CASCADE;
CREATE TABLE corpus
(
	id 			numeric(38) NOT NULL,
	corp_id		numeric(38) NOT NULL, -- !!! ACHTUNG !!! muss rausgenommen werden
	name		character varying(100) NOT NULL,
	"type"		character varying(100) NOT NULL, -- Typ des Eintrags, ob Doument oder Corpus (DOCUMENT, CORPUS)
	version		character varying(100) NOT NULL, -- Versions-"nummer" des Korpus
	pre			numeric(38)	NOT NULL,			-- Idee: pre könnte sein: id + neuer preWert, das führt dazu, dass kein artifizielles Wurzelelement eingeführt werden muss
	post		numeric(38)	NOT NULL
);
ALTER TABLE corpus ADD CONSTRAINT "PK_corpus" PRIMARY KEY (id);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_corp_id" UNIQUE (corp_id);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_pre" UNIQUE(pre);
ALTER TABLE corpus ADD CONSTRAINT "KEY_corpus_post" UNIQUE(post);
COMMENT ON TABLE corpus IS 'Relation corpus is for the metastrucure and to organize corpus and subcorpus.';
-- ===================================== Relation: meta_attribute =====================================
DROP TABLE corpus_meta_attribute CASCADE;
CREATE TABLE corpus_meta_attribute
(
	corpus_ref	numeric(38)					NOT NULL,
	namespace	character varying(100)		,
	name		character varying(1000)		NOT NULL,
	"value"		character varying(2000)
);
ALTER TABLE corpus_meta_attribute ADD CONSTRAINT "PK_cma" PRIMARY KEY (corpus_ref, name);
ALTER TABLE corpus_meta_attribute ADD CONSTRAINT "FK_cma_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus(id);
COMMENT ON TABLE meta_attribute IS 'The relation meta_attribute stores meta attributes to objects of relation collection';

-- ===================================== Relation: text =====================================
DROP TABLE text CASCADE;
CREATE TABLE text
(
	id 				numeric(38) NOT NULL,
	name			character varying(150)	NOT NULL,
	"text" 			text,
	CONSTRAINT "PK_text" PRIMARY KEY (id)
);
COMMENT ON TABLE text IS 'Relation text stores the primary text wich has to be tated.';
-- ===================================== Relation: struct ====================================
DROP TABLE struct CASCADE;
CREATE TABLE struct
(
	id 				numeric(38)	NOT NULL,
	text_ref 		numeric(38) NOT NULL,
	corpus_ref		numeric(38) NOT NULL,					-- dient der Identifikation von Strukturelementen, die sich auf einen Text/ Dokument(nicht spezifiziert) beziehen
	namespace		character varying(100),					-- Namensraum des Typ-Attributs (exmeralda, tiger, urml, mmax...)
	"name"			character varying(100) NOT NULL,		-- genauere Bezeichnung aus PAULA heraus, bspw. primmarkSeg
	"left" 			integer,
	"right" 		integer,
	"token_index"	numeric(38),											-- speichert die Reihenfolge von Tupeln mit name= TOKEN, sonst NULL
	continuous 		boolean,
	"span"			text,
	CONSTRAINT "PK_struct" PRIMARY KEY (id),
	CONSTRAINT "FK_struct_2_text" FOREIGN KEY (text_ref) REFERENCES text(id),
	CONSTRAINT "FK_struct_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus(id) 
);
COMMENT ON TABLE struct IS 'Relation struct stores a atructure wich is build over primary data. This structure is used to annotate it.';
CREATE INDEX IDX_STRUCT_ORDER on struct("token_index");
CREATE INDEX IDX_STRUCT_CORP_REF on struct("corpus_ref");
-- ===================================== Relation: rank =====================================
DROP TABLE "rank" CASCADE;
CREATE TABLE "rank"
(
	pre					numeric(38)	NOT NULL,
	post				numeric(38)	NOT NULL,
	struct_ref			numeric(38)	NOT NULL,
	rank_type_ref		numeric(38) NOT NULL,
	parent				numeric(38),
	CONSTRAINT "PK_rank" PRIMARY KEY (pre),
	CONSTRAINT "FK_rank_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id),
	CONSTRAINT "FK_rank_2_rt" FOREIGN KEY (rank_type_ref) REFERENCES rank_type(id),
	CONSTRAINT "FK_rank_2_rank" FOREIGN KEY (parent) REFERENCES rank(pre),	
	CONSTRAINT "KEY_rank_pre" UNIQUE(pre),
	CONSTRAINT "KEY_rank_post" UNIQUE(post)
);
COMMENT ON TABLE rank IS 'The Relation rank builds the structure uf struct. It relates the tuple of struct with eaxh other. The flag dominance means, this edge is an dominance-edge or a non-dominance-edge.';
CREATE INDEX IDX_rank_struct_ref ON rank(struct_ref);

-- ===================================== Relation: rank_type =====================================
DROP TABLE rank_type CASCADE; 
CREATE TABLE rank_type
(
	id 					numeric(38),
	"type"				character (1)				NOT NULL, -- Werte wie (d= dominance relation, p= pointing relation, c= coverage relation)
	namespace			character varying(100)		NOT NULL,
	name				character varying(100)		NOT NULL -- Werte wie edge, secedge etc.
);
COMMENT ON TABLE rank_type IS 'Relation rank_type contains parts of relation rank. It describes the kind of an edge.';
ALTER TABLE rank_type ADD CONSTRAINT "PK_rank_type" PRIMARY KEY (id);
-- ===================================== Relation: rank_anno =====================================
DROP TABLE rank_annotation CASCADE;
CREATE TABLE rank_annotation
(
	rank_ref		numeric(38)	NOT NULL,
	namespace		character varying(100),
	"name"			character varying(100),
	"value"			character varying(100),
	CONSTRAINT "FK_rank_anno_2_rank" FOREIGN KEY (rank_ref) REFERENCES rank(pre)
);
COMMENT ON TABLE rank_annotation IS 'Relation rank_annotation is an edge annotation for tuples in relation rank.';
CREATE INDEX IDX_rank_anno_rank_ref ON rank_annotation(rank_ref);
-- ===================================== Relation: anno =====================================
DROP TABLE annotation CASCADE;
CREATE TABLE annotation
(
	id 				numeric(38)	NOT NULL,
	struct_ref		numeric(38)	NOT NULL,
	namespace		character varying(150),		-- Typensatz (urml, exmeralda, tiger)
	name		character varying(150)		NOT NULL,
	"value"		character varying(1500),
	CONSTRAINT "PK_anno" PRIMARY KEY (id),
	CONSTRAINT "FK_anno_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id)
);
COMMENT ON TABLE annotation IS 'Relation annotation contains all annotations over a structural node.';
ALTER TABLE annotation ADD CONSTRAINT "UNQ_anno" UNIQUE(struct_ref, namespace, name); 

-- ===================================== Relation: anno_attribute =====================================
DROP TABLE annotation_meta_attribute CASCADE;
CREATE TABLE annotation_meta_attribute 
(
	annotation_ref	numeric(38)	NOT NULL,
	ns 				character varying(100),
	name			character varying(1000)		NOT NULL,
	"value"			character varying(2000)
);
COMMENT ON TABLE annotation_meta_attribute IS 'This relation contains meta annotations on annotations like description, examples ...';
ALTER TABLE annotation_meta_attribute ADD CONSTRAINT "FK_ama_2_anno" FOREIGN KEY (annotation_ref) REFERENCES annotation(id);
-- ************************************ Ende Erzeugen der Relationen ************************************