-- ***********************************************************************************************************
-- *****						SQL create script zur Erzeugung des relANNIS Schemas											*****
-- ***********************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.1																*****
-- *****	Datum:		03.03.2008														*****
-- ******************************************************************************************

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
-- 		Passwort:	relANNIS
--
-- 2) 	Die Datenbank relANNIS anlegen, dafür gibt es das Create-Script relANNIS.dat.
-- 	Die Datenbank kann in einer Shell mit POSTGRES_HOME\psql -U relANNIS_user -d relANNIS -f datei angelegt werden.
-- 	!!! ACHTUNG: Der Postgres-Server verlangt, dass die Datei, bzw. das Verzeichnis in dem diese liegt freigegeben wird
-- 	Unter Windows muss man sie im ganzen Netzwerk freigeben, damit der DB-Server darauf zugreifen kann

--
-- Name: relANNIS; Type: DATABASE; Schema: -; Owner: relANNIS_user
--
DROP DATABASE "relANNIS";
DROP USER "relANNIS_user";
CREATE ROLE "relANNIS_user" LOGIN
  ENCRYPTED PASSWORD 'md50d49afc443f2e2a6423873e7085c413e'
  SUPERUSER INHERIT CREATEDB CREATEROLE;
UPDATE pg_authid SET rolcatupdate=true WHERE OID=16636::oid;

--CREATE USER "relANNIS_user" WITH PASSWORD 'relANNIS';
CREATE DATABASE "relANNIS" ENCODING = 'UTF8';
ALTER DATABASE "relANNIS" OWNER TO "relANNIS_user";
\connect "relANNIS"

-- ************************************ löschen der Relationen ************************************
DROP TABLE meta_attribute;
DROP TABLE anno_attribute;
DROP TABLE anno;
DROP TABLE rank;
DROP TABLE doc_2_korp;
DROP TABLE struct;
DROP TABLE text;
DROP TABLE korpus;
-- ************************************ Ende löschen der Relationen ************************************

-- ************************************ Erzeugen der Relationen ************************************
-- ===================================== Relation: Korpus =====================================
CREATE TABLE korpus
(
	id 		numeric(38) NOT NULL,
	name	character varying(100) NOT NULL,
	pre		numeric(38)	NOT NULL,			-- Idee: pre könnte sein: id + neuer preWert, das führt dazu, dass kein artifizielles Wurzelelement eingeführt werden muss
	post		numeric(38)	NOT NULL,
	CONSTRAINT "PK_korpus" PRIMARY KEY (id),
	CONSTRAINT "KEY_korpus_pre" UNIQUE(pre),
	CONSTRAINT "KEY_korpus_post" UNIQUE(post)
);
COMMENT ON TABLE korpus IS 'Relation korpus is for the metastrucure and to organize korpus and subkorpus..';

-- ===================================== Relation: document =====================================
 CREATE TABLE doc_2_korp
(
	doc_id				numeric(38)	NOT NULL,
	korpus_ref		numeric(38)	NOT NULL,
	CONSTRAINT "PK_doc" PRIMARY KEY (doc_id, korpus_ref),
	CONSTRAINT "FK_doc_2_korpus" FOREIGN KEY (korpus_ref) REFERENCES korpus(id)
) ;
COMMENT ON TABLE doc_2_korp IS 'The Relation doc_2_korp relates the root-tuples of struct and korpus with each other';

-- ===================================== Relation: text =====================================
CREATE TABLE text
(
	id 				numeric(38) NOT NULL,
	name			character varying(150)	NOT NULL,
	"text" 			text,
	CONSTRAINT "PK_text" PRIMARY KEY (id)
);
COMMENT ON TABLE text IS 'Relation text stores the primary text wich has to be annotated.';
-- ===================================== Relation: struct =====================================
 CREATE TABLE struct
(
	id 			numeric(38)	NOT NULL,
	text_ref 	numeric(38),
	doc_ref	numeric(38),											-- dient der Identifikation von Strukturelementen, die sich auf einen Text/ Dokument(nicht spezifiziert) beziehen
	name 		character varying(100) NOT NULL,		-- ALL, ROOT, STRUCT, STRUCTEDGE oder TOKEN(bisher bekannt)
	"left" 		integer,
	"right" 		integer,
	"order"	numeric(38),											-- speichert die Reihenfolge von Tupeln mit name= TOKEN, sonst NULL
	cont 		boolean,
	"text"		text,
	CONSTRAINT "PK_struct" PRIMARY KEY (id),
	CONSTRAINT "FK_struct_2_text" FOREIGN KEY (text_ref) REFERENCES text(id)
	--CONSTRAINT "FK_struct_2_doc" FOREIGN KEY (doc_ref) REFERENCES doc_2_korp(doc_id) doc_id ist nicht eindeutig, daher kann es kein FK geben
) ;
COMMENT ON TABLE struct IS 'Relation struct stores a atructure wich is build over primary data. This structure is used to annotate it.';
CREATE INDEX IDX_STRUCT_ORDER on struct("order");
-- ===================================== Relation: rank =====================================
 CREATE TABLE rank
(
	pre					numeric(38)	NOT NULL,
	post					numeric(38)	NOT NULL,
	struct_ref			numeric(38)	NOT NULL,
	parent				numeric(38),
	CONSTRAINT "PK_rank" PRIMARY KEY (pre),
	CONSTRAINT "FK_rank_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id),
	CONSTRAINT "FK_rank_2_rank" FOREIGN KEY (parent) REFERENCES rank(pre),	
	CONSTRAINT "KEY_rank_pre" UNIQUE(pre),
	CONSTRAINT "KEY_rank_post" UNIQUE(post)
) ;
COMMENT ON TABLE rank IS 'The Relation rank builds the structure uf struct. It relates the tuple of struct with eaxh other.';

-- ===================================== Relation: anno =====================================
 CREATE TABLE anno
(
	id 				numeric(38)	NOT NULL,
	struct_ref		numeric(38)	NOT NULL,
	anno_level	character varying(150),		-- Typensatz (urml, exmeralda, tiger)
	CONSTRAINT "PK_anno" PRIMARY KEY (id),
	CONSTRAINT "FK_anno_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id)
) ;
COMMENT ON TABLE anno IS 'Relation anno is a placeholder for real annotations and is build over the relation struct.';

-- ===================================== Relation: anno_attribute =====================================
 CREATE TABLE anno_attribute
(
	anno_ref	numeric(38)						NOT NULL,
	name		character varying(150)		NOT NULL,
	"value"	character varying(150),
	CONSTRAINT "PK_anno_attribute" PRIMARY KEY (anno_ref, name),
	CONSTRAINT "FK_anno_attribute_2_anno" FOREIGN KEY (anno_ref) REFERENCES anno(id)
) ;
COMMENT ON TABLE anno_attribute IS 'Relation anno_attribute stores the real annotation over the anno tuples.';

-- ===================================== Relation: anno_attribute =====================================
 CREATE TABLE meta_attribute
(
	elem_ref	character varying(150)		NOT NULL,
	name		character varying(150)		NOT NULL,
	"value"	character varying(150),
	CONSTRAINT "PK_meta_attribute" PRIMARY KEY (elem_ref, name)
) ;
COMMENT ON TABLE anno_attribute IS 'The relation meta_attribute stores meta attributes to objects of relation korpus, text, struct and anno';

-- ************************************ Ende Erzeugen der Relationen ************************************